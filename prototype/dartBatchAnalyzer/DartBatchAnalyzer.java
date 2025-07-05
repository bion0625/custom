package dartBatchAnalyzer;

import naverStockCrawler.CompanyCrawler;

import java.util.List;

public class DartBatchAnalyzer {

    public static void main(String[] args) throws Exception {
        String apiKey = args[0];

        // 1. 전체 종목 로드
        System.out.println("📥 기업 리스트 불러오는 중...");
        List<Corp> corps = CompanyCrawler.getCompanyInfo().stream()
                .map(info -> new Corp(info.getCode(), info.getCode())).toList();
        System.out.println("총 기업 수: " + corps.size());

        boolean isThree = false;
        System.out.println("3분기 성장률을 조건에 넣는지: " + isThree);

        int successCount = 0;
        int matchCount = 0;

        // 2. 종목 순회
        for (Corp corp : corps) {

            // 테스트 중엔 200개 정도만 제한 실행
            //if (successCount > 200) break;

            // 중간 진행 로그
            if (successCount % 100 == 0 && successCount != 0) {
                System.out.println("... " + successCount + "개 처리 완료");
            }

            // 예외처리: 금융업 제외 등 (선택사항)
            //if (corp.getName().contains("은행") || corp.getName().contains("금융")) continue;

            successCount++;

            if (isThree) {
                List<Financials> fsList = DartApiClient.fetchFinancialsForQuarters(corp, apiKey);
                Thread.sleep(300);

                if (fsList.size() >= 3 && FinancialAnalyzer.matchesGrowthCriteria(fsList)) {
                    System.out.println("✅ 성장 종목 추천: " + corp.getName());
                    for (Financials ff : fsList) {
                        System.out.println("   - " + ff);
                    }
                } else continue;
            }


            Financials f = DartApiClient.fetchFinancials(corp, "2024", "11013", apiKey); // 1분기
            Thread.sleep(300); // 과도한 호출 방지 (DART 요청 제한 고려)

            if (f != null && matchesCriteria(f)) {
                matchCount++;
                System.out.println("✅ 추천 종목: " + f);
            }
        }
        System.out.println("... " + successCount + "개 처리 완료");

        System.out.println("🎉 조건 만족 종목 수: " + matchCount);
    }

    /**
     * 기본 재무 기준 필터링 조건
     *
     * 다음 세 가지 재무 지표가 모두 0보다 큰 경우에만 true를 반환:
     * - 매출액: 실제로 영업활동을 통해 매출이 발생했는지 (유령기업 제외)
     * - 영업이익: 본업에서 수익을 내고 있는지 (적자 기업 제외)
     * - 영업활동 현금흐름(OCF): 실제 현금이 유입되고 있는지 (건전성 평가)
     *
     * 즉, 실적과 현금흐름이 모두 양호한 기업만 추천 대상으로 간주함.
     */
    static boolean matchesCriteria(Financials f) {
        return f.getRevenue() > 0 &&
                f.getOperatingProfit() > 0 &&
                f.getOperatingCashFlow() > 0;
    }
}
