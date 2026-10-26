package konser.config;

/**
 * Konfigurasi koneksi database aplikasi.
 * Ubah nilai di bawah ini sesuai pengaturan PostgreSQL milikmu.
 */
public final class AppConfig {

    public static final String HOST = "localhost";
    public static final String PORT = "5432";
    public static final String DATABASE = "database_tiket";

    public static final String USER = "postgres";
    public static final String PASSWORD = "2007";

    private AppConfig() {
        // Mencegah objek konfigurasi dibuat.
    }
}
