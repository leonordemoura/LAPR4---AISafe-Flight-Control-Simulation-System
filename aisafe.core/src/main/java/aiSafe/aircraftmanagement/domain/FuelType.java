package aiSafe.aircraftmanagement.domain;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

@Entity
public class FuelType implements AggregateRoot<Long> {
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    private FuelTypeName type;

    @Embedded
    private FuelTypeDensity density;

    @Embedded
    private FuelTypeSpecificEnergy specificEnergy;

    protected FuelType() {
        // for ORM
    }

    public FuelType(final FuelTypeName type, final FuelTypeDensity density, final FuelTypeSpecificEnergy specificEnergy) {
        Preconditions.nonEmpty(type.type(), "Fuel type name cannot be null.");
        Preconditions.noneNull(density, "Fuel type density cannot be null.");
        Preconditions.noneNull(specificEnergy, "Fuel type specific energy cannot be null.");
        Preconditions.ensure(density.density() > 0, "Fuel type density must be greater than zero.");
        Preconditions.ensure(specificEnergy.specificEnergy() > 0, "Fuel type specific energy must be greater than zero.");
        this.type = type;
        this.density = density;
        this.specificEnergy = specificEnergy;
    }

    public static FuelType valueOf(final FuelTypeName type, final FuelTypeDensity density, final FuelTypeSpecificEnergy specificEnergy) {
        return new FuelType(type, density, specificEnergy);
    }

    public FuelTypeName type() {
        return this.type;
    }

    public FuelTypeDensity density() {
        return this.density;
    }

    public FuelTypeSpecificEnergy specificEnergy() {
        return this.specificEnergy;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final FuelType that = (FuelType) o;
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean sameAs(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        final FuelType that = (FuelType) other;
        return type.equals(that.type);
    }

    @Override
    public Long identity() {
        return id;
    }
}
