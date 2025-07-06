package krx;

import dto.StockInfo;

import util.HttpUtil;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

public class CompanyCrawler {
    private static final String KIND_URL =
            "https://kind.krx.co.kr/corpgeneral/corpList.do?method=download&searchType=13";

    /** 코스피·코스닥 법인 목록 반환 */
    public static List<StockInfo> getCompanyInfo() {
        try {
            byte[] raw = HttpUtil.fetchBytes(KIND_URL);
            String body = new String(raw, "EUC-KR").trim();

            // --- HTML/CSV 판단 로직 개선
            boolean isHtml = body.startsWith("<");

            List<StockInfo> parsed = isHtml ? parseHtml(body) : parseCsv(body);

            // 필요하면 코스피/코스닥 여부 판별 — 네이버 로고 검사 (생략 가능)
            return parsed.parallelStream()
                    .filter(CompanyCrawler::isKospiOrKosdaq)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("KIND download / parse fail", e);
        }
    }

    /* ---------- 파 싱 루 틴 ---------- */

    private static final Pattern ROW = Pattern.compile(
            "(?i)<tr>\\s*<td>(.*?)</td>\\s*<td[^>]*>(\\d{6})</td>", Pattern.DOTALL);

    private static List<StockInfo> parseHtml(String html) {
        List<StockInfo> list = new ArrayList<>();
        Matcher m = ROW.matcher(html);
        while (m.find()) {
            StockInfo s = new StockInfo();
            s.setName(m.group(1).trim());
            s.setCode(m.group(2));
            list.add(s);
        }
        return list;
    }

    private static List<StockInfo> parseCsv(String csv) {
        List<StockInfo> list = new ArrayList<>();

        for (String line : csv.split("\n")) {
            String[] col = line.replace("\"", "").split(",");
            if (col.length < 2 || "회사명".equals(col[0])) continue;  // 헤더 skip

            String raw = col[1].trim();               // ① 종목코드 열
            if (!raw.matches("\\d{1,6}")) continue;   // ② 숫자 1~6자리 아니면 건너뜀

            StockInfo s = new StockInfo();            // ③ 숫자면 6자리 zero-pad
            s.setName(col[0].trim());
            s.setCode(String.format("%06d", Integer.parseInt(raw)));
            list.add(s);                              // ④ 리스트에 추가
        }
        return list;
    }

    /* ---------- 상장시장 식별 (기존 로직 유지) ---------- */

    private static boolean isKospiOrKosdaq(StockInfo s) {
        try {
            String url = "https://finance.naver.com/item/main.naver?code=" + s.getCode();
            String html = HttpUtil.fetchString(url).toLowerCase();

            return html.contains("btn_kospi.gif") || html.contains("btn_kosdaq.gif");
        } catch (Exception e) { return false; }
    }
}
