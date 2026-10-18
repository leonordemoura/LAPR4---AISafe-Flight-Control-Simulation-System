package aiSafe.flight.application.importplan;

import java.util.Objects;

public final class ImportFlightPlanError {

    private final ImportFlightPlanErrorCode code;
    private final String message;
    private final Integer line;
    private final Integer column;

    public ImportFlightPlanError(
            final ImportFlightPlanErrorCode code,
            final String message) {
        this(code, message, null, null);
    }

    public ImportFlightPlanError(
            final ImportFlightPlanErrorCode code,
            final String message,
            final Integer line,
            final Integer column) {
        this.code = Objects.requireNonNull(code);
        this.message = Objects.requireNonNull(message);
        this.line = line;
        this.column = column;
    }

    public ImportFlightPlanErrorCode code() {
        return code;
    }

    public String message() {
        return message;
    }

    public Integer line() {
        return line;
    }

    public Integer column() {
        return column;
    }

    public String format() {
        if (line != null && column != null) {
            return code + " [Line " + line + ", Column " + column + "]: " + message;
        }
        return code + ": " + message;
    }
}
