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


/**
 * ---------------------------------------------
 * 💡 DART 종목 추천기 - JAR 파일 빌드 및 실행 방법
 * ---------------------------------------------
 *
 * 이 프로젝트는 prototype/dartBatchAnalyzer 패키지 아래에 있는
 * Java 소스들을 컴파일하여 단일 JAR 파일로 빌드할 수 있습니다.
 *
 * ✅ [1] 컴파일
 * - Java 17 기준
 * - out/ 디렉터리에 클래스 파일 생성
 *
 * > javac -d out prototype/dartBatchAnalyzer/*.java
 *
 *
 * ✅ [2] JAR 파일 생성
 * - 메인 클래스는 `dartBatchAnalyzer.Main` 으로 지정
 * - app.jar 이름으로 단일 실행 파일 생성
 *
 * > jar --create --file app.jar --main-class dartBatchAnalyzer.Main -C out .
 *
 *
 * ✅ [3] JAR 실행
 * - 실행 시 인자로 DART API Key를 전달해야 함
 *
 * > java -jar app.jar <YOUR_API_KEY>
 *
 *
 * 🔍 실행 구조 요약:
 * - Main.main(args) → DartBatchAnalyzer.main(args)
 * - args[0] = API Key
 *
 * 🔒 주의: 네트워크 접근 필요 (DART Open API 호출)
 *
 * 🔍 JAR 메타데이터 확인 (선택):
 * > jar --describe-module --file app.jar
 *
 */
