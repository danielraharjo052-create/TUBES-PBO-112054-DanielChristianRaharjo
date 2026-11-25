package konser.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static DatabaseConnection instance;

    private final String url;
    private final String username;
    private final String password;

    private DatabaseConnection() {
        url = "jdbc:postgresql://"
                + AppConfig.HOST
                + ":"
                + AppConfig.PORT
                + "/"
                + AppConfig.DATABASE;

        username = AppConfig.USER;
        password = AppConfig.PASSWORD;
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }

        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public void testConnection() throws SQLException {
        try (Connection connection = getConnection()) {
            // kalau koneksi berhasil, tidak perlu melakukan apapun
        }
    }
}
