import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Market market = new Market();
        User user = new User("Alice", 10000);

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n1. View Market");
            System.out.println("2. Buy Stock");
            System.out.println("3. Sell Stock");
            System.out.println("4. View Portfolio");
            System.out.println("5. Save Portfolio");
            System.out.println("0. Exit");

            int choice = sc.nextInt();

            if (choice == 0) break;

            switch (choice) {
                case 1:
                    market.displayMarket();
                    break;

                case 2:
                    System.out.print("Enter stock symbol: ");
                    String buySym = sc.next();
                    System.out.print("Quantity: ");
                    int buyQty = sc.nextInt();
                    user.buy(market.getStock(buySym), buyQty);
                    break;

                case 3:
                    System.out.print("Enter stock symbol: ");
                    String sellSym = sc.next();
                    System.out.print("Quantity: ");
                    int sellQty = sc.nextInt();
                    user.sell(market.getStock(sellSym), sellQty);
                    break;

                case 4:
                    user.getPortfolio().displayPortfolio(market);
                    System.out.println("Balance: $" + user.getBalance());
                    break;

                case 5:
                    FileManager.savePortfolio(user.getPortfolio());
                    System.out.println("Portfolio saved!");
                    break;
            }
        }
        sc.close();
    }
}
