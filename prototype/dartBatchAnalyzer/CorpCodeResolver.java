package dartBatchAnalyzer;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CorpCodeResolver {

    private static final String CORP_CODE_URL = "https://opendart.fss.or.kr/api/corpCode.xml";

    public static List<Corp> loadAllCorps(String apiKey) throws Exception {
        List<Corp> corpList = new ArrayList<>();

        // 1. ZIP 다운로드
        URL url = new URL(CORP_CODE_URL + "?crtfc_key=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (ZipInputStream zipStream = new ZipInputStream(conn.getInputStream())) {
            ZipEntry entry = zipStream.getNextEntry();
            if (entry != null && entry.getName().equals("CORPCODE.xml")) {

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = zipStream.read(buffer)) > 0) {
                    baos.write(buffer, 0, length);
                }

                byte[] xmlBytes = baos.toByteArray();
                InputStream xmlStream = new ByteArrayInputStream(xmlBytes);

                // 2. XML 파싱
                Document doc = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder().parse(xmlStream);

                NodeList listNodes = doc.getElementsByTagName("list");
                for (int i = 0; i < listNodes.getLength(); i++) {
                    Element e = (Element) listNodes.item(i);

                    Node stockNode = e.getElementsByTagName("stock_code").item(0);
                    if (stockNode == null) continue;

                    String stockCode = stockNode.getTextContent().trim();
                    if (stockCode.isEmpty()) continue; // ← 비상장 제외

                    String name = e.getElementsByTagName("corp_name").item(0).getTextContent();
                    String code = e.getElementsByTagName("corp_code").item(0).getTextContent();
                    corpList.add(new Corp(name, code));
                }
            }
        }

        return corpList;
    }

    // ✅ 테스트용 main
    public static void main(String[] args) throws Exception {
        List<Corp> corps = loadAllCorps("api key");
        System.out.println("총 기업 수: " + corps.size());
        System.out.println("샘플: " + corps.get(0));
    }
}
