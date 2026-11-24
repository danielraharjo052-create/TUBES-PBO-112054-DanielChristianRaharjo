package konser.service;

import java.sql.SQLException;

import konser.exception.SecurityException;
import konser.model.Authenticator;
import konser.model.User;
import konser.repository.UserRepositoryInterface;

public class AuthService implements Authenticator {
    private final UserRepositoryInterface users;
    public AuthService(UserRepositoryInterface users) { this.users = users; }

    @Override
    public User login(String username, String password) throws SecurityException {
        if (username == null || username.isBlank() || password == null || password.isBlank())
            throw new SecurityException("Username dan password wajib diisi.");
        try {
            return users.authenticate(username.trim(), password)
                    .orElseThrow(() -> new SecurityException("Username atau password salah."));
        } catch (SQLException e) {
            throw new SecurityException("Database gagal memproses login. Periksa koneksi PostgreSQL.");
        }
    }

    public void registerCustomer(String username, String password) throws SecurityException {
        if (username == null || !username.matches("[A-Za-z0-9_]{4,50}"))
            throw new SecurityException("Username harus 4-50 karakter (huruf, angka, underscore).");
        if (password == null || password.length() < 6)
            throw new SecurityException("Password minimal 6 karakter.");
        try {
            users.createUser(username.trim(), password, "CUSTOMER");
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) throw new SecurityException("Username sudah digunakan.");
            throw new SecurityException("Registrasi gagal. Periksa koneksi database.");
        }
    }
}
