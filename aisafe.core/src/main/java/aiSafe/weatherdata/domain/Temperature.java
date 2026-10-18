package aiSafe.weatherdata.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Objects;

@Embeddable
public class Temperature implements ValueObject {
    @Column(name = "temperature", nullable = false)
    private double value;

    @Enumerated(EnumType.STRING)
    @Column(name = "temperature_unit", nullable = false)
    private TemperatureUnit unit;

    protected Temperature() {
        // For ORM.
    }

    public Temperature(final double value, final TemperatureUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Temperature unit cannot be null");
        }
        this.value = value;
        this.unit = unit;
    }

    public double value() {
        return value;
    }

    public TemperatureUnit unit() {
        return unit;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Temperature that)) return false;
        return Double.compare(that.value, value) == 0 && unit.equals(that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }
}
