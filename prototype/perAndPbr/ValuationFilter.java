package perAndPbr;

import java.util.*;
import java.util.concurrent.*;

public class ValuationFilter {

    private static final double MAX_PER = 15.0;   // Graham rule
    private static final double MAX_PBR = 1.5;
    private static final int THREADS = 6;

    public static void main(String[] args) throws Exception {
        List<DealItem> picks = filterUndervalued();
        picks.forEach(System.out::println);
        System.out.printf("🎯 추천 종목: %d개%n", picks.size());
    }

    /** PER·PBR 저평가 리스트 */
    public static List<DealItem> filterUndervalued() throws Exception {

        List<StockInfo> targets = CompanyCrawler.getCompanyInfo();
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        List<Future<DealItem>> futures = new ArrayList<>();

        for (StockInfo s : targets) {
            futures.add(pool.submit(() -> judge(s)));
        }

        List<DealItem> result = new ArrayList<>();
        for (Future<DealItem> f : futures) {
            DealItem d = f.get();
            if (d != null) result.add(d);
        }
        pool.shutdown();
        return result;
    }

    private static DealItem judge(StockInfo s) {
        try {
            StockValuationCrawler.StockValuation v = StockValuationCrawler.fetch(s.getCode());
            if (v.per > 0 && v.pbr > 0 &&
                    v.per <= MAX_PER && v.pbr <= MAX_PBR) {
                DealItem d = new DealItem();
                d.setCode(s.getCode()); d.setName(s.getName());
                d.setPer(v.per);        d.setPbr(v.pbr);
                return d;
            }
        } catch (Exception ignore) {}
        return null;
    }
}
