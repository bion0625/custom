package com.project.stock.temp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;
import java.text.NumberFormat;
import java.util.stream.Collectors;

/**
 * WebFlux 기반 원시 구현:
 *  - SEC company_tickers.json 으로 Ticker->CIK 로딩 (User-Agent 포함)
 *  - SEC XBRL companyconcept API에서 분기별 지표 수집 (404/에러 안전 처리, 분기 필터)
 *  - Stooq CSV로 종가 조회 (API Key 불필요)
 *  - PER = price / EPS(diluted), PBR = price / (Equity / Shares)
 */
public class StockMetricsFetcher {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public StockMetricsFetcher() {
        // 큰 JSON(company_tickers.json) 수용을 위해 in-memory 버퍼 확장 (기본 256KB → 5MB)
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                .build();

        this.webClient = WebClient.builder()
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.USER_AGENT, "MyStockApp (contact: me@example.com)")
                .build();
    }

    /**
     * SEC의 company_tickers.json을 파싱해 (ticker, cik) 리스트 반환
     */
    public Mono<List<Map.Entry<String, String>>> fetchTickerList() {
        String url = "https://www.sec.gov/files/company_tickers.json";
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
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

    /**
     * companyconcept 안전 호출: 404 등 오류 시 빈 리스트로 대체
     */
    private Mono<List<JsonNode>> fetchSecConceptSafe(String cik, String taxonomy, String tag) {
        String url = String.format("https://data.sec.gov/api/xbrl/companyconcept/CIK%s/%s/%s.json", cik, taxonomy, tag);
        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp -> {
                    if (resp.statusCode().value() == 404) return Mono.empty();
                    return resp.createException().flatMap(Mono::error);
                })
                .bodyToMono(String.class)
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
                .onErrorResume(e -> {
                    if (e instanceof WebClientResponseException.NotFound) return Mono.<List<JsonNode>>just(Collections.<JsonNode>emptyList());
                    System.err.println("concept fetch error: " + e.getMessage());
                    return Mono.<List<JsonNode>>just(Collections.<JsonNode>emptyList());
                });
    }

    /** Stooq에서 종가 조회 (CSV) */
    private Mono<Double> fetchStooqPrice(String ticker) {
        String url = String.format("https://stooq.com/q/l/?s=%s.us&f=sd2t2ohlcv&h&e=csv", ticker.toLowerCase(Locale.ROOT));
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(csv -> {
                    try {
                        String[] lines = csv.split("\\r?\\n");
                        if (lines.length < 2) return Double.NaN;
                        String[] fields = lines[1].split(",");
                        if (fields.length < 7) return Double.NaN;
                        return Double.parseDouble(fields[6]); // Close
                    } catch (Exception e) {
                        System.err.println("stooq parse error: " + e.getMessage());
                        return Double.NaN;
                    }
                })
                .onErrorResume(e -> {
                    System.err.println("stooq fetch error: " + e.getMessage());
                    return Mono.just(Double.NaN);
                });
    }

    /** Stooq 일별 가격 시계열 (최근 3년 PER/PBR 계산용) */
    private Mono<java.util.NavigableMap<LocalDate, Double>> fetchStooqDailySeries(String ticker) {
        String url = String.format("https://stooq.com/q/d/l/?s=%s.us&i=d", ticker.toLowerCase(Locale.ROOT));
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(csv -> {
                    java.util.NavigableMap<LocalDate, Double> map = new java.util.TreeMap<>();
                    try {
                        String[] lines = csv.split("\\r?\\n");
                        for (int i = 1; i < lines.length; i++) { // 0: header
                            if (lines[i].isBlank()) continue;
                            String[] f = lines[i].split(",");
                            if (f.length < 5) continue;
                            LocalDate d = LocalDate.parse(f[0]); // yyyy-MM-dd
                            double close = Double.parseDouble(f[4]);
                            map.put(d, close);
                        }
                    } catch (Exception e) {
                        System.err.println("stooq daily parse error: " + e.getMessage());
                    }
                    return map;
                })
                .onErrorResume(e -> {
                    System.err.println("stooq daily fetch error: " + e.getMessage());
                    return Mono.just(new java.util.TreeMap<LocalDate, Double>());
                });
    }

    /** 주식수 태그 fallback: us-gaap/CommonStockSharesOutstanding → dei/EntityCommonStockSharesOutstanding */
    private Mono<List<JsonNode>> fetchSharesFacts(String cik) {
        return Flux.concat(
                        fetchSecConceptSafe(cik, "us-gaap", "CommonStockSharesOutstanding"),
                        fetchSecConceptSafe(cik, "dei",     "EntityCommonStockSharesOutstanding"),
                        Mono.<List<JsonNode>>just(Collections.<JsonNode>emptyList())
                )
                .filter(list -> !list.isEmpty())
                .next()
                .defaultIfEmpty(Collections.<JsonNode>emptyList());
    }

    /**
     * 매출 태그 fallback: 회사마다 Revenues 대신 다른 태그를 쓰는 경우가 많음
     * 우선순위: SalesRevenueNet → RevenueFromContractWithCustomerExcludingAssessedTax → Revenues → SalesRevenueGoodsNet
     */
    private Mono<List<JsonNode>> fetchRevenueFacts(String cik) {
        return Flux.concat(
                        fetchSecConceptSafe(cik, "us-gaap", "SalesRevenueNet"),
                        fetchSecConceptSafe(cik, "us-gaap", "RevenueFromContractWithCustomerExcludingAssessedTax"),
                        fetchSecConceptSafe(cik, "us-gaap", "Revenues"),
                        fetchSecConceptSafe(cik, "us-gaap", "SalesRevenueGoodsNet"),
                        Mono.<List<JsonNode>>just(Collections.<JsonNode>emptyList())
                )
                .filter(list -> !list.isEmpty())
                .next()
                .defaultIfEmpty(Collections.<JsonNode>emptyList());
    }

    /** 분기 보고(10-Q, Q1~Q3)만 필터 */
    private List<JsonNode> onlyQuarterly(List<JsonNode> facts) {
        return facts.stream()
                .filter(n -> {
                    String fp = n.path("fp").asText("");
                    String form = n.path("form").asText("");
                    return fp.startsWith("Q") || "10-Q".equals(form) || "10-Q/A".equals(form);
                })
                .collect(Collectors.toList());
    }

    public Mono<StockMetrics> computeMetrics(String ticker, String cik) {
        Mono<List<JsonNode>> revenueMono  = fetchRevenueFacts(cik);
        Mono<List<JsonNode>> opIncMono    = fetchSecConceptSafe(cik, "us-gaap", "OperatingIncomeLoss");
        Mono<List<JsonNode>> epsMono      = fetchSecConceptSafe(cik, "us-gaap", "EarningsPerShareDiluted");
        Mono<List<JsonNode>> equityMono   = fetchSecConceptSafe(cik, "us-gaap", "StockholdersEquity");
        Mono<List<JsonNode>> sharesMono   = fetchSharesFacts(cik);
        Mono<Double> priceMono = fetchStooqPrice(ticker);

        return Mono.zip(revenueMono, opIncMono, epsMono, equityMono, sharesMono, priceMono)
                .map(tuple -> {
                    List<JsonNode> revQ = onlyQuarterly(tuple.getT1());
                    List<JsonNode> opQ  = onlyQuarterly(tuple.getT2());
                    List<JsonNode> epsQ = onlyQuarterly(tuple.getT3());
                    List<JsonNode> eqQ  = onlyQuarterly(tuple.getT4());
                    List<JsonNode> shQ  = onlyQuarterly(tuple.getT5());

                    Comparator<JsonNode> byDate = Comparator.comparing(
                            (JsonNode n) -> LocalDate.parse(n.path("end").asText())
                    ).reversed();

                    JsonNode latestRev    = revQ.stream().sorted(byDate).findFirst().orElse(null);
                    JsonNode latestOp     = opQ.stream().sorted(byDate).findFirst().orElse(null);
                    JsonNode latestEps    = epsQ.stream().sorted(byDate).findFirst().orElse(null);
                    // Equity는 분기에 없을 수 있어 연간까지 fallback
                    JsonNode latestEquity = eqQ.stream().sorted(byDate).findFirst()
                            .orElse(tuple.getT4().stream().sorted(byDate).findFirst().orElse(null));
                    JsonNode latestShares = shQ.stream().sorted(byDate).findFirst()
                            .orElse(tuple.getT5().stream().sorted(byDate).findFirst().orElse(null));

                    double revenue  = (latestRev    != null) ? latestRev.path("val").asDouble(Double.NaN)    : Double.NaN;
                    double opIncome = (latestOp     != null) ? latestOp.path("val").asDouble(Double.NaN)     : Double.NaN;
                    double eps      = (latestEps    != null) ? latestEps.path("val").asDouble(Double.NaN)    : Double.NaN;
                    double equity   = (latestEquity != null) ? latestEquity.path("val").asDouble(Double.NaN) : Double.NaN;
                    double shares   = (latestShares != null) ? latestShares.path("val").asDouble(Double.NaN) : Double.NaN;

                    double price = tuple.getT6();
                    double per = (!Double.isNaN(price) && !Double.isNaN(eps) && eps != 0.0) ? price / eps : Double.NaN;
                    double pbr = (!Double.isNaN(price) && !Double.isNaN(equity) && !Double.isNaN(shares) && equity != 0.0 && shares != 0.0)
                            ? price / (equity / shares) : Double.NaN;

                    return new StockMetrics(ticker, revenue, opIncome, per, pbr);
                });
    }
    // 숫자 포맷터 (NaN/N/A 처리 + 한국 로케일 천단위 구분)
    private static String fmt(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return "N/A";
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(0);
        return nf.format(v);
    }

    /** 분기 팩트들을 (end -> 값) 맵으로 정제 */
    private java.util.Map<LocalDate, Double> toQuarterSeries(java.util.List<JsonNode> facts) {
        java.util.Map<LocalDate, Double> m = new java.util.HashMap<>();
        for (JsonNode n : facts) {
            String fp = n.path("fp").asText("");
            String form = n.path("form").asText("");
            if (!(fp.startsWith("Q") || "10-Q".equals(form) || "10-Q/A".equals(form))) continue;
            String end = n.path("end").asText("");
            if (end.isEmpty()) continue;
            LocalDate date = LocalDate.parse(end);
            double v = n.path("val").asDouble(Double.NaN);
            if (Double.isNaN(v)) continue;
            // 동일 end가 여러 개면 마지막 값으로 덮어쓰기 (간단화)
            m.put(date, v);
        }
        return m;
    }

    /** 해당 분기 종료일 기준, 그 날 또는 직전 거래일 종가 */
    private double findPriceOnOrBefore(java.util.NavigableMap<LocalDate, Double> series, LocalDate date) {
        java.util.Map.Entry<LocalDate, Double> e = series.floorEntry(date);
        if (e != null) return e.getValue();
        // 여유 버퍼로 며칠 더 뒤로 탐색
        LocalDate d = date.minusDays(1);
        for (int i = 0; i < 10; i++) {
            e = series.floorEntry(d);
            if (e != null) return e.getValue();
            d = d.minusDays(1);
        }
        return Double.NaN;
    }

    /** 최근 3년(최대 12분기) 분기별 지표 계산 */
    public Mono<java.util.List<QuarterMetrics>> computeMetricsSeries(String ticker, String cik) {
        Mono<java.util.List<JsonNode>> revenueMono  = fetchRevenueFacts(cik);
        Mono<java.util.List<JsonNode>> opIncMono    = fetchSecConceptSafe(cik, "us-gaap", "OperatingIncomeLoss");
        Mono<java.util.List<JsonNode>> epsMono      = fetchSecConceptSafe(cik, "us-gaap", "EarningsPerShareDiluted");
        Mono<java.util.List<JsonNode>> equityMono   = fetchSecConceptSafe(cik, "us-gaap", "StockholdersEquity");
        Mono<java.util.List<JsonNode>> sharesMono   = fetchSharesFacts(cik);
        Mono<java.util.NavigableMap<LocalDate, Double>> priceSeriesMono = fetchStooqDailySeries(ticker);

        return Mono.zip(revenueMono, opIncMono, epsMono, equityMono, sharesMono, priceSeriesMono)
                .map(tuple -> {
                    java.util.Map<LocalDate, Double> revS = toQuarterSeries(tuple.getT1());
                    java.util.Map<LocalDate, Double> opS  = toQuarterSeries(tuple.getT2());
                    java.util.Map<LocalDate, Double> epsS = toQuarterSeries(tuple.getT3());
                    java.util.Map<LocalDate, Double> eqS  = toQuarterSeries(tuple.getT4());
                    java.util.Map<LocalDate, Double> shS  = toQuarterSeries(tuple.getT5());
                    java.util.NavigableMap<LocalDate, Double> pxS = tuple.getT6();

                    java.util.Set<LocalDate> all = new java.util.HashSet<>();
                    all.addAll(revS.keySet()); all.addAll(opS.keySet()); all.addAll(epsS.keySet());
                    LocalDate cutoff = LocalDate.now().minusYears(3).minusDays(7);
                    java.util.List<LocalDate> dates = all.stream()
                            .filter(d -> d.isAfter(cutoff))
                            .sorted(java.util.Comparator.reverseOrder())
                            .limit(12)
                            .toList();

                    java.util.List<QuarterMetrics> out = new java.util.ArrayList<>();
                    for (LocalDate d : dates) {
                        double rev = revS.getOrDefault(d, Double.NaN);
                        double op  = opS.getOrDefault(d, Double.NaN);
                        double eps = epsS.getOrDefault(d, Double.NaN);
                        double eq  = eqS.getOrDefault(d, Double.NaN);
                        double sh  = shS.getOrDefault(d, Double.NaN);
                        double price = findPriceOnOrBefore(pxS, d);
                        double per = (!Double.isNaN(price) && !Double.isNaN(eps) && eps != 0.0) ? price/eps : Double.NaN;
                        double pbr = (!Double.isNaN(price) && !Double.isNaN(eq) && !Double.isNaN(sh) && eq != 0.0 && sh != 0.0)
                                ? price / (eq / sh) : Double.NaN;
                        out.add(new QuarterMetrics(d, rev, op, eps, eq, sh, price, per, pbr));
                    }
                    return out;
                });
    }

    public static class QuarterMetrics {
        public final LocalDate end;
        public final double revenue, operatingIncome, eps, equity, shares, price, per, pbr;
        public QuarterMetrics(LocalDate end, double revenue, double operatingIncome, double eps,
                              double equity, double shares, double price, double per, double pbr) {
            this.end=end; this.revenue=revenue; this.operatingIncome=operatingIncome; this.eps=eps;
            this.equity=equity; this.shares=shares; this.price=price; this.per=per; this.pbr=pbr;
        }
    }

    public static void main(String[] args) {
        StockMetricsFetcher fetcher = new StockMetricsFetcher();
        fetcher.fetchTickerList()
                // todo 추후 필요시 limit 5 삭제하는 식으로 구현
                .flatMapMany(list -> Flux.fromIterable(list.stream().limit(5).toList()))
                .flatMap(entry -> fetcher.computeMetricsSeries(entry.getKey(), entry.getValue())
                        .map(series -> java.util.Map.entry(entry.getKey(), series)))
                .collectList()
                .doOnNext(all -> {
                    for (var kv : all) {
                        String t = kv.getKey();
                        System.out.println("==== " + t + " (최근 3년 분기) ====");
                        for (QuarterMetrics q : kv.getValue()) {
                            System.out.printf("%s => 매출: %s, 영업이익: %s, PER: %s, PBR: %s%n",
                                    q.end, fmt(q.revenue), fmt(q.operatingIncome), fmt(q.per), fmt(q.pbr));
                        }
                    }
                })
                .block();
    }

    public static class StockMetrics {
        private final String ticker;
        private final double revenue;
        private final double operatingIncome;
        private final double peRatio;
        private final double pbRatio;

        public StockMetrics(String ticker, double revenue, double operatingIncome, double peRatio, double pbRatio) {
            this.ticker = ticker;
            this.revenue = revenue;
            this.operatingIncome = operatingIncome;
            this.peRatio = peRatio;
            this.pbRatio = pbRatio;
        }

        @Override
        public String toString() {
            return String.format(Locale.KOREA,
                    "%s => 매출: %s, 영업이익: %s, PER: %s, PBR: %s",
                    ticker,
                    StockMetricsFetcher.fmt(revenue),
                    StockMetricsFetcher.fmt(operatingIncome),
                    StockMetricsFetcher.fmt(peRatio),
                    StockMetricsFetcher.fmt(pbRatio));
        }
    }
}
