package konser.service;

import konser.exception.SecurityException;

import konser.model.Authenticator;

import konser.model.User;

import konser.repository.UserRepository;

import java.sql.SQLException;

public class AuthService implements Authenticator {

    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users=users;
    }

@Override public User login(String username, String password) throws SecurityException {

    if(username==null || username.isBlank() || password==null || password.isBlank()) throw new SecurityException("Username dan password wajib diisi.");

    try {
        return users.authenticate(username.trim(),password).orElseThrow(() -> new SecurityException("Username atau password salah."));
    }

catch(SQLException e) {
    throw new SecurityException("Database gagal memproses login. Periksa koneksi PostgreSQL.");
}

}

}
