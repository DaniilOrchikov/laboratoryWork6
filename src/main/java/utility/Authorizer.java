package utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class Authorizer {
    private final Connection conn;

    public Authorizer() throws IOException, SQLException {
        Properties info = new Properties();
        info.load(new FileInputStream("db.cfg"));
        conn = DriverManager.getConnection("jdbc:postgresql://pg:5432/studs", info);
        conn.setAutoCommit(false);
        try (Statement stat = conn.createStatement()) {
            ResultSet rsV = stat.executeQuery("SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_name = 'users')");
            if (rsV.next() && !rsV.getBoolean(1)) {
                stat.executeUpdate("CREATE TABLE users (name text PRIMARY KEY, password text, salt char(10))");
                conn.commit();
            }
        } catch (SQLException e) {
            conn.rollback();
        }
    }

    public String addUser(String name, String password) throws SQLException {
        String salt = RandomTextGenerator.generate(10);
        password = PasswordHasher.hashPassword(password, salt);
        try (PreparedStatement userStmt = conn.prepareStatement("INSERT INTO users VALUES (?, ?, ?)");
             PreparedStatement userExist = conn.prepareStatement("SELECT EXISTS(SELECT * FROM users WHERE name = ?)")) {
            userExist.setString(1, name);
            ResultSet rs = userExist.executeQuery();
            if (rs.next() && rs.getBoolean(1)) {
                return "Пользователь с таким именем уже существует.";
            }
            userStmt.setString(1, name);
            userStmt.setString(2, password);
            userStmt.setString(3, salt);
            userStmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            return "Не удалось подключиться к БД./" + e.getMessage();
        }
        return "OK";
    }

    public String authorize(String name, String password) {
        try (PreparedStatement userStmt = conn.prepareStatement("SELECT salt, password FROM users WHERE name = ?")) {
            userStmt.setString(1, name);
            ResultSet rs = userStmt.executeQuery();
            if (rs.next()) {
                String salt = rs.getString("salt");
                String userPassword = rs.getString("password");
                return userPassword.equals(PasswordHasher.hashPassword(password, salt)) ? "OK" : "Неверный пароль";
            }
            return "Пользователя с таким именем не существует. Для создания аккаунта введите команду sign_up";
        } catch (SQLException e) {
            return "Не удалось подключится к базе данных/" + e.getMessage();
        }
    }
}
