package aiSafe.weatherdata.domain;

import aiSafe.aircontrolarea.domain.GeoBoundary;
import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class WeatherSubArea implements ValueObject {
    @Column(name = "subarea_min_lat", nullable = false)
    private double minLatitude;

    @Column(name = "subarea_max_lat", nullable = false)
    private double maxLatitude;

    @Column(name = "subarea_min_lon", nullable = false)
    private double minLongitude;

    @Column(name = "subarea_max_lon", nullable = false)
    private double maxLongitude;

    @Column(name = "subarea_min_alt", nullable = false)
    private double minAltitude;

    @Column(name = "subarea_max_alt", nullable = false)
    private double maxAltitude;

    protected WeatherSubArea() {
        // For ORM.
    }

    public WeatherSubArea(final double minLatitude, final double maxLatitude,
                          final double minLongitude, final double maxLongitude,
                          final double minAltitude, final double maxAltitude) {
        if (minLatitude >= maxLatitude) {
            throw new IllegalArgumentException("Sub-area latitude bounds are invalid.");
        }
        if (minLongitude >= maxLongitude) {
            throw new IllegalArgumentException("Sub-area longitude bounds are invalid.");
        }
        if (minAltitude >= maxAltitude) {
            throw new IllegalArgumentException("Sub-area altitude bounds are invalid.");
        }
        if (minAltitude < 0) {
            throw new IllegalArgumentException("Sub-area minimum altitude cannot be negative.");
        }

        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;
        this.minAltitude = minAltitude;
        this.maxAltitude = maxAltitude;
    }

    public boolean isWithinControlArea(final GeoBoundary boundary, final double controlAreaMaxAltitude) {
        if (boundary == null) {
            return false;
        }
        final double boundaryMinLat = boundary.coordinates().stream().mapToDouble(c -> c.latitude()).min().orElseThrow();
        final double boundaryMaxLat = boundary.coordinates().stream().mapToDouble(c -> c.latitude()).max().orElseThrow();
        final double boundaryMinLon = boundary.coordinates().stream().mapToDouble(c -> c.longitude()).min().orElseThrow();
        final double boundaryMaxLon = boundary.coordinates().stream().mapToDouble(c -> c.longitude()).max().orElseThrow();

        return minLatitude >= boundaryMinLat
                && maxLatitude <= boundaryMaxLat
                && minLongitude >= boundaryMinLon
                && maxLongitude <= boundaryMaxLon
                && minAltitude >= 0
                && maxAltitude <= controlAreaMaxAltitude;
    }

    public boolean overlapsVolumeWith(final WeatherSubArea other) {
        final boolean overlapsLat = Math.min(this.maxLatitude, other.maxLatitude) > Math.max(this.minLatitude, other.minLatitude);
        final boolean overlapsLon = Math.min(this.maxLongitude, other.maxLongitude) > Math.max(this.minLongitude, other.minLongitude);
        final boolean overlapsAlt = Math.min(this.maxAltitude, other.maxAltitude) > Math.max(this.minAltitude, other.minAltitude);

        return overlapsLat && overlapsLon && overlapsAlt;
    }

    public double minLatitude() {
        return minLatitude;
    }

    public double maxLatitude() {
        return maxLatitude;
    }

    public double minLongitude() {
        return minLongitude;
    }

    public double maxLongitude() {
        return maxLongitude;
    }

    public double minAltitude() {
        return minAltitude;
    }

    public double maxAltitude() {
        return maxAltitude;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof WeatherSubArea that)) return false;
        return Double.compare(that.minLatitude, minLatitude) == 0
                && Double.compare(that.maxLatitude, maxLatitude) == 0
                && Double.compare(that.minLongitude, minLongitude) == 0
                && Double.compare(that.maxLongitude, maxLongitude) == 0
                && Double.compare(that.minAltitude, minAltitude) == 0
                && Double.compare(that.maxAltitude, maxAltitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minLatitude, maxLatitude, minLongitude, maxLongitude, minAltitude, maxAltitude);
    }
}
