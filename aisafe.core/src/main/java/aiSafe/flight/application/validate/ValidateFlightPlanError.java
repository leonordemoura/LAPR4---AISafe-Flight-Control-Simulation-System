package aiSafe.flight.application.validate;

public final class ValidateFlightPlanError {

    public final ValidateFlightPlanErrorCode code;
    public final String message;
    public final int line;
    public final int column;

    public ValidateFlightPlanError(final ValidateFlightPlanErrorCode code, final String message) {
        this(code, message, 0, 0);
    }

    public ValidateFlightPlanError(
            final ValidateFlightPlanErrorCode code,
            final String message,
            final int line,
            final int column
    ) {
        this.code = code;
        this.message = message;
        this.line = line;
        this.column = column;
    }

    public String format() {
        if (line > 0) {
            return message + " (line " + line + ", column " + column + ")";
        }
        return message;
    }
}
