package com.project.stock.temp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.text.NumberFormat;
import java.time.temporal.ChronoUnit;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

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

    private NavigableMap<LocalDate, Double> byEndDate(Map<QKey, Map.Entry<LocalDate, Double>> m) {
        NavigableMap<LocalDate, Double> r = new TreeMap<>();
        for (var e : m.entrySet()) {
            LocalDate d = e.getValue().getKey();
            double v = e.getValue().getValue();
            // 같은 end 날짜에 여러 태그가 들어오면 가장 최근(그대로)로 덮어씀
            r.put(d, v);
        }
        return r;
    }

    /** end 날짜가 정확히 일치하지 않아도 ±10일 내에서 가장 가까운 값을 사용 */
    private static double getNear(NavigableMap<LocalDate, Double> s, LocalDate date, int windowDays) {
        if (s == null || s.isEmpty()) return Double.NaN;
        Map.Entry<LocalDate, Double> f = s.floorEntry(date);
        Map.Entry<LocalDate, Double> c = s.ceilingEntry(date);

        double best = Double.NaN;
        long bestDiff = Long.MAX_VALUE;

        if (f != null) {
            long diff = Math.abs(DAYS.between(f.getKey(), date));
            if (diff <= windowDays && diff < bestDiff) { best = f.getValue(); bestDiff = diff; }
        }
        if (c != null) {
            long diff = Math.abs(DAYS.between(c.getKey(), date));
            if (diff <= windowDays && diff < bestDiff) { best = c.getValue(); bestDiff = diff; }
        }
        return best;
    }


    // ========================= 기본 로딩 =========================

    public Mono<List<Map.Entry<String, String>>> fetchTickerList() {
        String url = "https://www.sec.gov/files/company_tickers.json";
        return webClient.get().uri(url).retrieve().bodyToMono(String.class)
                .map(json -> {
                    try {
                        Map<String, Map<String, Object>> map =
                                mapper.readValue(json, new TypeReference<>() {
                                });
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
        return webClient.get().uri(url)
                .exchangeToMono(resp -> {
                    int s = resp.statusCode().value();
                    if (s == 404) return Mono.just("");             // 404는 빈 응답으로 처리
                    if (s >= 200 && s < 300) return resp.bodyToMono(String.class);
                    return resp.createException().flatMap(Mono::error);
                })
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(600)).jitter(0.5)
                                .filter(e -> e instanceof WebClientResponseException w &&
                                        (w.getStatusCode().value()==429 || w.getStatusCode().value()==403 || w.getStatusCode().value()==503))
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
            return Mono.just(new TreeMap<>());
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
                    return Mono.just(new TreeMap<>());
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

    static final class Item {
        final LocalDate end; final double val; final boolean ytd; final String form;
        Item(LocalDate e, double v, boolean isYTD, String f){
            this.end = e; this.val = v; this.ytd = isYTD; this.form = f;
        }
    }

    private static Item diffWithQuarterFallback(Item yCurr, Item yPrev, Item qCurr, Item... priorQuarters) {
        if (qCurr != null) return qCurr;
        if (yCurr != null && yPrev != null) {
            return new Item(yCurr.end, yCurr.val - yPrev.val, false, yCurr.form);
        }
        if (yCurr != null && priorQuarters != null && priorQuarters.length > 0) {
            double sum = 0; boolean has = false;
            for (Item qi : priorQuarters) {
                if (qi != null) { sum += qi.val; has = true; }
            }
            if (has) return new Item(yCurr.end, yCurr.val - sum, false, yCurr.form);
        }
        return null;
    }

    private static boolean isYTDLike(String fp, String frame, String start, String end) {
        if (frame != null && frame.toUpperCase(Locale.ROOT).contains("YTD")) return true;
        if (fp != null && fp.startsWith("Q") && start != null && !start.isBlank() && end != null && !end.isBlank()) {
            try {
                LocalDate s = LocalDate.parse(start);
                LocalDate e = LocalDate.parse(end);
                long days = ChronoUnit.DAYS.between(s, e);
                // 분기(≈90일)보다 충분히 길면 YTD로 본다 (여유 있게 115일+)
                return days > 115;
            } catch (Exception ignore) {}
        }
        return false;
    }


    /** duration 계정(YTD 포함)을 분기 값으로 변환 */
    private Map<QKey, Map.Entry<LocalDate, Double>> quarterizeDurationFacts(List<JsonNode> facts) {
        Map<Integer, Map<String, Item>> nonYTD = new HashMap<>();
        Map<Integer, Map<String, Item>> YTD = new HashMap<>();
        Map<Integer, Item> FYTotal = new HashMap<>();

        for (JsonNode n : facts) {
            int fy = n.path("fy").asInt(0);
            String fp = n.path("fp").asText("");
            String frame = n.path("frame").asText("");
            String start = n.path("start").asText("");
            String end = n.path("end").asText("");
            String form = n.path("form").asText("");
            double v = n.path("val").asDouble(Double.NaN);
            if (fy == 0 || end.isEmpty() || Double.isNaN(v)) continue;
            if (!isAllowedForm(form)) continue;
            LocalDate d = LocalDate.parse(end);

            boolean isYTD = isYTDLike(fp, frame, start, end);

            String q;
            if (fp.startsWith("Q")) {
                q = fp; // Q1..Q3 (가끔 Q4도 존재)
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

            Item q1Out = prefer(q1, y1);
            Item q2Out = diffWithQuarterFallback(y2, y1, q2, q1Out);
            Item q3Out = diffWithQuarterFallback(y3, y2, q3, q1Out, q2Out);

            putIfPresent(out, new QKey(fy, "Q1"), q1Out);
            putIfPresent(out, new QKey(fy, "Q2"), q2Out);
            putIfPresent(out, new QKey(fy, "Q3"), q3Out);

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
            if (!isAllowedForm(form)) continue;
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

                    // ✅ end-날짜 기반 시리즈
                    NavigableMap<LocalDate, Double> revD = byEndDate(revM);
                    NavigableMap<LocalDate, Double> opD  = byEndDate(opM);
                    NavigableMap<LocalDate, Double> epsD = byEndDate(epsM);
                    NavigableMap<LocalDate, Double> waD  = byEndDate(waDilutedM);

                    // 최근 3년 내 end 날짜 집합(내림차순)
                    LocalDate cutoff = LocalDate.now().minusYears(3).minusDays(7);
                    NavigableSet<LocalDate> allEnds = new TreeSet<>(Comparator.reverseOrder());
                    allEnds.addAll(revD.keySet());
                    allEnds.addAll(opD.keySet());
                    allEnds.addAll(epsD.keySet());
                    allEnds.removeIf(d -> d.isBefore(cutoff));

                    List<QuarterMetrics> out = new ArrayList<>();
                    int windowDays = 10; // end 날짜 허용 오차

                    for (LocalDate end : allEnds) {
                        double rev = getNear(revD, end, windowDays);
                        double op  = getNear(opD,  end, windowDays);
                        double eps = getNear(epsD, end, windowDays);

                        // EPS 보정(NetIncome / WADiluted)
                        if (Double.isNaN(eps)) {
                            double net = getNear(byEndDate(netM), end, windowDays);
                            double wa  = getNear(waD, end, windowDays);
                            if (!Double.isNaN(net) && !Double.isNaN(wa) && wa != 0.0) eps = net / wa;
                        }

                        // instant 계정은 분기말 이전값 floor
                        Map.Entry<LocalDate, Double> eqE = eqS.floorEntry(end);
                        Map.Entry<LocalDate, Double> shE = shS.floorEntry(end);
                        double eq = (eqE != null) ? eqE.getValue() : Double.NaN;
                        double sh = (shE != null) ? shE.getValue() : Double.NaN;

                        double price = findPriceOnOrBefore(pxS, end);
                        double per = (!Double.isNaN(price) && !Double.isNaN(eps) && eps != 0.0) ? price/eps : Double.NaN;
                        double pbr = (!Double.isNaN(price) && !Double.isNaN(eq) && !Double.isNaN(sh) && eq != 0.0 && sh != 0.0)
                                ? price / (eq / sh) : Double.NaN;

                        // 최소 하나 이상 값이 있어야 수록
                        if (!Double.isNaN(rev) || !Double.isNaN(op) || !Double.isNaN(eps)) {
                            out.add(new QuarterMetrics(end, rev, op, eps, eq, sh, price, per, pbr));
                            if (out.size() >= 12) break; // 최근 12분기
                        }
                    }
                    return out;
                });
    }

    // ========================= 개념별 팩트 로딩(Fallback 조합) =========================

    private Mono<List<JsonNode>> fetchRevenueFacts(String cik) {
        List<String> gaap = Arrays.asList(
                "SalesRevenueNet",
                "RevenueFromContractWithCustomerExcludingAssessedTax",
                "Revenues",
                "SalesRevenueGoodsNet",
                "RevenuesNetOfInterestExpense",
                "RevenueFromContractWithCustomerIncludingAssessedTax",
                "SalesRevenueServicesNet"
        );

        List<String> ifrs = Arrays.asList(
                "Revenue",
                "RevenueFromContractsWithCustomers"
        );

        return Flux.concat(
                        // companyconcept: us-gaap 전 태그
                        Flux.fromIterable(gaap).concatMap(t -> fetchSecConceptSafe(cik, "us-gaap", t)),
                        // companyconcept: ifrs-full 전 태그
                        Flux.fromIterable(ifrs).concatMap(t -> fetchSecConceptSafe(cik, "ifrs-full", t)),
                        // companyfacts: us-gaap 전 태그 합집합
                        fetchFromCompanyFactsUnion(cik, "us-gaap", gaap),
                        // companyfacts: ifrs-full 전 태그 합집합
                        fetchFromCompanyFactsUnion(cik, "ifrs-full", ifrs)
                )
                .collectList()
                .map(lists -> {
                    List<JsonNode> all = new ArrayList<>();
                    for (List<JsonNode> l : lists) if (l != null) all.addAll(l);
                    return all;
                })
                .defaultIfEmpty(Collections.<JsonNode>emptyList());
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

    private static boolean isAllowedForm(String form) {
        if (form == null || form.isBlank()) return true; // form 누락 레코드 허용
        return switch (form) {
            case "10-Q","10-Q/A","10-K","10-K/A",
                 "20-F","20-F/A","40-F","40-F/A",
                 "6-K","6-K/A" -> true;
            default -> false;
        };
    }

    private void debugFacts(String label, String ticker, List<JsonNode> facts) {
        if (facts == null || facts.isEmpty()) {
            System.err.println(label + "[" + ticker + "] facts EMPTY");
            return;
        }
        List<JsonNode> sorted = new ArrayList<>(facts);
        sorted.sort((a, b) -> {
            String ea = a.path("end").asText("");
            String eb = b.path("end").asText("");
            if (ea.isEmpty() || eb.isEmpty()) return 0;
            return LocalDate.parse(eb).compareTo(LocalDate.parse(ea));
        });

        int q1=0,q2=0,q3=0,q4=0, fyCnt=0, ytdCnt=0, nonYtdCnt=0;
        for (JsonNode n : facts) {
            String fp = n.path("fp").asText("");
            String frame = n.path("frame").asText("");
            String start = n.path("start").asText("");
            String end = n.path("end").asText("");
            boolean ytd = isYTDLike(fp, frame, start, end); // ✅ 변경
            if ("FY".equals(fp)) fyCnt++;
            else if (fp.startsWith("Q")) {
                switch (fp) { case "Q1" -> q1++; case "Q2" -> q2++; case "Q3" -> q3++; case "Q4" -> q4++; }
            }
            if (ytd) ytdCnt++; else nonYtdCnt++;
        }

        System.err.printf("%s[%s] total=%d, Q1=%d Q2=%d Q3=%d Q4=%d, FY=%d, YTD=%d nonYTD=%d%n",
                label, ticker, facts.size(), q1, q2, q3, q4, fyCnt, ytdCnt, nonYtdCnt);

        int show = Math.min(3, sorted.size());
        for (int i = 0; i < show; i++) {
            JsonNode n = sorted.get(i);
            System.err.println("  #" + (i+1) +
                    " form=" + n.path("form").asText("") +
                    ", fy=" + n.path("fy").asInt() +
                    ", fp=" + n.path("fp").asText("") +
                    ", frame=" + n.path("frame").asText("") +
                    ", start=" + n.path("start").asText("") +
                    ", end=" + n.path("end").asText("") +
                    ", val=" + n.path("val").asText(""));
        }
    }

    private Mono<List<JsonNode>> fetchFromCompanyFactsUnion(String cik, String namespace, List<String> tags) {
        String url = String.format("https://data.sec.gov/api/xbrl/companyfacts/CIK%s.json", cik);
        return getWithRetry(url)
                .map(json -> {
                    try {
                        JsonNode root = mapper.readTree(json);
                        JsonNode facts = root.path("facts").path(namespace);
                        if (facts.isMissingNode()) return Collections.<JsonNode>emptyList();

                        List<JsonNode> out = new ArrayList<>();
                        for (String tag : tags) {
                            JsonNode tagNode = facts.path(tag);
                            if (tagNode.isMissingNode()) continue;
                            JsonNode units = tagNode.path("units");
                            if (units.isMissingNode()) continue;
                            units.fields().forEachRemaining(e -> {
                                JsonNode arr = e.getValue();
                                if (arr.isArray()) arr.forEach(out::add);
                            });
                        }
                        return out;
                    } catch (Exception e) {
                        System.err.println("companyfacts(union) parse error: " + e.getMessage());
                        return Collections.<JsonNode>emptyList();
                    }
                })
                .onErrorResume(e -> Mono.just(Collections.<JsonNode>emptyList()));
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
