package utility;

import ticket.TicketBuilder;
import ticket.TicketType;
import ticket.VenueType;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Stack;

import static java.lang.Thread.sleep;

/**
 * Класс для чтения данных из консоли или файла и парсинг этих данных в команды. Также для вывода данных на стандартный поток вывода. Поля <b>in</b>, <b>scannerStack</b>, <b>fileNamesStack</b>, <b>ex</b>
 */
public class Client {
    /**
     * Поле сканер ввода.
     * По умолчанию - системный ввод
     */
    private Scanner in = new Scanner(System.in);
    /**
     * Поле стек сканеров.
     * Используется для организации рекурсивного чтения из файлов.
     * Когда в файле встречается команда исполнения нового файла, сюда помещается старый сканер
     */
    private final Stack<Scanner> scannerStack = new Stack<>();
    /**
     * Поле стек имен файлов.
     * Используется для организации рекурсивного чтения из файлов.
     * Когда начинается чтение из файла сюда заносится его имя. При каждой попытке открытия нового потока считывания файла проверяется не занесено ли сюда его имя. Если это так - запрещается считывание.
     * Это исключает ситуации бесконечной рекурсии при чтении файлов.
     */
    private final Stack<String> fileNamesStack = new Stack<>();
    /**
     * Поле объекта, который пишет в консоль
     */
    private final ConsoleWriter cw;
    private final TicketBuilder tb = new TicketBuilder();

    public Client(ConsoleWriter cw) throws IOException {
        this.cw = cw;
    }

    /**
     * Цикл считывания команд с помощью {@link Client}. Цикл завершается, если {@link Server#exit} == true или если введен символ конца ввода
     */
    public void readingCycle() throws IOException {
        while (true) {
            String[] command = read();
            nextCommand(command);
        }
    }

    /**
     * Проверяет не закончился ли считываемый файл. Если да - достает следующий сканер из {@link Client#scannerStack}.
     * Если из стека достали последний сканер (ввод с консоли) устанавливает {@link ConsoleWriter#inputStatus} 0
     */
    private void checkingScanner() {
        if (scannerStack.empty())
            cw.setInputStatus(0);
        if (cw.getInputStatus() == 1 || cw.getInputStatus() == 2) {
            while (!in.hasNext()) {
                in = scannerStack.pop();
                fileNamesStack.pop();
                if (scannerStack.empty()) {
                    cw.setInputStatus(0);
                    break;
                }
            }
        }
    }

    /**
     * Чтение следующей строки и разбиение ее на слова (разделитель - пробел)
     *
     * @return возвращает массив строк
     */
    public String[] read() throws IOException {
        checkingScanner();
        cw.print(">>");
        return nextInput().split(" ");
    }

    public void exit() {
        System.exit(0);
    }

    /**
     * Считывание строки
     *
     * @return возвращает строку
     */
    private String nextInput(){
        String str = "";
        try {
            str = in.nextLine().trim();
        } catch (NoSuchElementException e) {
            emergencyExit();
        }
        return str;
    }

    private void emergencyExit() {
        cw.printIgnoringPrintStatus("Экстренный выход");
        exit();
    }

    /**
     * Выборка команды.
     * Если команда не требует обращения к коллекции - исполнение команды.
     * Если команда не требует ввод объекта - команда передается Исполнителю {@link Server}({@link Server#commandExecution}).
     * Если команда требует ввода объекта - просит ввести поля и создает объект с помощью {@link TicketBuilder}
     * Далее передает данные и команду {@link Server}({@link Server#commandExecutionWithElement})
     *
     * @param command массив строк (команда, которую необходимо исполнить и, при необходимости, параметры)
     */
    public void nextCommand(String[] command) throws IOException {
        try {
            if (!checkingCompositeCommands(command)) return;
            switch (command[0]) {
                case ("update"):
                case ("add_if_max"):
                case ("add_if_min"):
                case ("add"):
                case ("remove_lower"):
                    tb.clear();
                    cw.println("Введите имя: ");
                    if (!enteringField("name")) return;
                    cw.println("Введите первую координату: ");
                    if (!enteringField("x")) return;
                    cw.println("Введите вторую координату: ");
                    if (!enteringField("y")) return;
                    cw.println("Введите цену: ");
                    if (!enteringField("price")) return;
                    cw.println("Введите тип билета " + Arrays.toString(TicketType.values()) + ": ");
                    if (!enteringField("tType")) return;
                    cw.println("Введите вместительность: ");
                    if (!enteringField("capacity")) return;
                    cw.println("Введите тип места назначения " + Arrays.toString(VenueType.values()) + ": ");
                    if (!enteringField("vType")) return;
                    cw.println("Введите название улицы: ");
                    if (!enteringField("street")) return;
                    cw.println("Введите почтовый индекс: ");
                    if (!enteringField("zip")) return;
                    try {
                        communicatingWithServer(command, (byte) 1);
                    } catch (ConnectException e) {
                        cw.printIgnoringPrintStatus("Сервер не отвечает");
                        if (cw.getInputStatus() != 0) return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case ("info"):
                case ("show"):
                case ("remove_by_id"):
                case ("remove_at"):
                case ("clear"):
                case ("remove_first"):
                case ("min_by_venue"):
                case ("filter_contains_name"):
                case ("filter_less_than_price"):
                case ("filter_by_price"):
                case ("count_greater_than_type"):
                case ("print_field_ascending_type"):
                    try {
                        communicatingWithServer(command, (byte) 0);
                    } catch (ConnectException e) {
                        cw.printIgnoringPrintStatus("Сервер не отвечает");
                        if (cw.getInputStatus() != 0) return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case ("help"):
                    cw.printIgnoringPrintStatus("""
                            help: вывести справку по доступным командам
                            info: вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)
                            show: вывести в стандартный поток вывода все элементы коллекции в строковом представлении
                            add {element}: добавить новый элемент в коллекцию
                            update id {element}: обновить значение элемента коллекции, id которого равен заданному
                            remove_by_id id: удалить элемент из коллекции по его id
                            clear: очистить коллекцию
                            execute_script file_name: считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.
                            exit: завершить программу (без сохранения в файл)
                            remove_first: удалить первый элемент из коллекции
                            add_if_max {element}: добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции
                            add_if_min {element}: добавить новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции
                            min_by_venue: вывести любой объект из коллекции, значение поля venue которого является минимальным
                            filter_contains_name name: вывести элементы, значение поля name которых содержит заданную подстроку
                            filter_less_than_price price: вывести элементы, значение поля price которых меньше заданного
                            remove_at index : удалить элемент, находящийся в заданной позиции коллекции (index)
                            remove_lower {element} : удалить из коллекции все элементы, меньшие, чем заданный
                            count_greater_than_type type : вывести количество элементов, значение поля type которых больше заданного
                            filter_by_price price : вывести элементы, значение поля price которых равно заданному
                            print_field_ascending_type : вывести значения поля type всех элементов в порядке возрастания""");
                    break;
                case ("execute_script"):
                    try {
                        new Scanner(Paths.get(command[1]));
                    } catch (AccessDeniedException e) {
                        System.out.println("Недостаточно прав для доступа к файлу " + command[1]);
                        break;
                    }
                    cw.setInputStatus(1);
                    scannerStack.add(in);
                    fileNamesStack.add(command[1]);
                    in = new Scanner(Paths.get(command[1]));
                    break;
                case ("exit"):
                    exit();
                    break;
                default:
                    cw.println("Введена неверная команда. Для просмотра справки по доступным командам введите команду help");
            }
        } catch (NoSuchElementException e) {
            cw.printIgnoringPrintStatus("Исполнение файла " + fileNamesStack.peek() + " прервано в связи с ошибкой при выполнении команды " + command[0]);
        }
    }

    private void communicatingWithServer(String[] command, byte mode) throws IOException, InterruptedException, ClassNotFoundException {
        try {
            int port = 5452;
            SocketChannel sock = SocketChannel.open(new InetSocketAddress(port));
            sock.configureBlocking(false);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            if (mode == 1) {
                if (command[0].equals("update"))
                    oos.writeObject(new Command(command, tb.getTicket(Long.parseLong(command[1]))));
                else oos.writeObject(new Command(command, tb.getTicket()));
            }else{
                oos.writeObject(new Command(command));
            }
            oos.close();
            byte[] arr = baos.toByteArray();
            ByteBuffer buf = ByteBuffer.wrap(arr);
            sock.write(buf);
            buf.clear();
            byte[] buffer = new byte[8192];
            ByteBuffer buff = ByteBuffer.wrap(buffer);
            sleep(100);
            sock.read(buff);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buff.array()));
            Answer answer = (Answer) ois.readObject();
            String answerText = answer.text();
            if (answer.systemInformation()) cw.println(answerText);
            else cw.printIgnoringPrintStatus(answerText);
            ois.close();
            sock.close();
        } catch (StreamCorruptedException e) {
            cw.printIgnoringPrintStatus("Не удалось получить ответ от сервера");
        }
    }

    /**
     * Проверка аргументов команд с аргументами на соответствие требованиям типов данных, валидности id и т.д.
     *
     * @param command команда
     * @return возвращает true, если введенный аргумент соответствует требованиям, false - если не соответствует
     */
    private boolean checkingCompositeCommands(String[] command) {
        switch (command[0]) {
            case ("update"):
            case ("remove_by_id"):
                try {
                    Long.parseLong(command[1]);
                } catch (NumberFormatException e) {
                    cw.println("Неверный формат id");
                    return false;
                } catch (ArrayIndexOutOfBoundsException e) {
                    cw.println("Вы не ввели id");
                    return false;
                }
                break;
            case ("count_greater_than_type"):
                try {
                    TicketType.valueOf(command[1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    cw.println("Вы не ввели тип");
                    return false;
                } catch (IllegalArgumentException e) {
                    cw.println("Неверный тип");
                    return false;
                }
                break;
            case ("filter_less_than_price"):
            case ("filter_by_price"):
                try {
                    if (Long.parseLong(command[1]) <= 0) {
                        cw.println("Цена должна быть больше 0");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    cw.println("Неверный формат поля price");
                    return false;
                } catch (ArrayIndexOutOfBoundsException e) {
                    cw.println("Вы не ввели поле price");
                    return false;
                }
                break;
            case ("filter_contains_name"):
                break;
            case ("execute_script"):
                if (command.length < 2) {
                    cw.println("Вы не ввели имя файла");
                    return false;
                }
                for (String fileName : fileNamesStack) {
                    if (fileName.equals(command[1])) {
                        cw.printIgnoringPrintStatus("Невозможно исполнить файл " + "\"" + command[1] + "\" из-за возникновения рекурсии");
                        return false;
                    }
                }
                File f = new File(command[1]);
                if (!f.exists()) {
                    cw.println("Неверное имя файла");
                    return false;
                } else if (f.isDirectory()) {
                    cw.println("Невозможно исполнить директорию");
                    return false;
                }
                break;
            case ("remove_at"):
                try {
                    int index = Integer.parseInt(command[1]);
                    if (index < 0) {
                        cw.println("Индекс не может быть меньше нуля");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    cw.println("Неверный формат индекса");
                    return false;
                } catch (ArrayIndexOutOfBoundsException e) {
                    cw.println("Вы не ввели индекс");
                    return false;
                }
                break;
            default:
                if (command.length > 1) {
                    cw.println("Введена неверная команда. Для просмотра справки по доступным командам введите команду help");
                    return false;
                }
        }
        return true;
    }

    public boolean enteringField(String command) throws IOException {
        while (true) {
            String status = switch (command) {
                case ("name") -> tb.setName(nextInput().trim());
                case ("x") -> tb.setX(nextInput().trim());
                case ("y") -> tb.setY(nextInput().trim());
                case ("price") -> tb.setPrice(nextInput().trim());
                case ("tType") -> tb.setType(nextInput().trim());
                case ("capacity") -> tb.setVenueCapacity(nextInput().trim());
                case ("vType") -> tb.setVenueType(nextInput().trim());
                case ("street") -> tb.setAddressStreet(nextInput().trim());
                case ("zip") -> tb.setAddressZipCode(nextInput().trim());
                default -> "error";
            };
            if (!status.equals("OK")) {
                if (cw.getInputStatus() == 1) {
                    cw.printIgnoringPrintStatus("В файле " + fileNamesStack.peek() + " введены неверные данные для создания объекта");
                    fileNamesStack.pop();
                    in = scannerStack.pop();
                    tb.clear();
                    return false;
                }
                cw.println(status);
                continue;
            }
            break;
        }
        return true;
    }
}