package bottomUpAnalyze.perAndPbr;

import dto.StockInfo;
import naverCrawler.CurrentOpCrawler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ValuationFilter {

    private static final double MAX_PER = 15.0;   // Graham rule
    private static final double MAX_PBR = 1.5;
    private static final int THREADS = 6;

    /** PER·PBR 저평가 리스트 */
    public static List<DealItem> filterUndervalued(List<StockInfo> targets) throws Exception {
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
            double per = CurrentOpCrawler.fetchForOne(s.getCode(), "PER");
            double pbr = CurrentOpCrawler.fetchForOne(s.getCode(), "PBR");
            if (per > 0 && pbr > 0 &&
                    per <= MAX_PER && pbr <= MAX_PBR) {
                DealItem d = new DealItem();
                d.setCode(s.getCode()); d.setName(s.getName());
                d.setPer(per);        d.setPbr(pbr);
                return d;
            }
        } catch (Exception ignore) {}
        return null;
    }

    public static void main(String[] args) throws Exception {
        StockInfo target = new StockInfo();
        target.setCode("123410");
        target.setName("코리아에프티");
        List<DealItem> dealItems = filterUndervalued(List.of(target));
        System.out.println(dealItems);
    }
}
