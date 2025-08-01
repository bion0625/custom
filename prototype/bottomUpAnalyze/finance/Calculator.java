package bottomUpAnalyze.finance;

import dto.StockInfo;
import naverCrawler.QuarterOpCrawler;

import java.util.List;

public class Calculator {
    public static List<StockInfo> execute(FinanceType subject, PeriodType period, List<StockInfo> companyInfos) {
        return companyInfos.parallelStream()
                .filter(info -> {
                    List<Long> profits =
                            QuarterOpCrawler.fetchRecent3Op(info.getCode(), subject, period);

                    // 만약 장기적으로 축소되어야 한느 값을 FinanceType에 추가한다면 조건식에서 아래 분기로 활용
//                    if (subject.equals(FinanceType.PBR) || subject.equals(FinanceType.PER)) {
//                        // PBR과 PER은 낮을수록 좋음
//                        return profits.size() == 3 &&
//                                profits.get(0) < profits.get(1) &&
//                                profits.get(1) < profits.get(2);
//                    }

                    return profits.size() == 3 &&
                            profits.get(0) > profits.get(1) &&
                            profits.get(1) > profits.get(2);
                })
                .toList();
    }
}
