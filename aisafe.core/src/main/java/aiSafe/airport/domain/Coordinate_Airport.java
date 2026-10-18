package aiSafe.airport.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class Coordinate_Airport {

    @Column(name = "latitude")
    private double latitude;
    @Column(name = "longitude")
    private double longitude;

    protected Coordinate_Airport() {}

    public Coordinate_Airport(double latitude, double longitude) {

        Preconditions.ensure(latitude >= -90 && latitude <= 90, "Invalid latitude");
        Preconditions.ensure(longitude >= -180 && longitude <= 180, "Invalid longitude");

        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double latitude() {
        return latitude;
    }

    public double longitude() {
        return longitude;
    }
}
