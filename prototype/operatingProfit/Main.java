package operatingProfit;

import krx.CompanyCrawler;
import naverStockCrawler.QuarterOpCrawler;
import dto.StockInfo;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<StockInfo> list = CompanyCrawler.getCompanyInfo().parallelStream()
                .filter(info -> {
                    List<Long> profits = QuarterOpCrawler.fetchRecent3OpProfit(info.getCode());
                    return profits.size() > 3 &&
                            profits.get(0) > profits.get(1) &&
                            profits.get(1) > profits.get(2);
                })
                .toList();
        System.out.println("영업이익이 최근 3분기 지속 성장한 종목은 아래와 같다.");
        System.out.println(list);
    }
}
