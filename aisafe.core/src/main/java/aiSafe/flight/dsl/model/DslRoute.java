package aiSafe.flight.dsl.model;

import java.util.Objects;

public final class DslRoute {

    private final String origin;
    private final String destination;
    private final int line;

    public DslRoute(final String origin, final String destination, final int line) {
        this.origin = origin;
        this.destination = destination;
        this.line = line;
    }

    public String origin() {
        return origin;
    }

    public String destination() {
        return destination;
    }

    public int line() {
        return line;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof DslRoute that)) return false;
        return line == that.line
                && Objects.equals(origin, that.origin)
                && Objects.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, destination, line);
    }
}
