package dartBatchAnalyzer;

public class Financials {
    private String corpName;
    private String corpCode;
    private String year;
    private String reportCode;

    private long revenue;        // 매출액
    private long operatingProfit; // 영업이익
    private long netIncome;     // 당기순이익
    private long operatingCashFlow; // OCF

    public Financials(String corpName, String corpCode, String year, String reportCode) {
        this.corpName = corpName;
        this.corpCode = corpCode;
        this.year = year;
        this.reportCode = reportCode;
    }

    // Setters
    public void setRevenue(long revenue) { this.revenue = revenue; }
    public void setOperatingProfit(long profit) { this.operatingProfit = profit; }
    public void setNetIncome(long income) { this.netIncome = income; }
    public void setOperatingCashFlow(long ocf) { this.operatingCashFlow = ocf; }

    // Getters
    public String getCorpName() { return corpName; }
    public String getCorpCode() { return corpCode; }
    public long getRevenue() { return revenue; }
    public long getOperatingProfit() { return operatingProfit; }
    public long getNetIncome() { return netIncome; }
    public long getOperatingCashFlow() { return operatingCashFlow; }

    @Override
    public String toString() {
        return "[종목: " + corpName + "] " +
                "매출: " + revenue +
                ", 영업이익: " + operatingProfit +
                ", 순이익: " + netIncome +
                ", OCF: " + operatingCashFlow;
    }
}
