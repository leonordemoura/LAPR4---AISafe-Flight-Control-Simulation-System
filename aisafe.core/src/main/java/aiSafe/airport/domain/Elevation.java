package aiSafe.airport.domain;


import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class Elevation {

    @Column(name = "elevation")
    private Double elevation;

    protected Elevation() {
        // for ORM
    }

    public Elevation(Double elevation) {

        Preconditions.ensure(elevation != null, "Elevation must not be null");
        this.elevation = elevation;
    }

    public Double value(){return elevation;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Elevation that)) return false;
        return Objects.equals(elevation, that.elevation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elevation);
    }

    @Override
    public String toString() {
        return elevation.toString();
    }
}
