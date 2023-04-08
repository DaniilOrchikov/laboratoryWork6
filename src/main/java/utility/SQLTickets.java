package utility;

import ticket.Ticket;
import ticket.TicketBuilder;
import ticket.TicketType;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SQLTickets {
    private Connection conn;
    TicketVector tv = new TicketVector();

    public SQLTickets() throws IOException, SQLException {
        Properties info = new Properties();
        info.load(new FileInputStream("db.cfg"));
        conn = DriverManager.getConnection("jdbc:postgresql://pg:5432/studs", info);
        Statement stat = conn.createStatement();
        try {
            stat.executeUpdate("CREATE TYPE venue_type AS enum ('PUB', 'BAR', 'OPEN_AREA');");
        } catch (SQLException ignored) {
        }
        try {
            stat.executeUpdate("CREATE TYPE ticket_type AS enum ('VIP', 'USUAL', 'BUDGETARY', 'CHEAP');");
        } catch (SQLException ignored) {
        }
        ResultSet rsA = stat.executeQuery("SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_name = 'address');");
        if (rsA.next()) if (!rsA.getBoolean("exists"))
            stat.executeUpdate("CREATE TABLE address (" +
                    "id bigserial PRIMARY KEY, " +
                    "street text , " +
                    "zip_code text)");
        rsA.close();
        ResultSet rsC = stat.executeQuery("SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_name = 'coordinates');");
        if (rsC.next()) if (!rsC.getBoolean("exists"))
            stat.executeUpdate("CREATE TABLE coordinates (" +
                    "id bigserial PRIMARY KEY, " +
                    "x integer, " +
                    "y integer)");
        rsC.close();
        ResultSet rsV = stat.executeQuery("SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_name = 'venue');");
        if (rsV.next()) if (!rsV.getBoolean("exists"))
            stat.executeUpdate("CREATE TABLE venue (" +
                    "id bigserial PRIMARY KEY, " +
                    "name text, " +
                    "capacity bigint, " +
                    "type venue_type, " +
                    "address_id integer REFERENCES address(id) ON DELETE CASCADE)");
        rsV.close();
        ResultSet rsT = stat.executeQuery("SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_name = 'ticket');");
        if (rsT.next()) if (!rsT.getBoolean("exists"))
            stat.executeUpdate("CREATE TABLE ticket (" +
                    "id bigserial PRIMARY KEY, " +
                    "name text, " +
                    "coordinates_id integer REFERENCES coordinates(id) ON DELETE CASCADE, " +
                    "creation_date timestamp NOT NULL DEFAULT NOW(), " +
                    "price integer, " +
                    "type ticket_type, " +
                    "venue_id integer REFERENCES venue(id) ON DELETE CASCADE)");
        rsT.close();
        stat.close();
    }

    public String add(TicketBuilder tb) throws SQLException {
        Statement stat;
        try {
            stat = conn.createStatement();
        } catch (SQLException e) {
            return "Ошибка при попытке подключится к базе данных./" + e.getMessage();
        }
        Long cId = null;
        ResultSet rs;
        try {
            rs = stat.executeQuery("INSERT INTO coordinates (x, y) VALUES (%s, %s) RETURNING id".formatted(tb.getX(), tb.getY()));
            if (rs.next()) cId = rs.getLong("id");
        } catch (SQLException e) {
            return "Ошибка при попытке добавить объект в базу данных./" + e.getMessage();
        }
        Long aId = null;
        try {
            rs = stat.executeQuery("INSERT INTO address (street, zip_code) VALUES ('%s', '%s') RETURNING id".formatted(tb.getAddressStreet(), tb.getAddressZipCode()));
            if (rs.next()) aId = rs.getLong("id");
        } catch (SQLException e) {
            return "Ошибка при попытке добавить объект в базу данных./" + e.getMessage();
        }
        Long vId = null;
        try {
            rs = stat.executeQuery("INSERT INTO venue (name, capacity, type, address_id) VALUES ('%s', %s, '%s', %s) RETURNING id".formatted(tb.getName(), tb.getVenueCapacity(), tb.getVenueType(), aId));
            if (rs.next()) vId = rs.getLong("id");
        } catch (SQLException e) {
            return "Ошибка при попытке добавить объект в базу данных./" + e.getMessage();
        }
        Long tId = null;
        java.time.LocalDateTime timestamp;
        try {
            rs = stat.executeQuery("INSERT INTO ticket (name, coordinates_id, price, type, venue_id)" +
                    "VALUES ('%s', %s, '%s', '%s', %s) RETURNING id, creation_date".formatted(tb.getName(), cId, tb.getPrice(), tb.getType(), vId));
            if (rs.next()) {
                tId = rs.getLong("id");
                timestamp = rs.getTimestamp("creation_date").toLocalDateTime();
                tb.setCreationDate(timestamp);
            }
        } catch (SQLException e) {
            stat.close();
            return "Ошибка при попытке добавить объект в базу данных./" + e.getMessage();
        }
        tb.setId(tId);
        tv.add(tb.getTicket());
        return "OK";
    }

    public String addIfMax(TicketBuilder tb) throws SQLException {
        Ticket maxT = tv.maxTicket();
        if (maxT == null) return add(tb);
        if (tb.compareTo(new TicketBuilder(maxT)) > 0) return add(tb);
        return "Объект не добавлен";
    }

    public String addIfMin(TicketBuilder tb) throws SQLException {
        Ticket minT = tv.minTicket();
        if (minT == null) return add(tb);
        if (tb.compareTo(new TicketBuilder(minT)) < 0) return add(tb);
        return "Объект не добавлен";
    }

    public String update(TicketBuilder tb, long id) throws SQLException {
        Statement stat;
        try {
            stat = conn.createStatement();
        } catch (SQLException e) {
            return "Ошибка при попытке подключится к базе данных./" + e.getMessage();
        }
        Long vId, aId, cId;
        LocalDateTime timestamp = null;
        try {
            ResultSet rs = stat.executeQuery("SELECT venue_id, coordinates_id, creation_date FROM ticket WHERE id = %s".formatted(id));
            if (rs.next()) {
                vId = rs.getLong("venue_id");
                cId = rs.getLong("coordinates_id");
                timestamp = rs.getTimestamp("creation_date").toLocalDateTime();
                rs = stat.executeQuery("SELECT (address_id) FROM venue WHERE id = %s".formatted(vId));
                if (rs.next()) {
                    aId = rs.getLong("address_id");
                    stat.executeUpdate("UPDATE coordinates SET x = %s, y = %s WHERE id = %s".formatted(tb.getX(), tb.getY(), cId));
                    stat.executeUpdate("UPDATE venue SET name = '%s', capacity = %s, type = '%s' WHERE id = %s".formatted(tb.getName(), tb.getVenueCapacity(), tb.getVenueType(), vId));
                    stat.executeUpdate("UPDATE address SET street = '%s', zip_code = '%s' WHERE id = %s".formatted(tb.getAddressStreet(), tb.getAddressZipCode(), aId));
                    stat.executeUpdate("UPDATE ticket SET name = '%s', price = %s, type = '%s' WHERE id = %s".formatted(tb.getName(), tb.getPrice(), tb.getType(), id));
                }
            }
            rs.close();
        } catch (SQLException e) {
            stat.close();
            return "Ошибка SQL./" + e.getMessage();
        }
        tb.setId(id);
        tb.setCreationDate(timestamp);
        tv.update(tb.getTicket(), id);
        stat.close();
        return "OK";
    }

    public String clear() throws SQLException {
        Statement stat;
        try {
            stat = conn.createStatement();
        } catch (SQLException e) {
            return "Ошибка при попытке подключится к базе данных./" + e.getMessage();
        }
        try {
            stat.executeUpdate("DELETE FROM ticket");
        } catch (SQLException e) {
            stat.close();
            return "Ошибка при очистке базы данных./" + e.getMessage();
        }
        tv.clear();
        stat.close();
        return "OK";
    }

    public String remove(int index) throws SQLException {
        Long id = tv.getIdByIndex(index);
        if (id == -1) return index == 0 ? "Вектор пустой" : "Индекс выходит за границы вектора";
        Statement stat;
        try {
            stat = conn.createStatement();
        } catch (SQLException e) {
            return "Ошибка при попытке подключится к базе данных./" + e.getMessage();
        }
        try {
            stat.executeUpdate("DELETE FROM ticket WHERE id = %s".formatted(id));
        } catch (SQLException e) {
            stat.close();
            return "Ошибка при попытке удалить объект из базы данных./" + e.getMessage();
        }
        tv.remove(index);
        stat.close();
        return "OK";
    }

    public List<Ticket> getAll() {
        return tv.getAll();
    }

    public String removeLower(TicketBuilder tb) throws SQLException {
        Statement stat;
        try {
            stat = conn.createStatement();
        } catch (SQLException e) {
            return "Ошибка при попытке подключится к базе данных./" + e.getMessage();
        }
        TicketBuilder tb1 = new TicketBuilder();
        List<Long> idL = new ArrayList<>();
        try {
            ResultSet rsT = stat.executeQuery("SELECT ticket.id, price, capacity, type FROM ticket " +
                    "JOIN venue ON venue.id = ticket.venue_id");
            while (rsT.next()) {
                tb1.clear();
                Long tId = rsT.getLong("id");
                tb1.setPrice(String.valueOf(rsT.getInt("price")));
                tb1.setType(rsT.getString("type"));
                tb1.setVenueCapacity(rsT.getString("capacity"));
                if (tb1.compareTo(tb) < 0) idL.add(tId);
            }
            rsT.close();
            for (Long i :idL){
                stat.executeUpdate("DELETE FROM ticket WHERE id = %s".formatted(i));
            }
        } catch (SQLException e) {
            stat.close();
            return "Ошибка при взаимодействии с базой данных./" + e.getMessage();
        }
        return String.valueOf(tv.removeLower(tb.getTicket()));
    }

    public String removeById(long id) throws SQLException {
        Statement stat;
        try {
            stat = conn.createStatement();
        } catch (SQLException e) {
            return "Ошибка при попытке подключится к базе данных./" + e.getMessage();
        }
        stat.executeUpdate("DELETE FROM ticket WHERE id = %s".formatted(id));
        stat.close();
        tv.removeById(id);
        return "OK";
    }

    public String getMinByVenue() {
        return tv.getMinByVenue();
    }

    public List<Ticket> filterContainsName(String str) {
        return tv.filterContainsName(str);
    }

    public List<Ticket> filterLessThanPrice(int price) {
        return tv.filterLessThanPrice(price);
    }

    public List<Ticket> filterByPrice(int price) {
        return tv.filterByPrice(price);
    }

    public String getFieldAscendingType() {
        return tv.getFieldAscendingType();
    }

    public long getCountGreaterThanType(TicketType type) {
        return tv.getCountGreaterThanType(type);
    }

    public String getInfo() {
        return tv.getInfo();
    }

    public boolean validId(long id) {
        return tv.validId(id);
    }

    public void exit() throws SQLException {
        conn.close();
    }

    public String loadTickets() throws SQLException {
        Statement stat;
        try {
            stat = conn.createStatement();
        } catch (SQLException e) {
            return "Ошибка при попытке подключится к базе данных./" + e.getMessage();
        }
        TicketBuilder tb = new TicketBuilder();
        try {
            ResultSet rsT = stat.executeQuery("SELECT ticket.id, name, creation_date, price, venue_type, x, y, capacity, type, street, zip_code FROM ticket " +
                    "JOIN (SELECT venue.id, capacity, venue.type AS venue_type, street, zip_code FROM venue JOIN address ON address.id = venue.address_id) AS svenue " +
                    "ON svenue.id = ticket.venue_id " +
                    "JOIN coordinates ON coordinates.id = ticket.coordinates_id");
            while (rsT.next()) {
                tb.clear();
                Long tId = rsT.getLong("id");
                tb.setId(tId);
                tb.setCreationDate(rsT.getTimestamp("creation_date").toLocalDateTime());
                tb.setPrice(String.valueOf(rsT.getInt("price")));
                tb.setType(rsT.getString("type"));
                tb.setVenueCapacity(rsT.getString("capacity"));
                tb.setVenueType(rsT.getString("venue_type"));
                tb.setAddressStreet(rsT.getString("street"));
                tb.setAddressZipCode(rsT.getString("zip_code"));
                tb.setX(String.valueOf(rsT.getInt("x")));
                tb.setY(String.valueOf(rsT.getInt("y")));
                tv.add(tb.getTicket());
            }
            rsT.close();
        } catch (SQLException e) {
            stat.close();
            return "Ошибка при чтении из базы данных. " + e.getMessage();
        }
        stat.close();
        return "OK";
    }
}
