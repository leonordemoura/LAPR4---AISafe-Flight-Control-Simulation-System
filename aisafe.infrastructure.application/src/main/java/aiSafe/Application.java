package aiSafe;

public final class Application {
    public static final String VERSION = "1.0.0";
    public static final String COPYRIGHT = "(C) AISafe Team";

    private static final AppSettings SETTINGS = new AppSettings();

    private Application() {
        // Utility class.
    }

    public static AppSettings settings() {
        return SETTINGS;
    }
}
