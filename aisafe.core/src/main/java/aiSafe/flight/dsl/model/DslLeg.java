package aiSafe.flight.dsl.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class DslLeg {

    private final DslSchedulePoint departure;
    private final DslSchedulePoint arrival;
    private final DslRoute route;
    private final List<DslSegment> segments;
    private final DslFuel fuel;
    private final int line;

    public DslLeg(
            final DslSchedulePoint departure,
            final DslSchedulePoint arrival,
            final DslRoute route,
            final List<DslSegment> segments,
            final DslFuel fuel,
            final int line) {
        this.departure = departure;
        this.arrival = arrival;
        this.route = route;
        this.segments = List.copyOf(segments);
        this.fuel = fuel;
        this.line = line;
    }

    public DslSchedulePoint departure() {
        return departure;
    }

    public DslSchedulePoint arrival() {
        return arrival;
    }

    public DslRoute route() {
        return route;
    }

    public List<DslSegment> segments() {
        return Collections.unmodifiableList(segments);
    }

    public DslFuel fuel() {
        return fuel;
    }

    public int line() {
        return line;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof DslLeg that)) return false;
        return line == that.line
                && Objects.equals(departure, that.departure)
                && Objects.equals(arrival, that.arrival)
                && Objects.equals(route, that.route)
                && Objects.equals(segments, that.segments)
                && Objects.equals(fuel, that.fuel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(departure, arrival, route, segments, fuel, line);
    }
}
