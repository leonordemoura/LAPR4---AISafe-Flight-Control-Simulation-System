package aiSafe.weatherdata.importer;

import java.util.Objects;

public class WeatherDataImportResult {

    private final int lineNumber;
    private final boolean success;
    private final String errorMessage;

    public WeatherDataImportResult(final int lineNumber, final boolean success, final String errorMessage) {
        this.lineNumber = lineNumber;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static WeatherDataImportResult ok(final int lineNumber) {
        return new WeatherDataImportResult(lineNumber, true, null);
    }

    public static WeatherDataImportResult failure(final int lineNumber, final String reason) {
        return new WeatherDataImportResult(lineNumber, false, reason);
    }

    public int lineNumber() { return lineNumber; }
    public boolean success() { return success; }
    public String errorMessage() { return errorMessage; }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof WeatherDataImportResult that)) return false;
        return lineNumber == that.lineNumber
                && success == that.success
                && Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineNumber, success, errorMessage);
    }
}