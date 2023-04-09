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
    private final Connection conn;
    TicketVector tv = new TicketVector();

    public SQLTickets() throws IOException, SQLException {
        Properties info = new Properties();
        info.load(new FileInputStream("db.cfg"));
        conn = DriverManager.getConnection("jdbc:postgresql://pg:5432/studs", info);
        conn.setAutoCommit(false);
    }

    public void connectToBD() throws SQLException {
        try (Statement stat = conn.createStatement()) {
            try {
                stat.executeUpdate("CREATE TYPE venue_type AS enum ('PUB', 'BAR', 'OPEN_AREA');");
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            }
            try {
                stat.executeUpdate("CREATE TYPE ticket_type AS enum ('VIP', 'USUAL', 'BUDGETARY', 'CHEAP');");
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            }
            try (ResultSet rsA = stat.executeQuery("SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_name = 'address');")) {
                if (rsA.next()) if (!rsA.getBoolean("exists"))
                    stat.executeUpdate("CREATE TABLE address (" +
                            "id bigserial PRIMARY KEY, " +
                            "street text , " +
                            "zip_code text)");
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            }
            try (ResultSet rsC = stat.executeQuery("SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_name = 'coordinates');")) {
                if (rsC.next()) if (!rsC.getBoolean("exists"))
                    stat.executeUpdate("CREATE TABLE coordinates (" +
                            "id bigserial PRIMARY KEY, " +
                            "x integer, " +
                            "y integer)");
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            }
            try (ResultSet rsV = stat.executeQuery("SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_name = 'venue');")) {
                if (rsV.next()) if (!rsV.getBoolean("exists"))
                    stat.executeUpdate("CREATE TABLE venue (" +
                            "id bigserial PRIMARY KEY, " +
                            "name text, " +
                            "capacity bigint, " +
                            "type venue_type, " +
                            "address_id integer REFERENCES address(id) ON DELETE CASCADE)");
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            }
            try (ResultSet rsT = stat.executeQuery("SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_name = 'ticket');")) {
                if (rsT.next()) if (!rsT.getBoolean("exists"))
                    stat.executeUpdate("CREATE TABLE ticket (" +
                            "id bigserial PRIMARY KEY, " +
                            "name text, " +
                            "coordinates_id integer REFERENCES coordinates(id) ON DELETE CASCADE, " +
                            "creation_date timestamp NOT NULL DEFAULT NOW(), " +
                            "price integer, " +
                            "type ticket_type, " +
                            "venue_id integer REFERENCES venue(id) ON DELETE CASCADE," +
                            "user_name text REFERENCES users(name))");
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            }
        }
    }

    public String add(TicketBuilder tb, String userName) throws SQLException {
        try (PreparedStatement coordinatesStmt = conn.prepareStatement("INSERT INTO coordinates (x, y) VALUES (?, ?) RETURNING id");
             PreparedStatement addressStmt = conn.prepareStatement("INSERT INTO address (street, zip_code) VALUES (?, ?) RETURNING id");
             PreparedStatement venueStmt = conn.prepareStatement("INSERT INTO venue (name, capacity, type, address_id) VALUES (?, ?, ?, ?) RETURNING id");
             PreparedStatement ticketStmt = conn.prepareStatement("INSERT INTO ticket (name, coordinates_id, price, type, venue_id, user_name) VALUES (?, ?, ?, ?, ?, ?) RETURNING id, creation_date")) {
            Long cId = null;
            ResultSet rs;
            try {
                coordinatesStmt.setInt(1, tb.getX());
                coordinatesStmt.setInt(2, tb.getY());
                rs = coordinatesStmt.executeQuery();
                if (rs.next()) {
                    cId = rs.getLong("id");
                }
            } catch (SQLException e) {
                conn.rollback();
                return "Ошибка при попытке добавить объект в базу данных./" + e.getMessage();
            }
            Long aId = null;
            try {
                addressStmt.setString(1, tb.getAddressStreet());
                addressStmt.setString(2, tb.getAddressZipCode());
                rs = addressStmt.executeQuery();
                if (rs.next()) {
                    aId = rs.getLong("id");
                }
            } catch (SQLException e) {
                conn.rollback();
                return "Ошибка при попытке добавить объект в базу данных./" + e.getMessage();
            }
            Long vId = null;
            try {
                venueStmt.setString(1, tb.getName());
                venueStmt.setLong(2, tb.getVenueCapacity());
                venueStmt.setObject(3, tb.getVenueType().toString(), Types.OTHER);
                venueStmt.setLong(4, aId);
                rs = venueStmt.executeQuery();
                if (rs.next()) {
                    vId = rs.getLong("id");
                }
            } catch (SQLException e) {
                conn.rollback();
                return "Ошибка при попытке добавить объект в базу данных./" + e.getMessage();
            }
            Long tId = null;
            try {
                ticketStmt.setString(1, tb.getName());
                ticketStmt.setLong(2, cId);
                ticketStmt.setInt(3, tb.getPrice());
                ticketStmt.setObject(4, tb.getType().toString(), Types.OTHER);
                ticketStmt.setLong(5, vId);
                ticketStmt.setString(6, userName);
                rs = ticketStmt.executeQuery();
                if (rs.next()) {
                    tId = rs.getLong("id");
                    tb.setCreationDate(rs.getTimestamp("creation_date").toLocalDateTime());
                }
            } catch (SQLException e) {
                conn.rollback();
                return "Ошибка при попытке добавить объект в базу данных./" + e.getMessage();
            }
            conn.commit();
            tb.setId(tId);
            tv.add(tb.getTicket());
        } catch (SQLException e) {
            return "Ошибка при попытке добавить объект в базу данных./" + e.getMessage();
        }
        return "OK";
    }

    public String addIfMax(TicketBuilder tb, String userName) throws SQLException {
        Ticket maxT = tv.maxTicket();
        if (maxT == null) return add(tb, userName);
        if (tb.compareTo(new TicketBuilder(maxT)) > 0) return add(tb, userName);
        return "Объект не добавлен";
    }

    public String addIfMin(TicketBuilder tb, String userName) throws SQLException {
        Ticket minT = tv.minTicket();
        if (minT == null) return add(tb, userName);
        if (tb.compareTo(new TicketBuilder(minT)) < 0) return add(tb, userName);
        return "Объект не добавлен";
    }

    public String update(TicketBuilder tb, long id, String userName) throws SQLException {
        try {
            PreparedStatement ticketStatement = conn.prepareStatement("SELECT venue_id, coordinates_id, creation_date FROM ticket WHERE id = ? AND user_name = ?");
            ticketStatement.setLong(1, id);
            ticketStatement.setString(2, userName);
            ResultSet ticketRs = ticketStatement.executeQuery();

            if (ticketRs.next()) {
                long venueId = ticketRs.getLong("venue_id");
                long coordinatesId = ticketRs.getLong("coordinates_id");
                LocalDateTime creationDate = ticketRs.getTimestamp("creation_date").toLocalDateTime();

                PreparedStatement coordinatesStatement = conn.prepareStatement("UPDATE coordinates SET x = ?, y = ? WHERE id = ?");
                coordinatesStatement.setInt(1, tb.getX());
                coordinatesStatement.setInt(2, tb.getY());
                coordinatesStatement.setLong(3, coordinatesId);
                coordinatesStatement.executeUpdate();

                PreparedStatement venueStatement = conn.prepareStatement("UPDATE venue SET name = ?, capacity = ?, type = ? WHERE id = ?");
                venueStatement.setString(1, tb.getName());
                venueStatement.setLong(2, tb.getVenueCapacity());
                venueStatement.setObject(3, tb.getVenueType().toString(), Types.OTHER);
                venueStatement.setLong(4, venueId);
                venueStatement.executeUpdate();

                PreparedStatement addressStatement = conn.prepareStatement("UPDATE address SET street = ?, zip_code = ? WHERE id = (SELECT address_id FROM venue WHERE id = ?)");
                addressStatement.setString(1, tb.getAddressStreet());
                addressStatement.setString(2, tb.getAddressZipCode());
                addressStatement.setLong(3, venueId);
                addressStatement.executeUpdate();

                PreparedStatement ticketUpdateStatement = conn.prepareStatement("UPDATE ticket SET name = ?, price = ?, type = ? WHERE id = ?");
                ticketUpdateStatement.setString(1, tb.getName());
                ticketUpdateStatement.setInt(2, tb.getPrice());
                ticketUpdateStatement.setObject(3, tb.getType().toString(), Types.OTHER);
                ticketUpdateStatement.setLong(4, id);
                ticketUpdateStatement.executeUpdate();

                conn.commit();
                tb.setCreationDate(creationDate);
            } else {
                return "Вы не можете обновить данный объект.";
            }
        } catch (SQLException e) {
            conn.rollback();
            return "Ошибка SQL./" + e.getMessage();
        }
        tb.setId(id);
        tv.update(tb.getTicket(), id);
        return "OK";
    }

    public String clear(String userName) throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM ticket WHERE user_name = ?")) {
            preparedStatement.setString(1, userName);
            preparedStatement.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            return "Ошибка при очистке базы данных./" + e.getMessage();
        }
        tv.clear();
        return "OK";
    }

    public String remove(int index, String userName) throws SQLException {
        Long id = tv.getIdByIndex(index);
        if (id == -1) return index == 0 ? "Вектор пустой" : "Индекс выходит за границы вектора";
        try (PreparedStatement deleteStatement = conn.prepareStatement("DELETE FROM ticket WHERE id = ?");
             PreparedStatement selectStatement = conn.prepareStatement("SELECT EXISTS(SELECT * FROM ticket WHERE id = ? AND user_name = ?)")) {
            selectStatement.setLong(1, id);
            selectStatement.setString(2, userName);
            ResultSet rs = selectStatement.executeQuery();
            if (rs.next() && rs.getBoolean(1)) {
                deleteStatement.setLong(1, id);
                deleteStatement.executeUpdate();
            } else return "Вы не можете удалить данный объект.";
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            return "Ошибка при попытке удалить объект из базы данных./" + e.getMessage();
        }
        tv.remove(index);
        return "OK";
    }

    public List<Ticket> getAll() {
        return tv.getAll();
    }

    public String removeLower(TicketBuilder tb, String userName) throws SQLException {
        TicketBuilder tb1 = new TicketBuilder();
        List<Long> idL = new ArrayList<>();
        try (PreparedStatement selectIdStatement = conn.prepareStatement("SELECT ticket.id, price, capacity, ticket.type FROM ticket WHERE user_name = ? JOIN venue ON venue.id = ticket.venue_id");
             PreparedStatement deleteStatement = conn.prepareStatement("DELETE FROM ticket WHERE id = ?")) {
            selectIdStatement.setString(1, userName);
            ResultSet rsT = selectIdStatement.executeQuery();
            while (rsT.next()) {
                tb1.clear();
                Long tId = rsT.getLong("id");
                tb1.setPrice(String.valueOf(rsT.getInt("price")));
                tb1.setType(rsT.getString("type"));
                tb1.setVenueCapacity(rsT.getString("capacity"));
                if (tb1.compareTo(tb) < 0) idL.add(tId);
            }
            rsT.close();
            for (Long i : idL) {
                deleteStatement.setLong(1, i);
                deleteStatement.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            return "Ошибка при взаимодействии с базой данных./" + e.getMessage();
        }
        tb.setId(-1L);
        return String.valueOf(tv.removeLower(tb.getTicket()));
    }

    public String removeById(long id, String userName) {
        try (PreparedStatement deleteStatement = conn.prepareStatement("DELETE FROM ticket WHERE id = ?");
             PreparedStatement existsStatement = conn.prepareStatement("SELECT EXISTS(SELECT * FROM ticket WHERE id = ? AND user_name = ?)")) {
            existsStatement.setLong(1, id);
            existsStatement.setString(2, userName);
            ResultSet rs = existsStatement.executeQuery();
            if (rs.next() && rs.getBoolean(1)) {
                deleteStatement.setLong(1, id);
                deleteStatement.executeUpdate();
            } else return "Вы не можете удалить этот объект.";
        } catch (SQLException e) {
            return "Ошибка при попытке подключится к базе данных./" + e.getMessage();
        }
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
        String select_query =
                "SELECT ticket.id, name, creation_date, price, venue_type, x, y, capacity, type, street, zip_code FROM ticket " +
                        "JOIN (SELECT venue.id, capacity, venue.type AS venue_type, street, zip_code FROM venue JOIN address ON address.id = venue.address_id) AS svenue " +
                        "ON svenue.id = ticket.venue_id " +
                        "JOIN coordinates ON coordinates.id = ticket.coordinates_id";
        TicketBuilder tb = new TicketBuilder();
        try (PreparedStatement stat = conn.prepareStatement(select_query);
             ResultSet rsT = stat.executeQuery()) {
            while (rsT.next()) {
                tb.clear();
                tb.setId(rsT.getLong("id"));
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
            conn.commit();
            return "OK";
        } catch (SQLException e) {
            conn.rollback();
            return "Ошибка при чтении из базы данных. " + e.getMessage();
        }
    }
}
