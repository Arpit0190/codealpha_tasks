import java.util.HashMap;
import java.util.Map;

public class Market {
    private Map<String, Stock> stocks = new HashMap<>();

    public Market() {
        stocks.put("AAPL", new Stock("AAPL", 180));
        stocks.put("GOOG", new Stock("GOOG", 2800));
        stocks.put("TSLA", new Stock("TSLA", 750));
    }

    public void displayMarket() {
        System.out.println("\n--- Market Prices ---");
        for (Stock stock : stocks.values()) {
            System.out.println(stock.getSymbol() + " : $" + stock.getPrice());
        }
    }

    public Stock getStock(String symbol) {
        return stocks.get(symbol);
    }
}

