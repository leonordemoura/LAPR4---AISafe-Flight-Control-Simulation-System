package aiSafe.weatherdata.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Objects;

@Embeddable
public class WindSpeed implements ValueObject {
    @Column(name = "wind_speed", nullable = false)
    private double value;

    @Enumerated(EnumType.STRING)
    @Column(name = "wind_speed_unit", nullable = false)
    private WindSpeedUnit unit;

    protected WindSpeed() {
        // For ORM.
    }

    public WindSpeed(final double value, final WindSpeedUnit unit) {
        if (value < 0) {
            throw new IllegalArgumentException("WindSpeed cannot be negative");
        }
        if (unit == null) {
            throw new IllegalArgumentException("WindSpeed unit cannot be null");
        }
        this.value = value;
        this.unit = unit;
    }

    public double value() {
        return value;
    }

    public WindSpeedUnit unit() {
        return unit;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof WindSpeed windSpeed)) return false;
        return Double.compare(windSpeed.value, value) == 0 && unit.equals(windSpeed.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }
}
