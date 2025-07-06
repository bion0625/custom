package bottomUpAnalyze;

import bottomUpAnalyze.finance.Calculator;
import bottomUpAnalyze.finance.FinanceType;
import bottomUpAnalyze.finance.PeriodType;
import bottomUpAnalyze.perAndPbr.DealItem;
import bottomUpAnalyze.perAndPbr.ValuationFilter;
import dto.StockInfo;
import krx.CompanyCrawler;

import java.util.Arrays;
import java.util.List;
/******************************************************************************
 * 아래 두 조건으로 종목 중복으로 필터.
 *
 * 1)
 * 영업이익
 * 매출액
 * 순이익률 (X)
 * 당기순이익 (X)
 * 위 리스트가 3분기 혹은 3년 동안 꾸준히 올랐는지 확인된 종목 추출
 *
 * 2)
 * PER 및 PBR  분석: 낮을수록 좋음
 *      MAX_PER   = 15.0        // 그레이엄 PER 한계
 *      MAX_PBR   = 1.5         // 그레이엄 PBR 한계
 *
 * 위 조건보다 낮은 종목 추출
 ******************************************************************************/
public class Main {
    public static void main(String[] args) {

        FinanceType[] financeTypes = FinanceType.values();
        PeriodType[] periodTypes = PeriodType.values();

        List<StockInfo> successInfos = CompanyCrawler.getCompanyInfo();

        for (FinanceType subject : financeTypes) {
            for (PeriodType period : periodTypes) {
                successInfos = Calculator.execute(subject, period, successInfos);
            }
        }

        Arrays.stream(financeTypes).forEach(System.out::println);
        Arrays.stream(periodTypes).forEach(System.out::println);

        try {
            List<DealItem> picks = ValuationFilter.filterUndervalued(successInfos);
            System.out.println("모든 재무 필터"+
                    Arrays.toString(FinanceType.values())
                    +"가 최근 3년 및 최근 3분기 성장중이면서 " +
                    "PER이 15.0(그레이엄 PER 한계), " +
                    "PBR이 1.5(그레이엄 PBR 한계) " +
                    "미만인 종목은 아래와 같다.");
            System.out.printf("🎯 추천 종목: %d개\n", picks.size());
            picks.forEach(System.out::println);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
