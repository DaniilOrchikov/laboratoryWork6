package utility;

import ticket.Ticket;
import ticket.TicketBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * Класс отвечающий за чтение и запись в csv файл
 */
public class CSVReaderAndWriter {
    /**
     * Поле имя файла
     */
    private String fileName = "";
    /**
     * Поле сканера
     */
    private Scanner scanner;
    /**
     * Поле символ разделения колонок в csv файле
     */
    private String separator = ";";
    private final TicketBuilder tb = new TicketBuilder();

    /**
     * Пытается установить поток считывания из файла
     *
     * @param fileName имя csv файла, в который будут записываться, и из которого будут читаться данные
     */
    public void setFile(String fileName) {
        this.fileName = fileName;
        try {
            File f = new File(this.fileName);
            if (!f.exists()) {
                System.out.println("Неверное имя файла");
            } else if (f.isDirectory()) {
                System.out.println("Невозможно исполнить директорию");
            } else if (!f.canRead()) {
                System.out.println("Недостаточно прав для чтения из файла");
            } else {
                scanner = new Scanner(Paths.get(fileName));
                scanner.useDelimiter("\n");
            }
        } catch (IOException e) {
            System.out.println("Не удалось настроить поток чтения из файла");
        }
    }

    /**
     * @return Возвращает true, если можно продолжить считывание, и false - если нельзя
     */
    public boolean hasNext() {
        if (scanner == null) return false;
        return scanner.hasNext();
    }

    /**
     * @return возвращает следующий объект из csv фала
     */
    public Ticket nextTicket() {
        return parseCSVLine(scanner.next());
    }

    /**
     * @param line строка, из которой необходимо создать объект
     * @return возвращает объект класса {@link Ticket}
     */
    private Ticket parseCSVLine(String line) {
        Scanner scanner = new Scanner(line.trim());
        scanner.useDelimiter(separator);
        tb.clear();
        long id;
        try {
            id = Long.parseLong(scanner.next());
        }catch (NumberFormatException e){
            throw new InputMismatchException();
        }
        if (!tb.setName(scanner.next()).equals("OK"))throw new InputMismatchException();
        if (!tb.setX(scanner.next()).equals("OK"))throw new InputMismatchException();
        if (!tb.setY(scanner.next()).equals("OK"))throw new InputMismatchException();
        if (!tb.setCreationDate(scanner.next()).equals("OK"))throw new InputMismatchException();
        if (!tb.setPrice(scanner.next()).equals("OK"))throw new InputMismatchException();
        if (!tb.setType(scanner.next()).equals("OK"))throw new InputMismatchException();
        if (!tb.setVenueCapacity(scanner.next()).equals("OK"))throw new InputMismatchException();
        if (!tb.setVenueType(scanner.next()).equals("OK"))throw new InputMismatchException();
        if (!tb.setAddressStreet(scanner.next()).equals("OK"))throw new InputMismatchException();
        if (!tb.setAddressZipCode(scanner.next()).equals("OK"))throw new InputMismatchException();
        return tb.getTicket(id);
    }

    /**
     * Запись в файл в формате csv
     *
     * @param tickets массив объектов класса {@link Ticket}, которые необходимо записать в файл
     * @return возвращает true, если удалось настроить поток записи и записать объекты в файл, false - если что-то пошло не так
     */
    public boolean writeToCSV(List<Ticket> tickets) {
        File f = new File(fileName);
        if (!f.exists() || f.isDirectory()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                System.out.println("Не удалось создать файл");
                return false;
            }
        } else if (!f.canWrite()) {
            System.out.println("Недостаточно прав на файл " + fileName);
            return false;
        }
        try (FileWriter writer = new FileWriter(fileName)) {
            for (Ticket t : tickets) {
                writer.write(t.toCSVFormat(";") + "\n");
            }
            return true;
        } catch (IOException e) {
            return false;

        }
    }
    public String getFileName(){
        return fileName;
    }
}
