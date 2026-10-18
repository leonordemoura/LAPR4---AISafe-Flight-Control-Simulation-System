package aiSafe.aircraftModel.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class LiftCoefficient {

    @Column(name = "cl")
    private double value;

    protected LiftCoefficient() {}

    public LiftCoefficient(double value) {
        Preconditions.ensure(value >= 0, "cl value must be non-negative");
        this.value = value;
    }

    public double value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LiftCoefficient that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

}