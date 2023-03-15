package ticket;

import utility.CSVReaderAndWriter;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;

/**
 * Класс билета с полями <b>id</b>, <b>name</b>, <b>coordinates</b>, <b>creationDate</b>, <b>price</b>, <b>type</b> и <b>venue</b>
 */
public class Ticket implements Comparable<Ticket>, Serializable {
    /**
     * Поле id.
     * Значение должно быть больше 0, значение должно быть уникальным, поле не может быть null
     */
    private Long id;
    /**
     * Поле названия.
     * Не может быть null, строка не может быть пустой
     */
    private final String name;
    /**
     * Поле координат {@link Coordinates}.
     * Не может быть null
     */
    private final Coordinates coordinates;
    /**
     * Поле времени и даты создания.
     * Не может быть null
     */
    private final java.time.LocalDateTime creationDate;
    /**
     * Поле цены.
     * Не может быть null, значение должно быть больше 0
     */
    private final Integer price;
    /**
     * Поле типа билета {@link TicketType}.
     * Не может быть null
     */
    private final TicketType type;
    /**
     * Поле места назначения {@link Venue}.
     * Не может быть null
     */
    private final Venue venue;

    public Ticket(Long id, String name, Coordinates coordinates, java.time.LocalDateTime creationDate, Integer price, TicketType type, Venue venue) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.price = price;
        this.type = type;
        this.venue = venue;
    }

    public Ticket(String name, Coordinates coordinates, java.time.LocalDateTime creationDate, Integer price, TicketType type, Venue venue) {
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.price = price;
        this.type = type;
        this.venue = venue;
    }

    @Override
    public String toString() {
        return String.format("{id:%s, name:%s, coordinates:%s, creationDate:%s, price:%s, type:%s, venue:%s}", id, name, coordinates, creationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), price, type, venue);
    }

    /**
     * @param separator символ разделения колонок в csv файле. {@link CSVReaderAndWriter#separator}
     * @return возвращает строку в формате для записи в csv файл
     */
    public String toCSVFormat(String separator) {
        return id + separator + name + separator + coordinates.toCSVFormat(separator) + separator + creationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + separator + price + separator + type + separator + venue.toCSVFormat(separator);
    }

    /**
     * Сравнивает объект с объектом типа ticket.Ticket {@link Ticket}
     * Сравнение происходит по полям {@link Ticket#type}({@link TicketType}), {@link Ticket#venue}({@link Venue}) и {@link Ticket#price}
     *
     * @param t объект типа {@link Ticket}, с которым производится сравнение
     */
    @Override
    public int compareTo(Ticket t) {
        int v = this.type.compareTo(t.type) * -5;
        v += this.venue.compareTo(t.venue);
        if (this.price > t.price) v += 2;
        else if (this.price < t.price) v--;
        return v;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
        this.venue.setId(id);
    }

    public Venue getVenue() {
        return venue;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public TicketType getType() {
        return type;
    }
}