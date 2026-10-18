package aiSafe.weatherdata.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Objects;

@Embeddable
public class Pressure implements ValueObject {
    @Column(name = "pressure", nullable = false)
    private double value;

    @Enumerated(EnumType.STRING)
    @Column(name = "pressure_unit", nullable = false)
    private PressureUnit unit;

    protected Pressure() {
        // For ORM.
    }

    public Pressure(final double value, final PressureUnit unit) {
        if (value <= 0) {
            throw new IllegalArgumentException("Pressure must be greater than zero");
        }
        if (unit == null) {
            throw new IllegalArgumentException("Pressure unit cannot be null");
        }
        this.value = value;
        this.unit = unit;
    }

    public double value() {
        return value;
    }

    public PressureUnit unit() {
        return unit;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Pressure pressure)) return false;
        return Double.compare(pressure.value, value) == 0 && unit.equals(pressure.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }
}
