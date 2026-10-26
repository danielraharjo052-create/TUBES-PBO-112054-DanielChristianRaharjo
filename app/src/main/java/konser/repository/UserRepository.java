package konser.repository;

import konser.config.DatabaseConnection;

import konser.factory.UserFactoryProvider;

import konser.model.User;

import java.sql.*;

import java.util.*;

public class UserRepository {

    private final DatabaseConnection db;

    public UserRepository(DatabaseConnection db) {
        this.db = db;
    }

public Optional<User> authenticate(String username, String password) throws SQLException {

    String sql = "SELECT id, username, role FROM users WHERE username = ? AND password_hash = ?";

    try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setString(1, username);

        ps.setString(2, PasswordUtil.sha256(password));

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return Optional.of(UserFactoryProvider.forRole(rs.getString("role")).createUser(rs.getInt("id"), rs.getString("username")));

        }

}

return Optional.empty();

}

public List<String> findAllUsers() throws SQLException {

    List<String> rows = new ArrayList<>();

    try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT id, username, role FROM users ORDER BY id"); ResultSet rs = ps.executeQuery()) {

        while (rs.next()) rows.add(rs.getInt("id") + " | " + rs.getString("username") + " | " + rs.getString("role"));

    }

return rows;

}

public void createUser(String username, String password, String role) throws SQLException {

    String sql = "INSERT INTO users(username, password_hash, role) VALUES (?, ?, ?)";

    try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

        ps.setString(1, username);
        ps.setString(2, PasswordUtil.sha256(password));
        ps.setString(3, role);
        ps.executeUpdate();

    }

}

}
