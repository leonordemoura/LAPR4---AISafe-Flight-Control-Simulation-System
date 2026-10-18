package aiSafe.aircraftModel.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Objects;

@Embeddable
public class Weight {

    @Column(name = "weight_value")
    private double value;

    @Enumerated(EnumType.STRING)
    @Column(name = "weigth_unit")
    private WeightUnit unit;

    protected Weight() {}

    public Weight(double value, WeightUnit unit) {

        Preconditions.noneNull(unit, "Weight unit must be non-null");
        Preconditions.ensure(value >= 0, "Weight value must be non-negative");

        this.value = value;
        this.unit = unit;
    }

    public double value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Weight that)) return false;
        return Double.compare(value, that.value) == 0
                && unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }

}
