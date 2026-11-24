package konser.config;

public final class AppConfig {

    public static final String HOST = "localhost";
    public static final String PORT = "5432";
    public static final String DATABASE = "database_tiket";

    public static final String USER = "postgres";
    public static final String PASSWORD = "2007";

    private AppConfig() {
        // mencegah objek konfigurasi dibuat
    }
}
