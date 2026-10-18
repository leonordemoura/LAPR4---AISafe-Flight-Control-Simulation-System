package aiSafe.flight.dsl.model;

import java.util.Objects;

public final class DslCoordinates {

    private final double latitude;
    private final double longitude;

    public DslCoordinates(final double latitude, final double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double latitude() {
        return latitude;
    }

    public double longitude() {
        return longitude;
    }

    public boolean samePosition(final DslCoordinates other) {
        return other != null
                && Double.compare(latitude, other.latitude) == 0
                && Double.compare(longitude, other.longitude) == 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof DslCoordinates that)) return false;
        return Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "(" + latitude + ", " + longitude + ")";
    }
}
