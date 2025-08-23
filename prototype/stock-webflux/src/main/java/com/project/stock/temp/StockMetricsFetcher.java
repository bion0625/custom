package com.project.stock.temp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * WebFlux 원시 구현(SEC + 가격 소스 다중 폴백)
 * - SEC company_tickers.json → Ticker/CIK
 * - companyconcept 1차, companyfacts 2차로 개념 로딩 안정화
 * - Duration(YTD 포함) → 분기화(quarterize): Q2=Q2YTD−Q1YTD, Q3=Q3YTD−Q2YTD, Q4=FY−Q3YTD
 *   · 이전 YTD가 없는 차분은 **금지**(잘못된 큰 수 방지)
 * - EPS 결측 시 NetIncome / WeightedAvgDilutedShares 보정
 * - Equity/Outstanding(instant)은 분기말 **floorEntry** 사용
 * - 가격: Stooq 일봉→주봉→월봉 폴백 + 실패 시 Yahoo Chart JSON 폴백
 */
public class StockMetricsFetcher {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public StockMetricsFetcher() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(25 * 1024 * 1024)) // 25MB
                .build();

        this.webClient = WebClient.builder()
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.USER_AGENT, "MyStockApp (contact: me@example.com)")
                .defaultHeader(HttpHeaders.REFERER, "https://www.sec.gov/")
                .defaultHeader(HttpHeaders.ACCEPT, "application/json, text/plain;q=0.8, */*;q=0.5")
                .build();
    }

    // ========================= 기본 로딩 =========================

    public Mono<List<Map.Entry<String, String>>> fetchTickerList() {
        String url = "https://www.sec.gov/files/company_tickers.json";
        return webClient.get().uri(url).retrieve().bodyToMono(String.class)
                .map(json -> {
                    try {
                        Map<String, Map<String, Object>> map =
                                mapper.readValue(json, new TypeReference<Map<String, Map<String, Object>>>() {});
                        List<Map.Entry<String, String>> list = new ArrayList<>();
                        for (Map<String, Object> record : map.values()) {
                            String ticker = String.valueOf(record.get("ticker"));
                            Number cikNum = (Number) record.get("cik_str");
                            String cik = String.format("%010d", cikNum.intValue());
                            list.add(new AbstractMap.SimpleEntry<>(ticker.toUpperCase(Locale.ROOT), cik));
                        }
                        return list;
                    } catch (Exception e) {
                        System.err.println("ticker json parse error: " + e.getMessage());
                        return Collections.emptyList();
                    }
                });
    }

    private Mono<String> getWithRetry(String url) {
        return webClient.get().uri(url).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp -> resp.createException().flatMap(Mono::error))
                .onStatus(HttpStatusCode::is5xxServerError, resp -> resp.createException().flatMap(Mono::error))
                .bodyToMono(String.class)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(600)).jitter(0.5)
                                .filter(e -> {
                                    if (e instanceof WebClientResponseException w) {
                                        int s = w.getStatusCode().value();
                                        return s == 429 || s == 403 || s == 503;
                                    }
                                    return false;
                                })
                )
                .delayElement(Duration.ofMillis(150));
    }

    private Mono<List<JsonNode>> fetchSecConceptSafe(String cik, String taxonomy, String tag) {
        String url = String.format("https://data.sec.gov/api/xbrl/companyconcept/CIK%s/%s/%s.json", cik, taxonomy, tag);
        return getWithRetry(url)
                .map(json -> {
                    try {
                        JsonNode root = mapper.readTree(json);
                        JsonNode units = root.path("units");
                        if (units.isMissingNode()) return Collections.<JsonNode>emptyList();
                        List<JsonNode> facts = new ArrayList<>();
                        units.fields().forEachRemaining(e -> {
                            JsonNode arr = e.getValue();
                            if (arr.isArray()) arr.forEach(facts::add);
                        });
                        return facts;
                    } catch (Exception e) {
                        System.err.println("concept parse error: " + e.getMessage());
                        return Collections.<JsonNode>emptyList();
                    }
                })
                .onErrorResume(e -> Mono.just(Collections.<JsonNode>emptyList()));
    }

    /** companyfacts 전체에서 원하는 태그를 첫 성공으로 가져오기 */
    private Mono<List<JsonNode>> fetchFromCompanyFacts(String cik, String namespace, List<String> tags) {
        String url = String.format("https://data.sec.gov/api/xbrl/companyfacts/CIK%s.json", cik);
        return getWithRetry(url)
                .map(json -> {
                    try {
                        JsonNode root = mapper.readTree(json);
                        JsonNode facts = root.path("facts").path(namespace);
                        if (facts.isMissingNode()) return Collections.<JsonNode>emptyList();
                        for (String tag : tags) {
                            JsonNode tagNode = facts.path(tag);
                            if (tagNode.isMissingNode()) continue;
                            JsonNode units = tagNode.path("units");
                            if (units.isMissingNode()) continue;
                            List<JsonNode> out = new ArrayList<>();
                            units.fields().forEachRemaining(e -> {
                                JsonNode arr = e.getValue();
                                if (arr.isArray()) arr.forEach(out::add);
                            });
                            if (!out.isEmpty()) return out;
                        }
                        return Collections.<JsonNode>emptyList();
                    } catch (Exception e) {
                        System.err.println("companyfacts parse error: " + e.getMessage());
                        return Collections.<JsonNode>emptyList();
                    }
                })
                .onErrorResume(e -> Mono.just(Collections.<JsonNode>emptyList()));
    }

    // ========================= 가격 소스 (Stooq → Yahoo 폴백) =========================

    private String normalizeTickerForStooq(String t) {
        String s = t.toLowerCase(Locale.ROOT);
        return s.replace('.', '-').replace('/', '-'); // BRK.B → brk-b
    }

    private String normalizeTickerForYahoo(String t) {
        return t.replace('/', '-').replace('.', '-'); // BRK.B → BRK-B
    }

    private Mono<String> getCsvWithRetry(String url) {
        return webClient.get().uri(url)
                .header(HttpHeaders.ACCEPT, "text/csv,*/*;q=0.1")
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(2, Duration.ofMillis(600)).jitter(0.5))
                .delayElement(Duration.ofMillis(150));
    }

    private Mono<NavigableMap<LocalDate, Double>> fetchStooqSeries(String ticker, String interval) {
        String sym = normalizeTickerForStooq(ticker);
        String url = String.format("https://stooq.com/q/d/l/?s=%s.us&i=%s", sym, interval); // i=d|w|m
        return getCsvWithRetry(url).map(csv -> {
            NavigableMap<LocalDate, Double> map = new TreeMap<>();
            try {
                if (csv.isBlank() || csv.startsWith("<")) return map; // HTML/오류
                String[] lines = csv.split("\r?\n");
                for (int i = 1; i < lines.length; i++) {
                    if (lines[i].isBlank()) continue;
                    String[] f = lines[i].split(",");
                    if (f.length < 5) continue;
                    LocalDate d = LocalDate.parse(f[0]);         // Date
                    double close = Double.parseDouble(f[4]);     // Close
                    map.put(d, close);
                }
            } catch (Exception e) {
                System.err.println("stooq parse error(" + interval + "): " + e.getMessage());
            }
            return map;
        }).onErrorResume(e -> {
            System.err.println("stooq fetch error(" + interval + "): " + e.getMessage());
            return Mono.just(new TreeMap<LocalDate, Double>());
        });
    }

    private Mono<NavigableMap<LocalDate, Double>> fetchStooqSeriesWithFallback(String ticker) {
        return fetchStooqSeries(ticker, "d")
                .flatMap(m -> m.isEmpty() ? fetchStooqSeries(ticker, "w") : Mono.just(m))
                .flatMap(m -> m.isEmpty() ? fetchStooqSeries(ticker, "m") : Mono.just(m))
                .doOnNext(m -> System.out.println(ticker + " stooq size=" + m.size()));
    }

    private Mono<NavigableMap<LocalDate, Double>> fetchYahooDailySeries(String ticker) {
        long now = Instant.now().getEpochSecond();
        long start = now - 10L * 365 * 24 * 3600; // 10년
        String symbol = normalizeTickerForYahoo(ticker);
        String url = String.format(
                "https://query1.finance.yahoo.com/v8/finance/chart/%s?period1=%d&period2=%d&interval=1d",
                symbol, start, now);

        return webClient.get().uri(url)
                .header(HttpHeaders.ACCEPT, "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .map(json -> {
                    NavigableMap<LocalDate, Double> map = new TreeMap<>();
                    try {
                        JsonNode root = mapper.readTree(json).path("chart").path("result");
                        if (!root.isArray() || root.isEmpty()) return map;
                        JsonNode res = root.get(0);
                        JsonNode ts = res.path("timestamp");
                        JsonNode quotes = res.path("indicators").path("quote");
                        if (!quotes.isArray() || quotes.isEmpty()) return map;
                        JsonNode closes = quotes.get(0).path("close");
                        if (!ts.isArray() || !closes.isArray()) return map;
                        for (int i = 0; i < ts.size() && i < closes.size(); i++) {
                            JsonNode t = ts.get(i);
                            JsonNode c = closes.get(i);
                            if (t.isNumber() && c.isNumber()) {
                                long epoch = t.asLong();
                                double close = c.asDouble(Double.NaN);
                                if (!Double.isNaN(close)) {
                                    LocalDate d = Instant.ofEpochSecond(epoch).atZone(ZoneId.of("America/New_York")).toLocalDate();
                                    map.put(d, close);
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("yahoo parse error: " + e.getMessage());
                    }
                    return map;
                })
                .onErrorResume(e -> {
                    System.err.println("yahoo fetch error: " + e.getMessage());
                    return Mono.just(new TreeMap<LocalDate, Double>());
                })
                .doOnNext(m -> System.out.println(ticker + " yahoo size=" + m.size()));
    }

    private Mono<NavigableMap<LocalDate, Double>> fetchPriceSeriesUnified(String ticker) {
        return fetchStooqSeriesWithFallback(ticker)
                .flatMap(m -> m.isEmpty() ? fetchYahooDailySeries(ticker) : Mono.just(m))
                .doOnNext(m -> System.out.println(ticker + " px size=" + m.size()));
    }

    // ========================= 분기화 유틸 =========================

    static final class QKey implements Comparable<QKey> {
        final int fy; // fiscal year
        final String q; // Q1..Q4
        QKey(int fy, String q) { this.fy = fy; this.q = q; }
        @Override public int compareTo(QKey o) { int c = Integer.compare(this.fy, o.fy); return (c!=0)?c:this.q.compareTo(o.q);}
        @Override public boolean equals(Object o){ if(!(o instanceof QKey)) return false; QKey k=(QKey)o; return fy==k.fy && Objects.equals(q,k.q);}
        @Override public int hashCode(){ return Objects.hash(fy, q);}
        @Override public String toString(){ return fy + "-" + q; }
    }

    static final class Item { final LocalDate end; final double val; boolean ytd = false; final String form; Item(LocalDate e, double v, boolean y, String f){
        String form1;
        end=e; val=v; y=ytd; this.ytd=y; form1 =f; form1 =f;
        form = form1;
    } }

    /** duration 계정(YTD 포함)을 분기 값으로 변환 */
    private Map<QKey, Map.Entry<LocalDate, Double>> quarterizeDurationFacts(List<JsonNode> facts) {
        Map<Integer, Map<String, Item>> nonYTD = new HashMap<>();
        Map<Integer, Map<String, Item>> YTD = new HashMap<>();
        Map<Integer, Item> FYTotal = new HashMap<>();

        for (JsonNode n : facts) {
            int fy = n.path("fy").asInt(0);
            String fp = n.path("fp").asText("");
            String frame = n.path("frame").asText("");
            String end = n.path("end").asText("");
            String form = n.path("form").asText("");
            double v = n.path("val").asDouble(Double.NaN);
            if (fy == 0 || end.isEmpty() || Double.isNaN(v)) continue;
            if (!("10-Q".equals(form) || "10-Q/A".equals(form) || "10-K".equals(form) || "10-K/A".equals(form))) continue;
            LocalDate d = LocalDate.parse(end);

            boolean isYTD = frame != null && frame.endsWith("YTD");
            String q;
            if (fp.startsWith("Q")) {
                q = fp; // Q1..Q3 (일부 Q4 존재)
            } else if ("FY".equals(fp)) {
                q = "Q4"; // FY는 Q4로 환산
            } else {
                continue;
            }

            Item item = new Item(d, v, isYTD, form);
            if ("FY".equals(fp) && !isYTD) {
                Item prev = FYTotal.get(fy);
                if (prev == null || d.isAfter(prev.end)) FYTotal.put(fy, item);
                continue;
            }

            Map<String, Item> target = isYTD ? YTD.computeIfAbsent(fy, k -> new HashMap<>())
                    : nonYTD.computeIfAbsent(fy, k -> new HashMap<>());
            Item prev = target.get(q);
            if (prev == null || d.isAfter(prev.end)) target.put(q, item);
        }

        Map<QKey, Map.Entry<LocalDate, Double>> out = new HashMap<>();
        for (Integer fy : unionKeys(nonYTD.keySet(), YTD.keySet())) {
            Item q1 = get(nonYTD, fy, "Q1"); Item y1 = get(YTD, fy, "Q1");
            Item q2 = get(nonYTD, fy, "Q2"); Item y2 = get(YTD, fy, "Q2");
            Item q3 = get(nonYTD, fy, "Q3"); Item y3 = get(YTD, fy, "Q3");
            Item q4 = get(nonYTD, fy, "Q4"); Item fyTot = FYTotal.get(fy);

            putIfPresent(out, new QKey(fy, "Q1"), prefer(q1, y1));
            putIfPresent(out, new QKey(fy, "Q2"), diff(y2, y1, q2));
            putIfPresent(out, new QKey(fy, "Q3"), diff(y3, y2, q3));
            Item q4Derived = null;
            if (fyTot != null && y3 != null) q4Derived = new Item(fyTot.end, fyTot.val - y3.val, false, fyTot.form);
            putIfPresent(out, new QKey(fy, "Q4"), (q4 != null) ? q4 : q4Derived);
        }
        return out;
    }

    private static <T> Set<T> unionKeys(Set<T> a, Set<T> b) { Set<T> s = new HashSet<>(a); s.addAll(b); return s; }
    private static Item get(Map<Integer, Map<String, Item>> m, Integer fy, String q) { Map<String, Item> inner = m.get(fy); return inner==null?null:inner.get(q); }
    private static void putIfPresent(Map<QKey, Map.Entry<LocalDate, Double>> out, QKey key, Item item) { if (item != null) out.put(key, Map.entry(item.end, item.val)); }
    private static Item prefer(Item quarter, Item ytd) { return quarter!=null ? quarter : ytd; }
    private static Item diff(Item ytdCurr, Item ytdPrev, Item quarter) { if (quarter!=null) return quarter; if (ytdCurr!=null && ytdPrev!=null) return new Item(ytdCurr.end, ytdCurr.val - ytdPrev.val, false, ytdCurr.form); return null; }

    /** instant(시점) 계정 → end 날짜별 NavigableMap */
    private NavigableMap<LocalDate, Double> toInstantNavSeries(List<JsonNode> facts) {
        NavigableMap<LocalDate, Double> m = new TreeMap<>();
        for (JsonNode n : facts) {
            String end = n.path("end").asText("");
            double v = n.path("val").asDouble(Double.NaN);
            String form = n.path("form").asText("");
            if (end.isEmpty() || Double.isNaN(v)) continue;
            if (!("10-Q".equals(form) || "10-Q/A".equals(form) || "10-K".equals(form) || "10-K/A".equals(form))) continue;
            LocalDate d = LocalDate.parse(end);
            m.put(d, v);
        }
        return m;
    }

    private double findPriceOnOrBefore(NavigableMap<LocalDate, Double> series, LocalDate date) {
        Map.Entry<LocalDate, Double> e = series.floorEntry(date);
        if (e != null) return e.getValue();
        LocalDate d = date.minusDays(1);
        for (int i = 0; i < 10; i++) { e = series.floorEntry(d); if (e != null) return e.getValue(); d = d.minusDays(1);}
        return Double.NaN;
    }

    // ========================= 시리즈 계산 =========================

    public Mono<List<QuarterMetrics>> computeMetricsSeries(String ticker, String cik) {
        Mono<List<JsonNode>> revenueMono  = fetchRevenueFacts(cik);
        Mono<List<JsonNode>> opIncMono    = fetchOperatingIncomeFacts(cik);
        Mono<List<JsonNode>> epsMono      = fetchEpsFacts(cik);
        Mono<List<JsonNode>> netMono      = fetchNetIncomeFacts(cik);
        Mono<List<JsonNode>> equityMono   = fetchEquityFacts(cik);
        Mono<List<JsonNode>> outSharesMono= fetchOutstandingSharesFacts(cik);
        Mono<List<JsonNode>> waDilutedMono= fetchWADilutedSharesFacts(cik);
        Mono<NavigableMap<LocalDate, Double>> priceSeriesMono = fetchPriceSeriesUnified(ticker);

        return Mono.zip(revenueMono, opIncMono, epsMono, netMono, equityMono, outSharesMono, waDilutedMono, priceSeriesMono)
                .map(tuple -> {
                    Map<QKey, Map.Entry<LocalDate, Double>> revM = quarterizeDurationFacts(tuple.getT1());
                    Map<QKey, Map.Entry<LocalDate, Double>> opM  = quarterizeDurationFacts(tuple.getT2());
                    Map<QKey, Map.Entry<LocalDate, Double>> epsM = quarterizeDurationFacts(tuple.getT3());
                    Map<QKey, Map.Entry<LocalDate, Double>> netM = quarterizeDurationFacts(tuple.getT4());

                    NavigableMap<LocalDate, Double> eqS = toInstantNavSeries(tuple.getT5());
                    NavigableMap<LocalDate, Double> shS = toInstantNavSeries(tuple.getT6());
                    Map<QKey, Map.Entry<LocalDate, Double>> waDilutedM = quarterizeDurationFacts(tuple.getT7());
                    NavigableMap<LocalDate, Double> pxS = tuple.getT8();

                    Set<QKey> all = new HashSet<>();
                    all.addAll(revM.keySet()); all.addAll(opM.keySet()); all.addAll(epsM.keySet());

                    LocalDate cutoff = LocalDate.now().minusYears(3).minusDays(7);
                    List<QKey> keys = all.stream()
                            .sorted((a, b) -> { int c = Integer.compare(b.fy, a.fy); if (c != 0) return c; return b.q.compareTo(a.q); })
                            .toList();

                    List<QuarterMetrics> out = new ArrayList<>();
                    for (QKey k : keys) {
                        Map.Entry<LocalDate, Double> revE = revM.get(k);
                        Map.Entry<LocalDate, Double> opE  = opM.get(k);
                        Map.Entry<LocalDate, Double> epsE = epsM.get(k);
                        Map.Entry<LocalDate, Double> netE = netM.get(k);
                        Map.Entry<LocalDate, Double> waE  = waDilutedM.get(k);

                        LocalDate end = null;
                        if (revE != null) end = revE.getKey();
                        else if (opE != null) end = opE.getKey();
                        else if (epsE != null) end = epsE.getKey();
                        if (end == null || end.isBefore(cutoff)) continue;

                        double rev = (revE != null) ? revE.getValue() : Double.NaN;
                        double op  = (opE  != null) ? opE.getValue()  : Double.NaN;
                        double eps = (epsE != null) ? epsE.getValue() : Double.NaN;

                        if (Double.isNaN(eps)) {
                            double net = (netE != null) ? netE.getValue() : Double.NaN;
                            double wa  = (waE  != null) ? waE.getValue()  : Double.NaN;
                            if (!Double.isNaN(net) && !Double.isNaN(wa) && wa != 0.0) eps = net / wa;
                        }

                        Map.Entry<LocalDate, Double> eqE = eqS.floorEntry(end);
                        Map.Entry<LocalDate, Double> shE = shS.floorEntry(end);
                        double eq = (eqE != null) ? eqE.getValue() : Double.NaN;
                        double sh = (shE != null) ? shE.getValue() : Double.NaN;

                        double price = findPriceOnOrBefore(pxS, end);
                        double per = (!Double.isNaN(price) && !Double.isNaN(eps) && eps != 0.0) ? price/eps : Double.NaN;
                        double pbr = (!Double.isNaN(price) && !Double.isNaN(eq) && !Double.isNaN(sh) && eq != 0.0 && sh != 0.0)
                                ? price / (eq / sh) : Double.NaN;

                        out.add(new QuarterMetrics(end, rev, op, eps, eq, sh, price, per, pbr));
                        if (out.size() >= 12) break; // 최근 12분기
                    }
                    return out;
                });
    }

    // ========================= 개념별 팩트 로딩(Fallback 조합) =========================

    private Mono<List<JsonNode>> fetchRevenueFacts(String cik) {
        List<String> tags = Arrays.asList(
                "SalesRevenueNet",
                "RevenueFromContractWithCustomerExcludingAssessedTax",
                "Revenues",
                "SalesRevenueGoodsNet",
                "RevenuesNetOfInterestExpense"
        );
        return Flux.concat(
                Flux.fromIterable(tags).concatMap(t -> fetchSecConceptSafe(cik, "us-gaap", t)),
                fetchFromCompanyFacts(cik, "us-gaap", tags),
                Mono.<List<JsonNode>>just(Collections.<JsonNode>emptyList())
        ).filter(list -> !list.isEmpty()).next().defaultIfEmpty(Collections.<JsonNode>emptyList());
    }

    private Mono<List<JsonNode>> fetchOperatingIncomeFacts(String cik) {
        List<String> tags = Arrays.asList(
                "OperatingIncomeLoss",
                "IncomeLossFromOperations"
        );
        return Flux.concat(
                Flux.fromIterable(tags).concatMap(t -> fetchSecConceptSafe(cik, "us-gaap", t)),
                fetchFromCompanyFacts(cik, "us-gaap", tags),
                Mono.<List<JsonNode>>just(Collections.<JsonNode>emptyList())
        ).filter(list -> !list.isEmpty()).next().defaultIfEmpty(Collections.<JsonNode>emptyList());
    }

    private Mono<List<JsonNode>> fetchEpsFacts(String cik) {
        List<String> tags = Arrays.asList(
                "EarningsPerShareDiluted",
                "EarningsPerShareBasicAndDiluted",
                "EarningsPerShareBasic"
        );
        return Flux.concat(
                Flux.fromIterable(tags).concatMap(t -> fetchSecConceptSafe(cik, "us-gaap", t)),
                fetchFromCompanyFacts(cik, "us-gaap", tags),
                Mono.<List<JsonNode>>just(Collections.<JsonNode>emptyList())
        ).filter(list -> !list.isEmpty()).next().defaultIfEmpty(Collections.<JsonNode>emptyList());
    }

    private Mono<List<JsonNode>> fetchNetIncomeFacts(String cik) {
        return Flux.concat(
                fetchSecConceptSafe(cik, "us-gaap", "NetIncomeLoss"),
                fetchFromCompanyFacts(cik, "us-gaap", Collections.singletonList("NetIncomeLoss")),
                Mono.<List<JsonNode>>just(Collections.<JsonNode>emptyList())
        ).filter(list -> !list.isEmpty()).next().defaultIfEmpty(Collections.<JsonNode>emptyList());
    }

    private Mono<List<JsonNode>> fetchEquityFacts(String cik) {
        List<String> tags = Arrays.asList(
                "StockholdersEquity",
                "StockholdersEquityIncludingPortionAttributableToNoncontrollingInterest"
        );
        return Flux.concat(
                Flux.fromIterable(tags).concatMap(t -> fetchSecConceptSafe(cik, "us-gaap", t)),
                fetchFromCompanyFacts(cik, "us-gaap", tags),
                Mono.<List<JsonNode>>just(Collections.<JsonNode>emptyList())
        ).filter(list -> !list.isEmpty()).next().defaultIfEmpty(Collections.<JsonNode>emptyList());
    }

    private Mono<List<JsonNode>> fetchOutstandingSharesFacts(String cik) {
        List<String> tags = Arrays.asList(
                "CommonStockSharesOutstanding",
                "EntityCommonStockSharesOutstanding",
                "CommonStockSharesIssued"
        );
        return Flux.concat(
                Flux.fromIterable(tags).concatMap(t -> fetchSecConceptSafe(cik, "us-gaap", t)),
                fetchSecConceptSafe(cik, "dei", "EntityCommonStockSharesOutstanding"),
                fetchFromCompanyFacts(cik, "us-gaap", tags),
                Mono.<List<JsonNode>>just(Collections.<JsonNode>emptyList())
        ).filter(list -> !list.isEmpty()).next().defaultIfEmpty(Collections.<JsonNode>emptyList());
    }

    private Mono<List<JsonNode>> fetchWADilutedSharesFacts(String cik) {
        List<String> tags = Arrays.asList(
                "WeightedAverageNumberOfDilutedSharesOutstanding",
                "WeightedAverageNumberOfSharesOutstandingDiluted"
        );
        return Flux.concat(
                Flux.fromIterable(tags).concatMap(t -> fetchSecConceptSafe(cik, "us-gaap", t)),
                fetchFromCompanyFacts(cik, "us-gaap", tags),
                Mono.<List<JsonNode>>just(Collections.<JsonNode>emptyList())
        ).filter(list -> !list.isEmpty()).next().defaultIfEmpty(Collections.<JsonNode>emptyList());
    }

    // ========================= 모델/포맷 & 실행 =========================

    private static String fmt(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return "N/A";
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
        nf.setMaximumFractionDigits(2); nf.setMinimumFractionDigits(0);
        return nf.format(v);
    }

    public static class QuarterMetrics {
        public final LocalDate end; public final double revenue, operatingIncome, eps, equity, shares, price, per, pbr;
        public QuarterMetrics(LocalDate end, double revenue, double operatingIncome, double eps, double equity, double shares, double price, double per, double pbr) {
            this.end=end; this.revenue=revenue; this.operatingIncome=operatingIncome; this.eps=eps; this.equity=equity; this.shares=shares; this.price=price; this.per=per; this.pbr=pbr;
        }
    }

    public static void main(String[] args) {
        StockMetricsFetcher fetcher = new StockMetricsFetcher();
        fetcher.fetchTickerList()
                .flatMapMany(list -> Flux.fromIterable(list.stream().limit(5).toList()))
                .flatMap(entry -> fetcher.computeMetricsSeries(entry.getKey(), entry.getValue())
                        .map(series -> Map.entry(entry.getKey(), series)))
                .collectList()
                .doOnNext(all -> {
                    for (Map.Entry<String, List<QuarterMetrics>> kv : all) {
                        System.out.println("==== " + kv.getKey() + " (최근 3년 분기) ====");
                        for (QuarterMetrics q : kv.getValue()) {
                            System.out.printf("%s => 매출: %s, 영업이익: %s, PER: %s, PBR: %s%n",
                                    q.end, fmt(q.revenue), fmt(q.operatingIncome), fmt(q.per), fmt(q.pbr));
                        }
                    }
                })
                .block();
    }
}
