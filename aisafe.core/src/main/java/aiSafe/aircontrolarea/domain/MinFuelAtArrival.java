package aiSafe.aircontrolarea.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import eapli.framework.domain.model.ValueObject;
import java.util.Objects;

@Embeddable
public class MinFuelAtArrival implements ValueObject {
    @Column(name = "min_fuel_at_arrival", nullable = false)
    private double value;

    protected MinFuelAtArrival() {
        // For ORM.
    }

    public MinFuelAtArrival(final double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("MinFuelAtArrival must be positive");
        }
        this.value = value;
    }

    public double value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof MinFuelAtArrival that)) return false;
        return Double.compare(value, that.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
