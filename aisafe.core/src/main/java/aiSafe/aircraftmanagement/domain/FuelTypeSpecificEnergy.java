package aiSafe.aircraftmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class FuelTypeSpecificEnergy implements ValueObject, Serializable {
    @Column (nullable = false)
    private Double specificEnergy;

    protected FuelTypeSpecificEnergy() {
        // for ORM
    }

    public FuelTypeSpecificEnergy(final Double specificEnergy) {
        Preconditions.nonNull(specificEnergy, "Fuel type specific energy cannot be null.");
        Preconditions.ensure(specificEnergy > 0, "Fuel type specific energy must be greater than zero.");
        this.specificEnergy = specificEnergy;
    }

    public static FuelTypeSpecificEnergy valueOf(final Double specificEnergy) {
        return new FuelTypeSpecificEnergy(specificEnergy);
    }

    public Double specificEnergy() {
        return this.specificEnergy;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final FuelTypeSpecificEnergy that = (FuelTypeSpecificEnergy) o;
        return specificEnergy.equals(that.specificEnergy);
    }

    @Override
    public int hashCode() {
        return specificEnergy.hashCode();
    }
}
