package aiSafe.flight.dsl.model;

import java.util.Objects;

public final class DslSegment {

    private final DslCoordinates start;
    private final DslCoordinates end;
    private final double altitudeMeters;
    private final double widthKm;
    private final double windDirectionDeg;
    private final double windSpeed;
    private final String windUnit;
    private final int line;

    public DslSegment(
            final DslCoordinates start,
            final DslCoordinates end,
            final double altitudeMeters,
            final int line) {
        this(start, end, altitudeMeters, 10.0, 0.0, 0.0, "kt", line);
    }

    public DslSegment(
            final DslCoordinates start,
            final DslCoordinates end,
            final double altitudeMeters,
            final double widthKm,
            final double windDirectionDeg,
            final double windSpeed,
            final String windUnit,
            final int line) {
        this.start = start;
        this.end = end;
        this.altitudeMeters = altitudeMeters;
        this.widthKm = widthKm;
        this.windDirectionDeg = windDirectionDeg;
        this.windSpeed = windSpeed;
        this.windUnit = windUnit;
        this.line = line;
    }

    public DslCoordinates start() {
        return start;
    }

    public DslCoordinates end() {
        return end;
    }

    public double altitudeMeters() {
        return altitudeMeters;
    }

    public double widthKm() {
        return widthKm;
    }

    public double windDirectionDeg() {
        return windDirectionDeg;
    }

    public double windSpeed() {
        return windSpeed;
    }

    public String windUnit() {
        return windUnit;
    }

    public int line() {
        return line;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof DslSegment that)) return false;
        return line == that.line
                && Double.compare(that.altitudeMeters, altitudeMeters) == 0
                && Double.compare(that.widthKm, widthKm) == 0
                && Double.compare(that.windDirectionDeg, windDirectionDeg) == 0
                && Double.compare(that.windSpeed, windSpeed) == 0
                && Objects.equals(start, that.start)
                && Objects.equals(end, that.end)
                && Objects.equals(windUnit, that.windUnit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, altitudeMeters, widthKm, windDirectionDeg, windSpeed, windUnit, line);
    }
}
