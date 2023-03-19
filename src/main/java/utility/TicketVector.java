package utility;

import ticket.Ticket;
import ticket.TicketType;
import ticket.Venue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

/**
 * Класс, отвечающий за организацию хранения и доступа к объектам класса {@link Ticket}. Тип коллекции, в которой хранятся объекты - Vector
 */
public class TicketVector {
    /**
     * Поле id.
     * Отвечает за уникальность id билетов
     */
    private Long id = 0L;
    /**
     * Поле даты и времени создания данного объекта
     */
    private final java.time.ZonedDateTime creationDate;
    /**
     * Поле коллекции, в которой хранятся объекты класса {@link Ticket}
     */
    private final Vector<Ticket> tv = new Vector<>();
    /**
     * Поле длинны коллекции tv
     *
     * @see TicketVector#tv
     */
    private long length = 0;

    public TicketVector() {
        creationDate = java.time.ZonedDateTime.now();
    }

    /**
     * Добавляет объект в коллекцию. При этом, если id объекта неоригинально, он не будет добавлен
     *
     * @param ticket объект класса {@link  Ticket}
     * @return возврящает true если удалось добавить объект, false - если нет
     */
    public boolean add(Ticket ticket) {
        if (ticket.getId() != null) this.id = Math.max(this.id + 1, ticket.getId() + 1);
        else {
            ticket.setId(id);
            id++;
        }
        if (tv.stream().anyMatch(t -> (t.getId() == ticket.getId() || t.getVenue().getId() == ticket.getVenue().getId())))
            return false;
        tv.add(ticket);
        length++;
        if (ticket.getId() >= id) id = ticket.getId();
        return true;
    }

    /**
     * Добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции
     *
     * @param ticket объект класса {@link  Ticket}
     * @return возвращает true, если удалось добавить объект, false - если нет
     */
    public boolean addIfMax(Ticket ticket) {
        Ticket maxT = maxTicket();
        if (maxT == null) return add(ticket);
        if (ticket.compareTo(maxT) > 0) return add(ticket);
        return false;
    }

    /**
     * Добавить новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции
     *
     * @param ticket объект класса {@link  Ticket}
     * @return возвращает true, если удалось добавить объект, false - если нет
     */
    public boolean addIfMin(Ticket ticket) {
        Ticket minT = minTicket();
        if (minT == null) return add(ticket);
        if (ticket.compareTo(minT) < 0) return add(ticket);
        return false;
    }

    /**
     * Обновляет элемент коллекции с указанным id
     *
     * @param ticket объект типа {@link Ticket}
     * @param id     id объекта, который надо обновить. Предполагается, что id проверенно на корректность (в коллекции существует элемент с таким id) {@link TicketVector#validId}
     */
    public void update(Ticket ticket, long id) {
        tv.removeIf(t -> t.getId() == id);
        length--;
        add(ticket);
    }

    /**
     * Очищает коллекцию
     */
    public void clear() {
        tv.clear();
        length = 0;
    }

    /**
     * Удаляет элемент по переданному индексу
     *
     * @param index индекс элемента, который нужно удалить
     * @return возвращает true, если длинна коллекции больше указанного индекса, false - если нет
     */
    public boolean remove(int index) {
        if (index >= length) return false;
        tv.remove(index);
        length--;
        return true;
    }

    /**
     * Удаляет все элементы коллекции, меньшие переданного объекта
     *
     * @param ticket объект класса {@link Ticket}, с которым производится сравнение {@link Ticket#compareTo}
     * @return возвращает количество удаленных объектов
     */
    public int removeLower(Ticket ticket) {
        List<Ticket> delTickets = tv.stream().filter(t -> ticket.compareTo(t) > 0).toList();
        length -= delTickets.size();
        tv.removeAll(delTickets);
        return delTickets.size();
    }

    /**
     * @return возвращает массив со всеми элементами коллекции
     */
    public List<Ticket> getAll() {
        return sortBySize(tv.stream().toList());
    }

    /**
     * Удаляет элемент коллекции с указанным id
     *
     * @param id id элемента, который нужно удалить. Предполагается, что id проверенно на корректность (в коллекции существует элемент с таким id) {@link TicketVector#validId}
     */
    public void removeById(long id) {
        length -= tv.size();
        tv.removeIf(t -> t.getId() == id);
        length += tv.size();
    }

    /**
     * @return возвращает минимальный элемент коллекции в строковом представлении. Сравнение ведется по полю venue {@link Venue#compareTo}
     */
    public String getMinByVenue() {
        Optional<Ticket> t = tv.stream().min(Comparator.comparing(Ticket::getVenue));
        return t.isPresent() ? t.get().toString() : "Массив пустой";
    }

    /**
     * @param str строка, по которой ведется поиск
     * @return возвращает элементы, значение поля name которых содержит заданную подстроку
     */
    public List<Ticket> filterContainsName(String str) {
        return sortBySize(tv.stream().filter(t -> t.getName().contains(str)).toList());
    }

    /**
     * @param price число
     * @return возвращает элементы, значение поля price которых меньше заданного
     */
    public List<Ticket> filterLessThanPrice(int price) {
        return sortBySize(tv.stream().filter(t -> t.getPrice() < price).toList());
    }

    /**
     * @param price число
     * @return возвращает элементы, значение поля price которых равно заданному
     */
    public List<Ticket> filterByPrice(int price) {
        return sortBySize(tv.stream().filter(t -> t.getPrice() == price).toList());
    }

    /**
     * @return возвращает значения поля type всех элементов в порядке возрастания {@link TicketType}
     */
    public String getFieldAscendingType() {
        StringBuilder str = new StringBuilder();
        tv.stream().sorted((t1, t2) -> t2.getType().compareTo(t1.getType())).forEach(t -> str.append(String.format("id:%s - type:%s\n", t.getId(), t.getType())));
        return str.toString();
    }

    /**
     * @param type тип билета {@link TicketType}
     * @return возвращает количество элементов коллекции, тип которых превышает переданный
     */
    public long getCountGreaterThanType(TicketType type) {
        return tv.stream().filter(t -> type.compareTo(t.getType()) > 0).count();
    }

    /**
     * @return возвращает максимальный элемент коллекции {@link Ticket#compareTo}.
     * Если коллекция пуста вернет null
     */
    private Ticket maxTicket() {
        return tv.stream().max(Ticket::compareTo).orElse(null);
    }

    /**
     * @return возвращает минимальный элемент коллекции {@link Ticket#compareTo}.
     * Если коллекция пуста вернет null
     */
    private Ticket minTicket() {
        return tv.stream().min(Ticket::compareTo).orElse(null);
    }

    /**
     * @return возвращает информацию об коллекции <b>дата инициализации {@link TicketVector#creationDate}</b>, <b>количество элементов {@link TicketVector#length}</b>, <b>максимальный элемент {@link TicketVector#maxTicket}</b>, <b>минимальный элемент {@link TicketVector#minTicket}</b>
     */
    public String getInfo() {
        return String.format("Тип - Vector\nДата инициализации - %s\nКоличество элементов - %s\nМаксимальный элемент - %s\nМинимальный элемент - %s", creationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ZZ")), length, maxTicket(), minTicket());
    }

    /**
     * Проверяет, что элемент с указанным id есть в коллекции
     *
     * @param id число
     * @return возвращает true, если в коллекции есть элемент с указанным id, false - если нет
     */
    public boolean validId(long id) {
        return tv.stream().anyMatch(t -> t.getId() == id);
    }

    public List<Ticket> sortBySize(List<Ticket> tVec) {
        return tVec.stream().sorted((t1, t2) -> {
            int s1, s2;
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(t1);
                s1 = baos.size();
                oos = new ObjectOutputStream(baos);
                oos.writeObject(t2);
                oos.close();
                s2 = baos.size() - s1;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return Long.compare(s1, s2);
        }).toList();
    }
}
