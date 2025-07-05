package dartBatchAnalyzer;

import java.util.List;

/**
 * [사용자 정의 조건 반영: 최근 3분기 성장 추세 필터링]
 *
 * 이 메서드는 사용자가 제안한 종목 추천 조건 중 다음을 구현합니다:
 *
 * ✅ 조건 1. 최근 3분기 영업이익이 지속적으로 증가하는가?
 *     → opGrow: q3 < q2 < q1 순으로 영업이익 비교
 *
 * ✅ 조건 2. 최근 3분기 영업활동 현금흐름(OCF)이 개선되고 있는가?
 *     → ocfGrow: q3 < q2 < q1 순으로 OCF 비교
 *
 * 즉, "영업이익의 최근 3분기 성장 지표"와
 *     "최근 3분기 현금흐름이 좋아지는지" 조건을 모두 만족하는 종목만 true 반환
 *
 * ❌ PER, PBR 필터링은 현재 포함되어 있지 않으며, 추후 KRX API 또는 웹 크롤링을 통해 별도 구현 필요
 *
 * 반환값:
 * - true: 실적과 현금흐름이 모두 개선되고 있는 성장 종목
 * - false: 성장 추세가 명확하지 않거나 데이터가 불충분한 경우
 */

public class FinancialAnalyzer {

    public static boolean matchesGrowthCriteria(List<Financials> fsList) {
        // 최근 3개 분기의 재무제표가 모두 있는지 확인
        if (fsList.size() < 3) return false;

        // 최신 분기부터 과거 순으로 재무 데이터 분리
        Financials q1 = fsList.get(0); // 가장 최근 분기 (예: 2024년 1분기)
        Financials q2 = fsList.get(1); // 두 번째 분기 (예: 2023년 연간 또는 4분기)
        Financials q3 = fsList.get(2); // 세 번째 분기 (예: 2023년 3분기)

        // 영업이익이 분기마다 증가하고 있는지 확인
        // q3 < q2 < q1 이면 true → 실적이 개선 추세
        boolean opGrow = q1.getOperatingProfit() > q2.getOperatingProfit() &&
                q2.getOperatingProfit() > q3.getOperatingProfit();

        // 영업활동으로 인한 현금흐름(OCF)이 분기마다 증가하고 있는지 확인
        boolean ocfGrow = q1.getOperatingCashFlow() > q2.getOperatingCashFlow() &&
                q2.getOperatingCashFlow() > q3.getOperatingCashFlow();

        // 영업이익과 OCF가 모두 연속적으로 증가하는 기업만 추천 대상
        return opGrow && ocfGrow;

    }
}

