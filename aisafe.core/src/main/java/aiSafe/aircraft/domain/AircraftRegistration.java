package aiSafe.aircraft.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class AircraftRegistration implements ValueObject, Serializable, Comparable<AircraftRegistration> {

    @Column(name = "registration", nullable = false, updatable = false)
    private String value;

    protected AircraftRegistration() {}

    private AircraftRegistration(final String value) {
        Preconditions.nonEmpty(value, "Aircraft registration cannot be null or empty");
        this.value = value.trim().toUpperCase();
    }

    public static AircraftRegistration valueOf(final String value) {
        return new AircraftRegistration(value);
    }

    public String value() { return value; }

    @Override
    public int compareTo(final AircraftRegistration other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AircraftRegistration that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return value.hashCode(); }

    @Override
    public String toString() { return value; }
}