import java.time.LocalDateTime;

public class Transaction {
    private String symbol;
    private int quantity;
    private double price;
    private LocalDateTime time;

    public Transaction(String symbol, int quantity, double price) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.time = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return time + " | " + symbol + " | Qty: " + quantity + " | $" + price;
    }
}

