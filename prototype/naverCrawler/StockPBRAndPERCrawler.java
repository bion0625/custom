package naverCrawler;

import dto.StockPBRAndPER;

import java.net.URI;
import java.net.http.*;
import java.util.regex.*;

public class StockPBRAndPERCrawler {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static StockPBRAndPER fetchForPerAndPbr(String code) throws Exception {
        String html = CLIENT.send(
                HttpRequest.newBuilder(
                                URI.create("https://finance.naver.com/item/main.naver?code=" + code))
                        .header("User-Agent","Mozilla/5.0").build(),
                HttpResponse.BodyHandlers.ofString()).body();

        double per = extract(html,"PER");
        double pbr = extract(html,"PBR");
        return new StockPBRAndPER(per,pbr);
    }

    /** 라벨( PER/PBR ) 뒤 첫 <td> 숫자 */
    private static double extract(String html,String label){
        String re="(?s)<th[^>]*><strong>\\s*"+label+
                "\\([^<]+</strong></th>\\s*<td[^>]*>\\s*([\\d.,]+)";
        Matcher m=Pattern.compile(re).matcher(html);
        if(m.find()){
            return Double.parseDouble(m.group(1).replace(",",""));
        }
        return -1;
    }
}

