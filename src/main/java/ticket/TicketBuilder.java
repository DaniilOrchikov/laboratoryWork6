package ticket;


import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Класс, который поэтапно создает объект класса {@link Ticket}. При этом каждый метод возвращает строку "OK" если поле создано корректно и сообщение об ошибке, в противном случае
 */
public class TicketBuilder implements Serializable, Comparable<TicketBuilder> {
    /**
     * Класс билета с полями <b>id</b>, <b>name</b>, <b>coordinates</b>, <b>creationDate</b>, <b>price</b>, <b>type</b> и <b>venue</b>
     */


    private Long id = null;
    /*
     * Не может быть null, строка не может быть пустой
     */
    private String name = null;
    /*
     * Не может быть null
     */
    private Coordinates coordinates = null;
    /*
     * Не может быть null, значение должно быть больше 0
     */
    private Integer price = null;
    /*
     * Не может быть null
     */
    private TicketType type = null;
    /*
     * не может быть null, должно быть больше 0
     */
    private Long venueCapacity = null;
    /*
     * Не может быть Null
     */
    private VenueType venueType = null;
    /*
     * Не может быть пустой, Поле не может быть null
     */
    private String addressStreet = null;
    /*
     * Не может быть null
     */
    private String addressZipCode = null;
    private Integer x = null;
    private Integer y = null;
    private LocalDateTime creationDate = null;

    public TicketBuilder() {

    }

    public TicketBuilder(Ticket t) {
        this.name = t.getName();
        this.coordinates = t.getCoordinates();
        this.price = t.getPrice();
        this.type = t.getType();
        this.venueCapacity = t.getVenue().getCapacity();
        this.venueType = t.getVenue().getType();
        this.addressStreet = t.getVenue().getAddress().street();
        this.addressZipCode = t.getVenue().getAddress().zipCode();
        this.x = coordinates.x();
        this.y = coordinates.y();
    }

    public String setName(String name) {
        if (name.equals("")) return "Строка не может быть пустой";
        this.name = name;
        return "OK";
    }

    public String getName() {
        return name;
    }

    public String setX(String strX) {
        int x;
        if (strX.equals("")) return "Строка не может быть пустой";
        try {
            x = Integer.parseInt(strX);
        } catch (NumberFormatException e) {
            return "Ожидалось целое число типа int";
        }
        this.x = x;
        return "OK";
    }

    public Integer getX() {
        return x;
    }

    public String setY(String strY) {
        int y;
        if (strY.equals("")) return "Строка не может быть пустой";
        try {
            y = Integer.parseInt(strY);
        } catch (NumberFormatException e) {
            return "Ожидалось целое число типа int";
        }
        this.y = y;
        return "OK";
    }

    public Integer getY() {
        return y;
    }

    public String setPrice(String strPrice) {
        Integer price;
        if (strPrice.equals("")) return "Строка не может быть пустой";
        try {
            price = Integer.parseInt(strPrice);
        } catch (NumberFormatException e) {
            return "Ожидалось целое число типа int";
        }
        if (price <= 0) return "Поле должно быть больше нуля";
        this.price = price;
        return "OK";
    }

    public Integer getPrice() {
        return price;
    }

    public String setAddressStreet(String addressStreet) {
        if (addressStreet.equals("")) return "Строка не может быть пустой";
        this.addressStreet = addressStreet;
        return "OK";
    }

    public String getAddressStreet() {
        return addressStreet;
    }

    public String setAddressZipCode(String addressZipCode) {
        if (addressZipCode.equals("")) return "Строка не может быть пустой";
        this.addressZipCode = addressZipCode;
        return "OK";
    }

    public String getAddressZipCode() {
        return addressZipCode;
    }

    public String setType(String strType) {
        if (!validTicketType(strType)) {
            return "Неверный тип";
        }
        this.type = TicketType.valueOf(strType);
        return "OK";
    }

    public TicketType getType() {
        return type;
    }

    public String setVenueCapacity(String strVenueCapacity) {
        long venueCapacity;
        if (strVenueCapacity.equals("")) return "Строка не может быть пустой";
        try {
            venueCapacity = Long.parseLong(strVenueCapacity);
        } catch (NumberFormatException e) {
            return "Ожидалось целое число типа long";
        }
        if (venueCapacity <= 0) return "Поле должно быть больше нуля";
        this.venueCapacity = venueCapacity;
        return "OK";
    }

    public Long getVenueCapacity() {
        return venueCapacity;
    }

    public String setVenueType(String strVenueType) {
        if (!validVenueType(strVenueType)) {
            return "Неверный тип";
        }
        this.venueType = VenueType.valueOf(strVenueType);
        return "OK";
    }

    public VenueType getVenueType() {
        return venueType;
    }

    public void setCreationDate(LocalDateTime dt) {
        this.creationDate = dt;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * Сбрасывает все поля в null
     */
    public void clear() {
        id = null;
        name = null;
        coordinates = null;
        price = null;
        type = null;
        venueCapacity = null;
        venueType = null;
        addressStreet = null;
        addressZipCode = null;
        x = null;
        y = null;
        creationDate = null;
    }

    /**
     * Проверяет, что все поля заполнены
     */
    public boolean readyTCreate() {
        return (name != null &&
                coordinates != null &&
                price != null &&
                type != null &&
                venueCapacity != null &&
                venueType != null &&
                addressStreet != null &&
                addressZipCode != null &&
                x != null &&
                y != null);
    }

    /**
     * @return возвращает объект класса {@link Ticket}
     */
    public Ticket getTicket() {
        return new Ticket(id, name, new Coordinates(x, y), this.creationDate == null ? LocalDateTime.now() : creationDate, price, type, new Venue(id, name, venueCapacity, venueType, new Address(addressStreet, addressZipCode)));
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    /**
     * Проверяет, что строка соответствует одному из значений {@link VenueType}
     *
     * @param str строка
     * @return возвращает true если соответствует, false - если нет
     */
    private boolean validVenueType(String str) {
        for (VenueType c : VenueType.values()) {
            if (c.name().equals(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Проверяет, что строка соответствует одному из значений {@link TicketType}
     *
     * @param str строка
     * @return возвращает true если соответствует, false - если нет
     */
    private boolean validTicketType(String str) {
        for (TicketType c : TicketType.values()) {
            if (c.name().equals(str)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasId() {
        return id != null;
    }

    /**
     * Сравнивает объект с объектом типа ticket.Ticket {@link Ticket}
     * Сравнение происходит по полям {@link Ticket#type}({@link TicketType}), {@link Ticket#venue}({@link Venue}) и {@link Ticket#price}
     *
     * @param tb объект типа {@link TicketBuilder}, с которым производится сравнение
     */
    @Override
    public int compareTo(TicketBuilder tb) {
        int v = this.type.compareTo(tb.type) * -5;
        v += this.venueCapacity.compareTo(tb.venueCapacity);
        if (this.price > tb.price) v += 2;
        else if (this.price < tb.price) v--;
        return v;
    }
}

