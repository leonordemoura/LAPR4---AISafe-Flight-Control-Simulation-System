package aiSafe.weatherdata.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class WindDirection implements ValueObject {
    @Column(name = "wind_direction", nullable = false)
    private double value;

    protected WindDirection() {
        // For ORM.
    }

    public WindDirection(final double value) {
        if (value < 0 || value >= 360) {
            throw new IllegalArgumentException("WindDirection must be between 0 (inclusive) and 360 (exclusive)");
        }
        this.value = value;
    }

    public double value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof WindDirection that)) return false;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
