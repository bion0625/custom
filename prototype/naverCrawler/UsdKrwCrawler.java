package naverCrawler;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.regex.*;

public class UsdKrwCrawler {

    /* ---------- 1. HTML 내려받기 ---------- */
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static String fetch(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")   // UA 없으면 빈 페이지가 내려올 때가 있음
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        HttpResponse<String> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200)
            throw new IOException("HTTP " + res.statusCode());
        return res.body();
    }

    /* ---------- 2. 모델 ---------- */
    public record Observation(String date /* YYYY-MM-DD */, double value) {
        @Override public String toString() { return date + " → " + value; }
    }

    /* ---------- 3. HTML → Observation 리스트 ---------- */
    private static final Pattern TR    = Pattern.compile("(?is)<tr[^>]*>(.*?)</tr>");
    private static final Pattern DATE  = Pattern.compile("(?is)<td[^>]*class=\"date\"[^>]*>(.*?)</td>");
    private static final Pattern NUM   = Pattern.compile("(?is)<td[^>]*class=\"num\"[^>]*>(.*?)</td>");

    public static List<Observation> parse(String html) {
        List<Observation> list = new ArrayList<>();

        Matcher rMatch = TR.matcher(html);
        while (rMatch.find()) {
            String row = rMatch.group(1);

            /* 1) 날짜 셀 — 2025.07.04 같은 형식 */
            Matcher dMatch = DATE.matcher(row);
            if (!dMatch.find()) continue;
            String rawDate = dMatch.group(1).replaceAll("\\s+", ""); // 공백 제거
            String date    = rawDate.replace('.', '-');              // YYYY-MM-DD

            /* 2) 첫 번째 <td class="num"> 가 ‘종가’ */
            Matcher nMatch = NUM.matcher(row);
            if (!nMatch.find()) continue;
            String numTxt = nMatch.group(1).replaceAll("<[^>]+>", "") // <img> 제거
                    .replace(",", "")              // 천단위 콤마 제거
                    .trim();
            try {
                double value = Double.parseDouble(numTxt);
                list.add(new Observation(date, value));
            } catch (NumberFormatException ignore) { /* 값이 빈칸이면 건너뜀 */ }
        }
        return list;
    }

    /* ---------- 4. 실행 예시 ---------- */
    public static void main(String[] args) {
        String url = "https://finance.naver.com/marketindex/exchangeDailyQuote.naver"
                + "?marketindexCd=FX_USDKRW";   // AJAX 파라미터 제거해야 테이블이 보임
        try {
            String html = fetch(url);
            List<Observation> data = parse(html);
            data.stream().limit(10).forEach(System.out::println);
        } catch (Exception e) {
            System.err.println("오류: " + e.getMessage());
        }
    }
}





