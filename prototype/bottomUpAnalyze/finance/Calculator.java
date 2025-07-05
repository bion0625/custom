package bottomUpAnalyze.finance;

import dto.StockInfo;
import krx.CompanyCrawler;
import naverCrawler.QuarterOpCrawler;

import java.util.List;

public class Calculator {
    public static List<StockInfo> execute(FinanceType subject, PeriodType period) {
        return CompanyCrawler.getCompanyInfo().parallelStream()
                .filter(info -> {
                    List<Long> profits =
                            QuarterOpCrawler.fetchRecent3Op(info.getCode(), subject, period);

                    if (subject.equals(FinanceType.PBR) || subject.equals(FinanceType.PER)) {
                        // PBR과 PER은 낮을수록 좋음
                        return profits.size() > 3 &&
                                profits.get(0) < profits.get(1) &&
                                profits.get(1) < profits.get(2);
                    }

                    return profits.size() > 3 &&
                            profits.get(0) > profits.get(1) &&
                            profits.get(1) > profits.get(2);
                })
                .toList();
    }
}
