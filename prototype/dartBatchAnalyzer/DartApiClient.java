package dartBatchAnalyzer;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class DartApiClient {
    private static final String BASE_URL = "https://opendart.fss.or.kr/api/fnlttSinglAcntAll.xml";

    public static Financials fetchFinancials(Corp corp, String year, String reprtCode, String apiKey) {
        try {
            String urlStr = BASE_URL + "?"
                    + "crtfc_key=" + apiKey
                    + "&corp_code=" + URLEncoder.encode(corp.getCorpCode(), "UTF-8")
                    + "&bsns_year=" + year
                    + "&reprt_code=" + reprtCode
                    + "&fs_div=CFS"; // 연결재무제표

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            InputStream input = conn.getInputStream();
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(input);

            NodeList list = doc.getElementsByTagName("list");

            Financials f = new Financials(corp.getName(), corp.getCorpCode(), year, reprtCode);

            for (int i = 0; i < list.getLength(); i++) {
                Element e = (Element) list.item(i);
                String account = e.getElementsByTagName("account_nm").item(0).getTextContent();
                String amountStr = e.getElementsByTagName("thstrm_amount").item(0).getTextContent()
                        .replace(",", "").replaceAll("[^0-9-]", "");

                long amount = amountStr.isEmpty() ? 0 : Long.parseLong(amountStr);

                switch (account) {
                    case "매출액":
                        f.setRevenue(amount);
                        break;
                    case "영업이익":
                        f.setOperatingProfit(amount);
                        break;
                    case "당기순이익":
                        f.setNetIncome(amount);
                        break;
                    case "영업활동으로인한현금흐름":
                        f.setOperatingCashFlow(amount);
                        break;
                }
            }

            return f;

        } catch (Exception e) {
            System.err.println("❌ " + corp.getName() + " (" + corp.getCorpCode() + ") 재무정보 조회 실패");
            return null;
        }
    }

    public static List<Financials> fetchFinancialsForQuarters(Corp corp, String apiKey) {
        List<Financials> result = new ArrayList<>();

        // 최근 3개 분기 순차 조회
        result.add(fetchFinancials(corp, "2024", "11013", apiKey)); // 1분기
        result.add(fetchFinancials(corp, "2023", "11012", apiKey)); // 연간
        result.add(fetchFinancials(corp, "2023", "11011", apiKey)); // 3분기

        // null 제거
        result.removeIf(f -> f == null);

        return result;
    }
}
