package dartBatchAnalyzer;

/**
 * DartBatchAnalyzer
 *
 * 목적:
 * - DART 전자공시 API를 통해 전체 상장기업의 재무제표 데이터를 수집하고
 * - 사용자가 정의한 재무 기준 (ex: 영업이익 > 0, OCF > 0 등)에 따라
 *   조건을 만족하는 종목을 자동 선별하여 추천한다.
 *
 * 주요 처리 흐름:
 * 1. DART에서 전체 corp_code 목록을 가져옴 (zip 다운로드 및 파싱)
 * 2. 각 종목에 대해 fnlttSinglAcntAll API 호출 (연결재무제표 기준)
 * 3. 필수 항목(매출액, 영업이익, 순이익, OCF)을 추출
 * 4. 조건에 맞는 종목만 콘솔에 출력
 *
 * 제한 사항:
 * - DART API 호출 제한을 고려해 Thread.sleep()으로 호출 간격을 조정
 * - PER/PBR은 DART API로는 수집 불가 (추후 보완 필요)
 *
 * 향후 확장 방향:
 * - 병렬 처리 (ExecutorService)
 * - Spring REST API화
 * - 최근 3분기 추세 분석 기능 추가
 * - 추천 종목 CSV 저장 기능 등
 */

public class Main {
    public static void main(String[] args) throws Exception {
        // TODO args에 API_KEY_입력
        DartBatchAnalyzer.main(args);
    }
}
