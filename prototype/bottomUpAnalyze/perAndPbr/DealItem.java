package bottomUpAnalyze.perAndPbr;

public class DealItem {
    private String code, name;  private double per, pbr;
    public void setCode(String c){code=c;}
    public void setName(String n){name=n;}
    public void setPer(double v){per=v;}
    public void setPbr(double v){pbr=v;}
    @Override public String toString(){
        return String.format("\n[%s] %s  PER:%.2f  PBR:%.2f", code, name, per, pbr);
    }
}

