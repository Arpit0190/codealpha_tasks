import java.io.*;
import java.util.Map;

public class FileManager {

    public static void savePortfolio(Portfolio portfolio) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("portfolio.txt"));
        for (Map.Entry<String, Integer> entry : portfolio.getHoldings().entrySet()) {
            writer.write(entry.getKey() + "," + entry.getValue());
            writer.newLine();
        }
        writer.close();
    }
}
