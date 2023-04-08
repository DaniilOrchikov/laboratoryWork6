import utility.CSVReaderAndWriter;
import utility.Client;
import utility.ConsoleWriter;
import utility.SQLTickets;

import java.io.IOException;
import java.sql.*;

public class Main {
    /**
     * Если при запуске с консоли указать ключ server, то запустится серверное приложение. Если при этом указать файл, данные из него будут загружены в коллекцию
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        if (args.length > 0) {
            if (args[0].equals("server")) {
                SQLTickets sqlt = new SQLTickets();
                utility.Server ex = new utility.Server(sqlt);
                ex.createTQ();
                ex.acceptingConnections();
            }
        } else {
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