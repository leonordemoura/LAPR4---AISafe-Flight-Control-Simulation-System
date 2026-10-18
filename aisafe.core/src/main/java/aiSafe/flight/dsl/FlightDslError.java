package aiSafe.flight.dsl;

import java.util.Objects;

public final class FlightDslError {

    private final FlightDslErrorType type;
    private final String message;
    private final int line;
    private final int column;

    public FlightDslError(
            final FlightDslErrorType type,
            final String message,
            final int line,
            final int column) {
        this.type = type;
        this.message = message;
        this.line = line;
        this.column = column;
    }

    public FlightDslErrorType type() {
        return type;
    }

    public String message() {
        return message;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }

    public String format() {
        return typeLabel() + " Error [Line " + line + ", Column " + column + "]: " + message;
    }

    private String typeLabel() {
        return switch (type) {
            case LEXICAL -> "Lexical";
            case SYNTAX -> "Syntax";
            case SEMANTIC -> "Semantic";
        };
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof FlightDslError that)) return false;
        return line == that.line
                && column == that.column
                && type == that.type
                && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, message, line, column);
    }

    @Override
    public String toString() {
        return format();
    }
}
