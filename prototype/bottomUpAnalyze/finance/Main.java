package bottomUpAnalyze.finance;

import dto.StockInfo;
import krx.CompanyCrawler;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/******************************************************************************
 * 영업이익
 * 매출액
 * 순이익률 (X)
 * 당기순이익 (X)
 * 위 리스트가 3분기 혹은 3년 동안 꾸준히 올랐는지 확인된 종목 추출
 ******************************************************************************/

public class Main {
    public static void main(String[] args) {
        FinanceType[] financeTypes = FinanceType.values();
        PeriodType[] periodTypes = PeriodType.values();

        int all = financeTypes.length * periodTypes.length;
        AtomicInteger counter = new AtomicInteger();

        List<StockInfo> companyInfos = CompanyCrawler.getCompanyInfo();

        for (PeriodType period : periodTypes) {
            for (FinanceType subject : financeTypes) {
                List<StockInfo> infos = Calculator.execute(subject, period, companyInfos);
                String periodText = period.equals(PeriodType.YEAR) ? "년" : "분기";

                System.out.println(subject + ": 최근 3" + periodText + " 지속 성장한 종목은 아래와 같다.");
                System.out.println(infos);
                System.out.println(counter.incrementAndGet() + "/" + all);
            }
        }

        for (PeriodType period : periodTypes) {
            for (FinanceType subject : financeTypes) {
                companyInfos = Calculator.execute(subject, period, companyInfos);
            }
        }

        Arrays.stream(financeTypes).forEach(System.out::println);
        Arrays.stream(periodTypes).forEach(System.out::println);
        System.out.println("모든 재무 필터"+
                Arrays.toString(FinanceType.values())
                +"가 성장중인 종목은 아래와 같다.");
        System.out.println(companyInfos);
    }
}
