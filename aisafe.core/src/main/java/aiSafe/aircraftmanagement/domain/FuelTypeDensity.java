package aiSafe.aircraftmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class FuelTypeDensity implements Serializable, ValueObject {
    @Column (nullable = false)
    private Double density;

    protected FuelTypeDensity() {
        // for ORM
    }

    public FuelTypeDensity(final Double density) {
        Preconditions.noneNull(density, "Fuel type density cannot be null.");
        Preconditions.ensure(density > 0, "Fuel type density must be greater than zero.");
        this.density = density;
    }

    public static FuelTypeDensity valueOf(final Double density) {
        return new FuelTypeDensity(density);
    }

    public Double density() {
        return this.density;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final FuelTypeDensity that = (FuelTypeDensity) o;
        return density.equals(that.density);
    }

    @Override
    public int hashCode() {
        return density.hashCode();
    }
}
