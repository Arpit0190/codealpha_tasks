public class User {
    private String name;
    private double balance;
    private Portfolio portfolio;

    public User(String name, double balance) {
        this.name = name;
        this.balance = balance;
        this.portfolio = new Portfolio();
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public double getBalance() {
        return balance;
    }

    public void buy(Stock stock, int qty) {
        double cost = stock.getPrice() * qty;
        if (balance >= cost) {
            balance -= cost;
            portfolio.buyStock(stock.getSymbol(), qty, stock.getPrice());
        } else {
            System.out.println("Insufficient balance!");
        }
    }

    public void sell(Stock stock, int qty) {
        balance += stock.getPrice() * qty;
        portfolio.sellStock(stock.getSymbol(), qty, stock.getPrice());
    }
}

