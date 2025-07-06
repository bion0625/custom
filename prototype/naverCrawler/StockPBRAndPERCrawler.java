package naverCrawler;

import dto.StockPBRAndPER;

import util.HttpUtil;
import java.util.regex.*;

public class StockPBRAndPERCrawler {

    public static StockPBRAndPER fetchForPerAndPbr(String code) throws Exception {
        String url = "https://finance.naver.com/item/main.naver?code=" + code;
        String html = HttpUtil.fetchString(url);

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

