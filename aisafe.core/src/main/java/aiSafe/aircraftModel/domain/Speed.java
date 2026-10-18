package aiSafe.aircraftModel.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Objects;

@Embeddable
public class Speed {
    @Column(name = "speed")
    private double value;

    @Enumerated(EnumType.STRING)
    @Column(name = "speed_unit")
    private SpeedUnit unit;

    protected Speed() {}

    public Speed(double value, SpeedUnit unit) {

        Preconditions.noneNull(unit, "Speed unit must be non-null");
        Preconditions.ensure(value >= 0, "Speed value must be non-negative");

        this.value = value;
        this.unit = unit;
    }

    public double value() {
        return value;
    }

    public SpeedUnit unit() {
        return unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Speed that)) return false;
        return Double.compare(value, that.value) == 0
                && unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }

}
