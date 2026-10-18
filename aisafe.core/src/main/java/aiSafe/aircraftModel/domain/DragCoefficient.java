package aiSafe.aircraftModel.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class DragCoefficient {

    @Column(name = "cd")
    private double value;

    protected DragCoefficient() {}

    public DragCoefficient(double value) {

        Preconditions.ensure(value >= 0, "Cd must be non-negative");
        this.value = value;
    }

    public double value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DragCoefficient that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
