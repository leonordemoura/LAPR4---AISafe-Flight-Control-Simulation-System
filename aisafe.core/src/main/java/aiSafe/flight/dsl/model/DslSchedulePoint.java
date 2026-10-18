package aiSafe.flight.dsl.model;

import java.util.Objects;

public final class DslSchedulePoint {

    private final String airportCode;
    private final String date;
    private final String time;
    private final int line;

    public DslSchedulePoint(
            final String airportCode,
            final String date,
            final String time,
            final int line) {
        this.airportCode = airportCode;
        this.date = date;
        this.time = time;
        this.line = line;
    }

    public String airportCode() {
        return airportCode;
    }

    public String date() {
        return date;
    }

    public String time() {
        return time;
    }

    public int line() {
        return line;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof DslSchedulePoint that)) return false;
        return line == that.line
                && Objects.equals(airportCode, that.airportCode)
                && Objects.equals(date, that.date)
                && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(airportCode, date, time, line);
    }
}
