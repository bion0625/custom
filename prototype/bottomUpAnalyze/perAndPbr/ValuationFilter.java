package bottomUpAnalyze.perAndPbr;

import dto.StockPBRAndPER;
import krx.CompanyCrawler;
import dto.StockInfo;
import naverCrawler.StockPBRAndPERCrawler;

import java.util.*;
import java.util.concurrent.*;

public class ValuationFilter {

    private static final double MAX_PER = 15.0;   // Graham rule
    private static final double MAX_PBR = 1.5;
    private static final int THREADS = 6;

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
            StockPBRAndPER v = StockPBRAndPERCrawler.fetchForPerAndPbr(s.getCode());
            System.out.print("+");
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
