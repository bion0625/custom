package perAndPbr;
/******************************************************************************
 *  📈  VALUE-SCREENER  |  KRX + NAVER + 순수 Java 11
 *
 *  ──────────────────────────────────────────────────────────────────────────
 *  개요
 *  --------------------------------------------------------------------------
 *  ▸ 이 코드는 외부 라이브러리 없이( **java.net.http** 만 사용 )
 *    “한국 상장사 가치저평가 종목”을 자동으로 선별하는 간단한 CLI 툴이다.
 *
 *      1)  KRX KIND 엑셀(HTML) 다운로드 → 회사명·종목코드 추출
 *      2)  네이버 금융 페이지에서 코스피/코스닥 로고(img) 존재 확인
 *      3)  네이버 금융 첫 열 PER·PBR 숫자 크롤링
 *      4)  “PER ≤ 15  AND  PBR ≤ 1.5”  (벤저민 그레이엄 절대 저평가) 필터
 *      5)  통과 종목을 DealItem 으로 출력
 *
 *  --------------------------------------------------------------------------
 *  모듈 구성
 *  --------------------------------------------------------------------------
 *  ▶ StockInfo               : { name, code }  ─ 회사 기본정보 DTO
 *  ▶ DealItem                : { code, name, per, pbr } ─ 추천 종목 DTO
 *
 *  ▶ CompanyCrawler
 *      • KIND CSV (EUC-KR) 다운로드
 *        - 302  → HTTPS 리다이렉트 follow
 *        - Content-Type: html / ms-excel  →  HTML 테이블 파싱
 *      • 종목코드 열이 숫자 1~6자리인 행만 사용
 *      • 네이버 금융 로고(img.kospi|img.kosdaq)로 상장시장 판별
 *
 *  ▶ StockValuationCrawler
 *      • java.net.http.HttpClient 로 네이버 HTML 요청
 *      • 정규식  “<th><strong>PER(…)</strong></th><td><em>12.34</em>”  패턴 캡처
 *      • PER / PBR 첫 번째 값 추출(숫자 아니면 -1 반환)
 *
 *  ▶ ValuationFilter   ←  main()
 *      • ThreadPool (고정 THREADS) 로 네이버 크롤링 병렬화
 *      • PER & PBR 음수(파싱 실패) 행 제외
 *      • 𝙿𝙴𝚁 ≤ MAX_PER  &&  𝙿𝙱𝚁 ≤ MAX_PBR  통과 시 DealItem 생성
 *
 *  --------------------------------------------------------------------------
 *  주요 파라미터
 *  --------------------------------------------------------------------------
 *      MAX_PER   = 15.0        // 그레이엄 PER 한계
 *      MAX_PBR   = 1.5         // 그레이엄 PBR 한계
 *      THREADS   = 6           // 동시 네이버 호출 스레드 수
 *
 *  파라미터를 조정해 임계값(예: PER 20, PBR 2) 또는 스레드 수를 쉽게 변경 가능.
 *
 *  --------------------------------------------------------------------------
 *  빌드 & 실행
 *  --------------------------------------------------------------------------
 *      javac -d out src/dartBatchAnalyzer/*.java
 *      jar --create --file screener.jar \
 *          --main-class dartBatchAnalyzer.ValuationFilter -C out .
 *      java -jar screener.jar
 *
 *  --------------------------------------------------------------------------
 *  주의 사항
 *  --------------------------------------------------------------------------
 *  • KIND 응답은 실제 CSV가 아닌 EUC-KR HTML 테이블(.xls) → HTML 파싱 우선
 *  • 네이버 HTML 구조가 바뀌면 StockValuationCrawler.extract() 정규식 수정 필요
 *  • 네이버 과도 크롤링 시 차단될 수 있으니 THREADS·sleep 조절 권장
 *
 *  ──────────────────────────────────────────────────────────────────────────
 *  © 2025  value-screener  (MIT Licence)   —  Pure-Java example for learning
 ******************************************************************************/

public class Main {
    public static void main(String[] args) throws Exception {
        ValuationFilter.main(args);
    }
}
