package naverCrawler;

import util.HttpUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurrentOpCrawler {

    public static Double fetchForOne(String code, String name) throws Exception {
        String url = "https://finance.naver.com/item/main.naver?code=" + code;
        String html = HttpUtil.fetchString(url);

        // 1) trade_compare 테이블부터 시작되는 위치
        int tableStart = html.indexOf("section trade_compare");
        if (tableStart < 0) return 0D;

        // 2) 테이블 </table>까지 snippet 자르기
        int tableTagStart = html.indexOf("<table", tableStart);
        int tableEnd = html.indexOf("</table>", tableTagStart);
        if (tableTagStart < 0 || tableEnd < 0) return 0D;
        String snippet = html.substring(tableTagStart, tableEnd + 8);

        // 3) <th><span>name</span></th> 다음 첫 <td> 값 추출
        // 1) name에서 괄호 안 내용 제거 → baseName (예: "PBR")
        String baseName = name.replaceAll("\\(.*?\\)", "");

        // 2) 정규식: <span>baseName(anything)</span> 뒤 첫 <td> 숫자 캡처
        Pattern p = Pattern.compile(
                "(?si)"                                 // DOTALL + CASE_INSENSITIVE
                        + "<th[^>]*>\\s*<span[^>]*>\\s*"
                        + Pattern.quote(baseName)                // "PER" 또는 "PBR"
                        + "\\([^<]+\\)\\s*</span>\\s*</th>.*?"     // (… 아무 글자…) </span></th> 까지
                        + "<td[^>]*>\\s*([\\d.,-]+)\\s*</td>"      // 첫 <td> 안의 숫자(콤마·소수점·마이너스) 캡처
        );
        Matcher m = p.matcher(snippet);
        if (m.find()) {
            String raw = m.group(1).replace(",", "");
            return raw.equals("-") ? 0L : Double.parseDouble(raw);
        }
        return 0D;
    }
}

