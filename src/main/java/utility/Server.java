package utility;

import ticket.Ticket;
import ticket.TicketType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Класс серверного приложения.
 * <br>Принимает команду от клиентского приложения.
 * <br>Исполняет ее с помощью класса {@link TicketVector}
 * <br>Отправляет ответ обратно клиенту
 */
public class Server {
    /**
     * Поле {@link TicketVector}
     */
    private final SQLTickets sqlt;
    /**
     * Поле {@link ServerSocket}, который создает сокеты для общения с клиентом
     */
    private final ServerSocket serv;
    /**
     * Поле логгера {@link Logger}
     */
    private static final Logger logger = LogManager.getLogger(Server.class);


    public Server(SQLTickets sqlt) throws IOException, SQLException {
        this.sqlt = sqlt;
        serv = new ServerSocket(5454);
        serv.setSoTimeout(200);
    }

    /**
     * Чтение команды клиента
     *
     * @return возвращает полученную команду
     */
    private Command readRequest(Socket sock) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
        return (Command) ois.readObject();
    }

    /**
     * Ответ клиенту
     */
    private void response(Command command, Socket sock) throws IOException, SQLException {
        Answer answer;
        if (command.hasTicket) answer = commandExecutionWithElement(command);
        else answer = commandExecution(command);
        ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
        oos.writeObject(answer);
    }

    public void acceptingConnections() throws IOException, ClassNotFoundException, SQLException {
        logger.info("Сервер запущен.");
        while (true) {
            try {
                Socket sock = serv.accept();
                logger.info("Установлено подключение. Адрес - " + sock.getRemoteSocketAddress() + ".");
                Command command = readRequest(sock);
                logger.info("Получена команда " + String.join(" ", command.getCommand()) + ".");
                response(command, sock);
                logger.info("Отправлен ответ.");
                logger.info(sock.getRemoteSocketAddress() + " отключился.");
            } catch (SocketTimeoutException e) {
                if (System.in.available() > 0) {
                    try {
                        Scanner in = new Scanner(System.in);
                        switch (in.next()) {
                            case ("save"):
//                                if (csvRW.writeToCSV(sqlt.getAll())) logger.info("Сохранение прошло успешно.");
//                                else logger.warn("Не удалось сохранить данные в связи с ошибкой записи в файл.");
                                break;
                            case ("exit"):
//                                if (csvRW.writeToCSV(sqlt.getAll())) logger.info("Сохранение прошло успешно.");
//                                else logger.warn("Не удалось сохранить данные в связи с ошибкой записи в файл.");
                                logger.info("Сервер выключен.");
                                sqlt.exit();
                                serv.close();
                                System.exit(0);
                        }
                    } catch (NoSuchElementException err) {
                        logger.warn("Экстренное выключение сервера.");
                        sqlt.exit();
                        serv.close();
                        System.exit(0);
                    }
                }
            }
        }
    }

    /**
     * С помощью {@link CSVReaderAndWriter} считывает объекты из csv файла и добавляет в вектор ({@link TicketVector})
     */
    public void createTQ() throws SQLException {
        String resp = sqlt.loadTickets();
        if (resp.equals("OK")) logger.info("Загрузка коллекции из базы данных прошла успешно");
        else logger.warn(resp);
    }

    /**
     * Исполнение команд не требующих создания объекта класса {@link Ticket}.<br>
     * Команды - <b>show</b>, <b>clear</b>, <b>remove_first</b>, <b>remove_at</b>, <b>remove_by_id</b>, <b>min_by_venue</b>, <b>filter_contains_name</b>, <b>filter_less_than_price</b>, <b>filter_by_price</b>, <b>save</b>, <b>info</b>, <b>count_greater_than_type</b>, <b>print_field_ascending_type</b>
     *
     * @param command объект класса {@link Command}
     * @return возвращает объект класса {@link Answer} для отправки клиенту
     */
    public Answer commandExecution(Command command) throws SQLException {
        switch (command.getCommand()[0]) {
            case ("show"):
                StringBuilder str = new StringBuilder();
                sqlt.getAll().forEach(t -> str.append(t).append("\n"));
                return new Answer(str.toString(), false);
            case ("clear"):
                String[] resp = sqlt.clear().split("/");
                if (resp[0].equals("OK")) return new Answer("Коллекция очищена", true);
                else {
                    logger.warn(resp[1]);
                    return new Answer(resp[0], false);
                }
            case ("remove_first"):
                resp = sqlt.remove(0).split("/");
                if (resp[0].equals("OK"))
                    return new Answer("Первый элемент удален", true);
                else {
                    logger.warn(resp[1]);
                    return new Answer(resp[0], false);
                }
            case ("remove_at"):
                resp = sqlt.remove(Integer.parseInt(command.getCommand()[1])).split("/");
                if (resp[0].equals("OK"))
                    return new Answer("Элемент под индексом " + command.getCommand()[1] + " удален", true);
                else {
                    logger.warn(resp[1]);
                    return new Answer(resp[0], false);
                }
            case ("remove_by_id"):
                long id = Long.parseLong(command.getCommand()[1]);
                if (!sqlt.validId(id))
                    return new Answer("Неверный id", false);
                resp = sqlt.removeById(id).split("/");
                if (resp[0].equals("OK"))
                    return new Answer(String.format("Элемент с id %s удален", id), true);
                else {
                    logger.warn(resp[1]);
                    return new Answer(resp[0], false);
                }
            case ("min_by_venue"):
                return new Answer(sqlt.getMinByVenue(), false);
            case ("filter_contains_name"):
                StringBuilder sb = new StringBuilder();
                String name;
                if (command.getCommand().length > 1) name = command.getCommand()[1];
                else name = "";
                for (Ticket t : sqlt.filterContainsName(name)) sb.append(t.toString());
                return new Answer(sb.toString(), false);
            case ("filter_less_than_price"):
                sb = new StringBuilder();
                int price = Integer.parseInt(command.getCommand()[1]);
                for (Ticket t : sqlt.filterLessThanPrice(price)) sb.append(t.toString());
                return new Answer(sb.toString(), false);
            case ("filter_by_price"):
                sb = new StringBuilder();
                price = Integer.parseInt(command.getCommand()[1]);
                for (Ticket t : sqlt.filterByPrice(price)) sb.append(t.toString());
                return new Answer(sb.toString(), false);
            case ("info"):
                return new Answer(sqlt.getInfo(), false);
            case ("count_greater_than_type"):
                return new Answer(String.valueOf(sqlt.getCountGreaterThanType(TicketType.valueOf(command.getCommand()[1]))), false);
            case ("print_field_ascending_type"):
                return new Answer(sqlt.getFieldAscendingType(), false);
        }
        return new Answer("error", true);
    }

    /**
     * Исполнение команд, требующих создание объекта класса {@link Ticket}.
     * Команды - <b>add</b>, <b>update</b>, <b>add_if_max</b>, <b>add_if_min</b>, <b>remove_lower</b>.<br>
     * Передаются параметры нужные для создания объекта класса {@link Ticket}({@link Ticket#Ticket})
     *
     * @param command объект класса {@link Command}
     * @return возвращает объект класса {@link Answer} для отправки клиенту
     */
    public Answer commandExecutionWithElement(Command command) throws SQLException {
        switch (command.getCommand()[0]) {
            case ("add"):
                String[] resp = sqlt.add(command.getTicketBuilder()).split("/");
                if (resp[0].equals("OK")) return new Answer("Объект добавлен", true);
                else {
                    logger.warn(resp[1]);
                    return new Answer(resp[0], false);
                }
            case ("update"):
                long id = Long.parseLong(command.getCommand()[1]);
                if (!sqlt.validId(id)) return new Answer("Неверный id", false);
                resp = sqlt.update(command.getTicketBuilder(), id).split("/");
                if (resp[0].equals("OK")) return new Answer("Объект обновлен", true);
                else {
                    logger.warn(resp[1]);
                    return new Answer(resp[0], false);
                }
            case ("add_if_max"):
                resp = sqlt.addIfMax(command.getTicketBuilder()).split("/");
                if (resp[0].equals("OK")) return new Answer("Объект добавлен", true);
                else {
                    return new Answer(resp[0], false);
                }
            case ("add_if_min"):
                resp = sqlt.addIfMin(command.getTicketBuilder()).split("/");
                if (resp[0].equals("OK")) return new Answer("Объект добавлен", true);
                else {
                    return new Answer(resp[0], false);
                }
            case ("remove_lower"):
                resp = sqlt.removeLower(command.getTicketBuilder()).split("/");
                if (resp[0].matches("^[0-9]+$")) return new Answer("Удалено " + resp[0] + " элементов", false);
                else {
                    logger.info(resp[1]);
                    return new Answer(resp[0], false);
                }
        }
        return new Answer("error", false);
    }
}
