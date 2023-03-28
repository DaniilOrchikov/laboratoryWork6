package ticket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Класс, который поэтапно создает объект класса {@link Ticket}. При этом каждый метод возвращает строку "OK" если поле создано корректно и сообщение об ошибке, в противном случае
 */
public class TicketBuilder {
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


    public String setName(String name) {
        if (name.equals("")) return "Строка не может быть пустой";
        this.name = name;
        return "OK";
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

    public String setAddressStreet(String addressStreet) {
        if (addressStreet.equals("")) return "Строка не может быть пустой";
        this.addressStreet = addressStreet;
        return "OK";
    }

    public String setAddressZipCode(String addressZipCode) {
        if (addressZipCode.equals("")) return "Строка не может быть пустой";
        this.addressZipCode = addressZipCode;
        return "OK";
    }

    public String setType(String strType) {
        if (!validTicketType(strType)) {
            return "Неверный тип";
        }
        this.type = TicketType.valueOf(strType);
        return "OK";
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

    public String setVenueType(String strVenueType) {
        if (!validVenueType(strVenueType)) {
            return "Неверный тип";
        }
        this.venueType = VenueType.valueOf(strVenueType);
        return "OK";
    }

    public String setCreationDate(String strDate) {
        try {
            this.creationDate = LocalDateTime.parse(strDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e) {
            return "Неверный формат даты";
        }
        return "OK";
    }

    /**
     * Сбрасывает все поля в null
     */
    public void clear() {
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
        return new Ticket(name, new Coordinates(x, y), this.creationDate == null ? LocalDateTime.now() : creationDate, price, type, new Venue(name, venueCapacity, venueType, new Address(addressStreet, addressZipCode)));
    }

    /**
     * @param id явное указание id
     * @return возвращает объект класса {@link Ticket}
     */
    public Ticket getTicket(Long id) {
        return new Ticket(id, name, new Coordinates(x, y), this.creationDate == null ? LocalDateTime.now() : creationDate, price, type, new Venue(id, name, venueCapacity, venueType, new Address(addressStreet, addressZipCode)));
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
}
