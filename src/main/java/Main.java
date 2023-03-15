import utility.CSVReaderAndWriter;
import utility.Client;
import utility.ConsoleWriter;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (args.length > 0) {
            if (args[0].equals("server")) {
                CSVReaderAndWriter csvRW = new CSVReaderAndWriter();
                if (args.length > 1)
                    csvRW.setFile(args[1]);
                utility.Server ex = new utility.Server(csvRW);
                ex.createTQFromCSV();
                ex.acceptingConnections();
            }
        }
        else {
            try {
                ConsoleWriter cw = new ConsoleWriter();
                Client cr = new Client(cw);
                cr.readingCycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}