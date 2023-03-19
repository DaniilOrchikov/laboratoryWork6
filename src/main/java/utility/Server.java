package utility;

import ticket.Ticket;
import ticket.TicketType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Класс исполняющий команды, которые требуют обратиться к коллекции ({@link TicketVector}), управляющий работой {@link Client} и {@link CSVReaderAndWriter}.
 * Поля - <b>exit</b>, <b>tv</b>, <b>cr</b> и <b>csvRW</b>
 */
public class Server {
    /**
     * Поле {@link TicketVector}
     */
    private final TicketVector tv = new TicketVector();
    /**
     * Поле {@link CSVReaderAndWriter}
     */
    private final CSVReaderAndWriter csvRW;
    private final ServerSocket serv;
    private static final Logger logger = LogManager.getLogger(Server.class);


    public Server(CSVReaderAndWriter csvRW) throws IOException {
        this.csvRW = csvRW;
        serv = new ServerSocket(5452);
        serv.setSoTimeout(200);
    }

    public void acceptingConnections() throws IOException, ClassNotFoundException {
        logger.info("Сервер запущен.");
        while (true) {
            try {
                Socket sock = serv.accept();
                logger.info("Установлено подключение. Адрес - " + sock.getRemoteSocketAddress() + ".");
                ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
                Command command = (Command) ois.readObject();
                Answer answer;
                if (command.hasTicket) answer = commandExecutionWithElement(command);
                else answer = commandExecution(command);
                ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
                oos.writeObject(answer);
                logger.info(sock.getRemoteSocketAddress() + " отключился.");
            } catch (SocketTimeoutException e) {
                if (System.in.available() > 0) {
                    try {
                        Scanner in = new Scanner(System.in);
                        switch (in.next()) {
                            case ("save"):
                                if (csvRW.writeToCSV(tv.getAll())) logger.info("Сохранение прошло успешно.");
                                else logger.warn("Не удалось сохранить данные в связи с ошибкой записи в файл.");
                                break;
                            case ("exit"):
                                if (csvRW.writeToCSV(tv.getAll())) logger.info("Сохранение прошло успешно.");
                                else logger.warn("Не удалось сохранить данные в связи с ошибкой записи в файл.");
                                logger.info("Сервер выключен.");
                                serv.close();
                                System.exit(0);
                        }
                    } catch (NoSuchElementException err) {
                        logger.warn("Экстренное выключение сервера без сохранения.");
                        serv.close();
                        System.exit(0);
                    }
                }
            }
        }
    }
    public void log(String str){
        logger.info(str);
    }

    /**
     * С помощью {@link CSVReaderAndWriter} считывает объекты из csv файла и добавляет в вектор ({@link TicketVector})
     */
    public void createTQFromCSV() {
        long invalidId = 0, invalidTicket = 0;
        while (csvRW.hasNext()) {
            try {
                if (!tv.add(csvRW.nextTicket())) invalidId++;
            } catch (InputMismatchException e) {
                invalidTicket++;
            } catch (NoSuchElementException ignored) {
            }
        }
        if (invalidId > 0)
            logger.warn("Объектов не добавлено по причине неоригинального id - " + invalidId + ".");
        if (invalidTicket > 0)
            logger.warn("Объектов не добавлено по причине несоответствия структуре - " + invalidTicket + ".");
        if (!csvRW.getFileName().equals("") && invalidTicket == 0 && invalidId == 0)
            logger.warn("Загружено без ошибок.");
    }

    /**
     * Исполнение команд не требующих создания объекта класса {@link Ticket}.<br>
     * Команды - <b>show</b>, <b>clear</b>, <b>remove_first</b>, <b>remove_at</b>, <b>remove_by_id</b>, <b>min_by_venue</b>, <b>filter_contains_name</b>, <b>filter_less_than_price</b>, <b>filter_by_price</b>, <b>save</b>, <b>info</b>, <b>count_greater_than_type</b>, <b>print_field_ascending_type</b>
     *
     * @param command массив строк
     */
    public Answer commandExecution(Command command) {
        switch (command.getCommand()[0]) {
            case ("show"):
                StringBuilder str = new StringBuilder();
                tv.getAll().stream().forEach(t -> str.append(t).append("\n"));
                return new Answer(str.toString(), false);
            case ("clear"):
                tv.clear();
                return new Answer("Коллекция очищена", true);
            case ("remove_first"):
                if (tv.remove(0))
                    return new Answer("Первый элемент удален", true);
                else
                    return new Answer("Массив пустой", false);
            case ("remove_at"):
                if (tv.remove(Integer.parseInt(command.getCommand()[1])))
                    return new Answer("Элемент под индексом " + command.getCommand()[1] + " удален", true);
                else
                    return new Answer("Индекс выходит за границы массива", false);
            case ("remove_by_id"):
                long id = Long.parseLong(command.getCommand()[1]);
                if (!tv.validId(id))
                    return new Answer("Неверный id", false);
                tv.removeById(id);
                return new Answer(String.format("Элемент с id %s удален", id), true);
            case ("min_by_venue"):
                return new Answer(tv.getMinByVenue(), false);
            case ("filter_contains_name"):
                StringBuilder sb = new StringBuilder();
                String name;
                if (command.getCommand().length > 1) name = command.getCommand()[1];
                else name = "";
                for (Ticket t : tv.filterContainsName(name)) sb.append(t.toString());
                return new Answer(sb.toString(), false);
            case ("filter_less_than_price"):
                sb = new StringBuilder();
                int price = Integer.parseInt(command.getCommand()[1]);
                for (Ticket t : tv.filterLessThanPrice(price)) sb.append(t.toString());
                return new Answer(sb.toString(), false);
            case ("filter_by_price"):
                sb = new StringBuilder();
                price = Integer.parseInt(command.getCommand()[1]);
                for (Ticket t : tv.filterByPrice(price)) sb.append(t.toString());
                return new Answer(sb.toString(), false);
            case ("info"):
                return new Answer(tv.getInfo(), false);
            case ("count_greater_than_type"):
                return new Answer(String.valueOf(tv.getCountGreaterThanType(TicketType.valueOf(command.getCommand()[1]))), false);
            case ("print_field_ascending_type"):
                return new Answer(tv.getFieldAscendingType(), false);
        }
        return new Answer("error", true);
    }

    /**
     * Исполнение команд, требующих создание объекта класса {@link Ticket}.
     * Команды - <b>add</b>, <b>update</b>, <b>add_if_max</b>, <b>add_if_min</b>, <b>remove_lower</b>.<br>
     * Передаются параметры нужные для создания объекта класса {@link Ticket}({@link Ticket#Ticket})
     *
     * @param command массив строк
     */
    public Answer commandExecutionWithElement(Command command) {
        switch (command.getCommand()[0]) {
            case ("add"):
                if (tv.add(command.getTicket())) return new Answer("Объект добавлен", true);
                else return new Answer("Объект не добавлен. Неоригинальный id", false);
            case ("update"):
                long id = Long.parseLong(command.getCommand()[1]);
                if (!tv.validId(id)) return new Answer("Неверный id", false);
                tv.update(command.getTicket(), id);
                return new Answer("Объект обновлен", true);
            case ("add_if_max"):
                if (tv.addIfMax(command.getTicket())) return new Answer("Объект добавлен", true);
                else return new Answer("Объект не добавлен", false);
            case ("add_if_min"):
                if (tv.addIfMin(command.getTicket())) return new Answer("Объект добавлен", true);
                else return new Answer("Объект не добавлен", false);
            case ("remove_lower"):
                return new Answer("Удалено " + tv.removeLower(command.getTicket()) + " элементов", false);
        }
        return new Answer("error", false);
    }
}
