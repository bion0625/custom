package bottomUpAnalyze.finance;

import dto.StockInfo;
import krx.CompanyCrawler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
                            String subjectText = subject.equals(FinanceType.PBR) || subject.equals(FinanceType.PER) ? "축소된" : "성장한";
                            String periodText = period.equals(PeriodType.YEAR) ? "년" : "분기";

                            System.out.println(subject + ": 최근 3" + periodText + " 지속 "+subjectText+" 종목은 아래와 같다.");
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
