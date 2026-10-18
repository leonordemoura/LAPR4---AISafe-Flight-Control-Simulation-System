package aiSafe.flight.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FuelQuantity implements ValueObject, Serializable {

    @Column(name = "fuel_quantity", nullable = false)
    private double value;

    @Column(name = "fuel_unit", nullable = false)
    private String unit;

    protected FuelQuantity() {
        // for ORM
    }

    public FuelQuantity(final double value, final String unit) {
        if (value <= 0) {
            throw new IllegalArgumentException("Fuel quantity must be strictly positive.");
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Fuel unit is required.");
        }
        this.value = value;
        this.unit = unit.trim();
    }

    public double value() {
        return value;
    }

    public String unit() {
        return unit;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof FuelQuantity that)) return false;
        return Double.compare(that.value, value) == 0 && Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }
}
