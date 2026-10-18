package aiSafe.aircraft.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class FlightCrewSize implements ValueObject, Serializable {

    @Column(nullable = false)
    private int value;

    protected FlightCrewSize() {
        // for ORM
    }

    private FlightCrewSize(final int value) {
        this.value = value;
    }

    public static FlightCrewSize valueOf(final int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Flight crew size must be greater than zero.");
        }
        return new FlightCrewSize(value);
    }

    public int value() { return value; }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return value == ((FlightCrewSize) o).value;
    }

    @Override
    public int hashCode() { return Integer.hashCode(value); }
}