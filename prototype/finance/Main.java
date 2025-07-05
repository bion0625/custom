package finance;

import dto.StockInfo;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Arrays.stream(FinanceType.values()).forEach(type -> {
            List<StockInfo> infos = Calculator.execute(type);
            String text = type.equals(FinanceType.PBR) || type.equals(FinanceType.PER) ? "축소된" : "성장한";
            System.out.println(type + "이 최근 3분기 지속 "+text+" 종목은 아래와 같다.");
            System.out.println(infos);
        });
    }
}
