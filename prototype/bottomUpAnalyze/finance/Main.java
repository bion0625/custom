package bottomUpAnalyze.finance;

import dto.StockInfo;
import krx.CompanyCrawler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/******************************************************************************
 * 영업이익
 * 매출액
 * 순이익률
 * 당기순이익
 * 위 리스트가 3분기 혹은 3년 동안 꾸준히 올랐는지 (PER과 PBR의 경우는 내렸는지) 확인된 종목 추출
 ******************************************************************************/

public class Main {
    public static void main(String[] args) {
        FinanceType[] financeTypes = FinanceType.values();
        PeriodType[] periodTypes = PeriodType.values();

        int all = financeTypes.length * periodTypes.length;
        AtomicInteger counter = new AtomicInteger();

        List<StockInfo> companyInfos = CompanyCrawler.getCompanyInfo();
        Arrays.stream(financeTypes)
                .forEach(subject -> Arrays.stream(periodTypes)
                        .forEach(period -> {
                            List<StockInfo> infos = Calculator.execute(subject, period, companyInfos);
                            String periodText = period.equals(PeriodType.YEAR) ? "년" : "분기";

                            System.out.println(subject + ": 최근 3" + periodText + " 지속 성장한 종목은 아래와 같다.");
                            System.out.println(infos);
                            System.out.println(counter.incrementAndGet() + "/" + all);
                        }));

        final List[] allSuccessInfos = new List[]{new ArrayList<>(companyInfos)};

        Arrays.stream(financeTypes)
                .forEach(subject -> Arrays.stream(periodTypes)
                        .forEach(period -> allSuccessInfos[0] = Calculator.execute(subject, period, allSuccessInfos[0])));

        Arrays.stream(financeTypes).forEach(System.out::println);
        Arrays.stream(periodTypes).forEach(System.out::println);
        System.out.println("모든 재무 필터를 거친 종목은 아래와 같다.");
        System.out.println(allSuccessInfos[0]);
    }
}
