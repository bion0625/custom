package dartBatchAnalyzer;

import java.util.List;

public class DartBatchAnalyzer {

    public static void main(String[] args) throws Exception {
        String apiKey = args[0];

        // 1. 전체 종목 로드
        System.out.println("📥 기업 리스트 불러오는 중...");
        List<Corp> corps = CorpCodeResolver.loadAllCorps(apiKey);
        System.out.println("총 기업 수: " + corps.size());

        int successCount = 0;
        int matchCount = 0;

        // 2. 종목 순회
        for (Corp corp : corps) {

            // 예외처리: 금융업 제외 등 (선택사항)
            if (corp.getName().contains("은행") || corp.getName().contains("금융")) continue;

            Financials f = DartApiClient.fetchFinancials(corp, "2024", "11013", apiKey); // 1분기
            Thread.sleep(300); // 과도한 호출 방지 (DART 요청 제한 고려)

            if (f != null) {
                successCount++;

                if (matchesCriteria(f)) {
                    matchCount++;
                    System.out.println("✅ 추천 종목: " + f);
                }
            }

            // 중간 진행 로그
            if (successCount % 100 == 0) {
                System.out.println("... " + successCount + "개 처리 완료");
            }

            // 테스트 중엔 200개 정도만 제한 실행
            if (successCount >= 200) break;
        }

        System.out.println("🎉 조건 만족 종목 수: " + matchCount);
    }

    static boolean matchesCriteria(Financials f) {
        return f.getRevenue() > 0 &&
                f.getOperatingProfit() > 0 &&
                f.getOperatingCashFlow() > 0;
    }
}
