import utility.*;

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
                Server serv = new Server(sqlt);
                serv.createTQ();
                serv.mainLoop();
            }
        } else {
            try {
                ConsoleWriter cw = new ConsoleWriter();
                Client client = new Client(cw);
                client.readingCycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}