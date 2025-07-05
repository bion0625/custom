package dartBatchAnalyzer;

public class Corp {
    private String name;
    private String corpCode;

    public Corp(String name, String corpCode) {
        this.name = name;
        this.corpCode = corpCode;
    }

    public String getName() {
        return name;
    }

    public String getCorpCode() {
        return corpCode;
    }

    @Override
    public String toString() {
        return "dartBatchAnalyzer.Corp{name='" + name + "', corpCode='" + corpCode + "'}";
    }
}
