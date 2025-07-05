package perAndPbr;

public class StockInfo {
    private String name, code;               // 6자리 코드
    public String getName() {return name;}
    public void   setName(String n){name=n;}
    public String getCode() {return code;}
    public void   setCode(String c){code=c;}
    @Override public String toString(){return code+" "+name;}
}

