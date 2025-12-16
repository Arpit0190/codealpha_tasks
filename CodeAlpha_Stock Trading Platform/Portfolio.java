import java.util.*;

public class Portfolio {
    private Map<String, Integer> holdings = new HashMap<>();
    private List<Transaction> transactions = new ArrayList<>();

    public void buyStock(String symbol, int qty, double price) {
        holdings.put(symbol, holdings.getOrDefault(symbol, 0) + qty);
        transactions.add(new Transaction(symbol, qty, price));
    }

    public void sellStock(String symbol, int qty, double price) {
        holdings.put(symbol, holdings.get(symbol) - qty);
        transactions.add(new Transaction(symbol, -qty, price));
    }

    public void displayPortfolio(Market market) {
        System.out.println("\n--- Portfolio ---");
        double totalValue = 0;

        for (String symbol : holdings.keySet()) {
            int qty = holdings.get(symbol);
            double price = market.getStock(symbol).getPrice();
            double value = qty * price;
            totalValue += value;

            System.out.println(symbol + " | Qty: " + qty + " | Value: $" + value);
        }
        System.out.println("Total Portfolio Value: $" + totalValue);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public Map<String, Integer> getHoldings() {
        return holdings;
    }
}

