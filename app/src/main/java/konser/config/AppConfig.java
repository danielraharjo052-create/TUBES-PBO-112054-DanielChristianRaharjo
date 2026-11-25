package konser.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AppConfig {

    private static final Properties PROPERTIES = loadProperties();

    public static final String HOST = getRequired("db.host");
    public static final String PORT = getRequired("db.port");
    public static final String DATABASE = getRequired("db.name");
    public static final String USER = getRequired("db.username");
    public static final String PASSWORD = getRequired("db.password");

    private AppConfig() {
        // mencegah objek konfigurasi dibuat
    }

    private static Properties loadProperties() {
        Path configPath = findConfigFile();
        Properties properties = new Properties();

        if (configPath == null) {
            throw new IllegalStateException(
                    "File config/database.properties tidak ditemukan. "
                    + "copy config/database.example.properties menjadi "
                    + "config/database.properties di folder utama project, "
                    + "lalu isi sesuai database lokal."
            );
        }

        try (InputStream input = Files.newInputStream(configPath)) {
            properties.load(input);
            return properties;
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "gagal membaca konfigurasi database: " + configPath, ex
            );
        }
    }

    private static Path findConfigFile() {
        // mencari dari folder kerja saat ini hingga folder induknya.
        // ini mendukung aplikasi yang dijalankan dari root project maupun modul app.
        Path current = Path.of("").toAbsolutePath();

        while (current != null) {
            Path candidate = current.resolve("config").resolve("database.properties");
            if (Files.exists(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }

        return null;
    }

    private static String getRequired(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Konfigurasi '" + key + "' belum diisi di config/database.properties."
            );
        }
        return value.trim();
    }
}
