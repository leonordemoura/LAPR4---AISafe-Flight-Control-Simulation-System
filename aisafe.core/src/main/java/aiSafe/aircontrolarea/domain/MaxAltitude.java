package aiSafe.aircontrolarea.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class MaxAltitude implements ValueObject {
    @Column(name = "max_altitude_meters", nullable = false)
    private double value;

    protected MaxAltitude() {
        // For ORM.
    }

    public MaxAltitude(final double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Max altitude must be greater than zero.");
        }
        this.value = value;
    }

    public double value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof MaxAltitude that)) return false;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
