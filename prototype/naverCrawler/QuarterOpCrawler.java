package naverCrawler;

import bottomUpAnalyze.finance.FinanceType;
import bottomUpAnalyze.finance.PeriodType;

import util.HttpUtil;
import java.util.*;
import java.util.regex.*;

/**
 * 순수 Java11+ HttpClient + Pattern 으로
 * “재무제표(IFRS 연결)” 테이블에서 영업이익 최근 3분기를 추출합니다.
 */
public class QuarterOpCrawler {

    /**
     * @param stockCode 6자리 종목코드 (예: "005930")
     * @return [가장 최근 분기, 직전 분기, 2분기 전] 억원 단위 영업이익.
     *         파싱 실패 시 빈 리스트 반환.
     */
    public static List<Long> fetchRecent3Op(String stockCode, FinanceType subject, PeriodType period) {
        try {
            // 1) 페이지 HTML 로드
            String url = "https://finance.naver.com/item/main.naver?code=" + stockCode;
            String html = HttpUtil.fetchString(url);

            // 2) IFRS 재무제표 테이블 위치 찾기
            int tableIdx = html.indexOf("tb_type1_ifrs");  // class="tb_type1 tb_num tb_type1_ifrs"
            if (tableIdx < 0) return Collections.emptyList();
            // 잘려야 industry-compare 나 다른 표가 섞이지 않도록, trade_compare 이전까지만
            int endIdx = html.indexOf("<div class=\"section trade_compare\"", tableIdx);
            String snippet = html.substring(tableIdx, endIdx < 0 ? html.length() : endIdx);

            // 3) 영업이익 <tr>에서 첫 10개 <td> 숫자 캡처
            Pattern p = Pattern.compile(
                    "(?is)<tr[^>]*>\\s*<th[^>]*>\\s*" +
                            "(?:<strong>\\s*)?" + subject.name() + "(?:\\s*</strong>)?\\s*</th>" +
                            "\\s*<td[^>]*>\\s*([\\d,.-]+)\\s*</td>" +
                            "\\s*<td[^>]*>\\s*([\\d,.-]+)\\s*</td>" +
                            "\\s*<td[^>]*>\\s*([\\d,.-]+)\\s*</td>" +
                            "\\s*<td[^>]*>\\s*([\\d,.-]+)\\s*</td>" +
                            "\\s*<td[^>]*>\\s*([\\d,.-]+)\\s*</td>" +
                            "\\s*<td[^>]*>\\s*([\\d,.-]+)\\s*</td>" +
                            "\\s*<td[^>]*>\\s*([\\d,.-]+)\\s*</td>" +
                            "\\s*<td[^>]*>\\s*([\\d,.-]+)\\s*</td>" +
                            "\\s*<td[^>]*>\\s*([\\d,.-]+)\\s*</td>" +
                            "\\s*<td[^>]*>\\s*([\\d,.-]+)\\s*</td>"
            );
            Matcher m = p.matcher(snippet);
            if (!m.find()) return Collections.emptyList();

            // 4) 그룹별 숫자 파싱
            List<Long> profits = new ArrayList<>(3);
            for (int i = period.equals(PeriodType.YEAR) ? 4 : 10; i >= (period.equals(PeriodType.YEAR) ? 2 : 8); i--) {
                String raw = m.group(i).replace(",", "").trim();
                if (!raw.equals("-") && !raw.isEmpty()) {
                    profits.add(Long.parseLong(raw));
                }
            }
            return profits;

        } catch (Exception e) {
            // 네트워크/파싱 오류 시 빈 리스트
            return Collections.emptyList();
        }
    }

    // 테스트 진입점
    public static void main(String[] args) {
        String code = args.length > 0 ? args[0] : "005930";
        PeriodType period = PeriodType.YEAR;
        List<Long> last3 = fetchRecent3Op(code, FinanceType.매출액, period);
        if (last3.isEmpty()) {
            System.out.println(FinanceType.매출액 + " 데이터 없음 또는 파싱 실패");
        } else {

            System.out.printf("최근 3" + (period.equals(PeriodType.YEAR) ? "년 " : "분기 ") + FinanceType.매출액 + " (억 원): %s%n", last3);
        }
    }
}


