package bottomUpAnalyze;

import bottomUpAnalyze.finance.Calculator;
import bottomUpAnalyze.finance.FinanceType;
import bottomUpAnalyze.finance.PeriodType;
import bottomUpAnalyze.perAndPbr.DealItem;
import bottomUpAnalyze.perAndPbr.ValuationFilter;
import dto.StockInfo;
import krx.CompanyCrawler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

        StringBuilder resultMsg = new StringBuilder("모든 재무 필터" +
                Arrays.toString(FinanceType.values())
                + "가 최근 3년 및\n" +
                "최근 3분기 성장중이면서\n" +
                "PER이 15.0(그레이엄 PER 한계),\n" +
                "PBR이 1.5(그레이엄 PBR 한계) " +
                "미만인 종목은 아래와 같다.\n");
        try {
            List<DealItem> picks = ValuationFilter.filterUndervalued(successInfos);
            resultMsg.append(String.format("🎯 추천 종목: %d개\n", picks.size()));
            for (DealItem pick : picks) {
                resultMsg.append(pick).append("\n");
            }

            System.out.println(resultMsg);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // 파일명에 현재 날짜·시간을 포함
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String filename = formatter.format(now) + ".txt";

        // 파일에 쓰기
        try (BufferedWriter writer =  Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_8)) {
            writer.write(resultMsg.toString());
            System.out.println("결과를 파일로 저장했습니다: " + filename);
        } catch (IOException ioe) {
            System.err.println("파일 쓰기 오류: " + ioe.getMessage());
        }
    }
}
