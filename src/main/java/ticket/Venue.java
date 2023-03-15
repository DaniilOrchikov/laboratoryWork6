package ticket;

import utility.CSVReaderAndWriter;

import java.io.Serializable;

/**
 * Класс места назначения билета с полями <b>id</b>, <b>name</b>, <b>capacity</b>, <b>type</b>, <b>address</b>
 */
public class Venue implements Comparable<Venue>, Serializable {
    /**
     * Поле id.
     * Значение должно быть больше 0, значение должно быть уникальным
     */
    private long id;
    /**
     * Поле название.
     * Не может быть null, Строка не может быть пустой
     */
    private final String name;
    /**
     * Поле вместимость.
     * не может быть null, должно быть больше 0
     */
    private final Long capacity;
    /**
     * Поле типа места назначения {@link VenueType}.
     * Не может быть Null
     */
    private final VenueType type;
    /**
     * Поле адрес {@link Address}.
     * Не может быть null
     */
    private final Address address;

    public Venue(long id, String name, Long capacity, VenueType type, Address address) {
        this.name = name;
        this.capacity = capacity;
        this.id = id;
        this.type = type;
        this.address = address;
    }
    public Venue(String name, Long capacity, VenueType type, Address address) {
        this.name = name;
        this.capacity = capacity;
        this.type = type;
        this.address = address;
    }

    @Override
    public String toString() {
        return String.format("{id:%s, name:%s, capacity:%s, type:%s, address:%s}", id, name, capacity, type, address);
    }
    /**
     * @param separator символ разделения колонок в csv файле. {@link CSVReaderAndWriter#separator}
     * @return возвращает строку в формате для записи в csv файл
     */
    public String toCSVFormat(String separator) {
        return capacity + separator + type + separator + address.toCSVFormat(separator);
    }

    /**
     * @param v объект типа {@link Venue}, с которым производится сравнение
     * Объекты сравниваются по полю {@link Venue#capacity}
     */
    @Override
    public int compareTo(Venue v) {
        return Long.compare(this.capacity, v.capacity);
    }

    public long getId() {
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }
}
