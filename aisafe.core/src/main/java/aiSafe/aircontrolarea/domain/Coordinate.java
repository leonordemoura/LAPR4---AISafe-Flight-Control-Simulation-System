package aiSafe.aircontrolarea.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import eapli.framework.domain.model.ValueObject;
import java.util.Objects;

@Embeddable
public class Coordinate implements ValueObject {
    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    protected Coordinate() {
        // For ORM.
    }

    public Coordinate(final double latitude, final double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double latitude() {
        return latitude;
    }

    public double longitude() {
        return longitude;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate that)) return false;
        return Double.compare(latitude, that.latitude) == 0
                && Double.compare(longitude, that.longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
}
