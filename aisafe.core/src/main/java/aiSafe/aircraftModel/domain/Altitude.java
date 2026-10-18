package aiSafe.aircraftModel.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Objects;

@Embeddable
public class Altitude {

    @Column(name = "altitude_value")
    private double value;

    @Enumerated(EnumType.STRING)
    @Column(name = "altitude_unit")
    private AltitudeUnit unit;

    protected Altitude() {}

    public Altitude(double value, AltitudeUnit unit) {

        Preconditions.ensure(value >= 0, "Altitude value must be non-negative");
        Preconditions.noneNull(unit, "Altitude unit must be non-null");

        this.value = value;
        this.unit = unit;
    }

    public double value() { return value; }
    public AltitudeUnit unit() { return unit; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Altitude that)) return false;
        return Double.compare(value, that.value) == 0
                && unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }

}
