package bottomUpAnalyze.finance;

import dto.StockInfo;
import krx.CompanyCrawler;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        FinanceType[] financeTypes = FinanceType.values();
        PeriodType[] periodTypes = PeriodType.values();

        int all = financeTypes.length + periodTypes.length;
        int counter = 0;

        List<StockInfo> companyInfos = CompanyCrawler.getCompanyInfo();
        Arrays.stream(financeTypes)
                .forEach(subject -> Arrays.stream(periodTypes)
                        .forEach(period -> {
                            List<StockInfo> infos = Calculator.execute(subject, period, companyInfos);
                            String subjectText = subject.equals(FinanceType.PBR) || subject.equals(FinanceType.PER) ? "축소된" : "성장한";
                            String periodText = period.equals(PeriodType.YEAR) ? "년" : "분기";

                            System.out.println(subject + ": 최근 3" + periodText + " 지속 "+subjectText+" 종목은 아래와 같다.");
                            System.out.println(infos);
                            System.out.println(counter + "/" + all);
                        }));
    }
}
