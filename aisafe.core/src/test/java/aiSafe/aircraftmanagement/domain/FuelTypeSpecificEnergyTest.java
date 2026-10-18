package aiSafe.aircraftmanagement.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class FuelTypeSpecificEnergyTest {

    @Test
    void ensureFuelTypeSpecificEnergyIsCreatedSuccessfully() {
        // Arrange
        final Double specificEnergyValue = 42.8;

        // Act
        final FuelTypeSpecificEnergy specificEnergy = FuelTypeSpecificEnergy.valueOf(specificEnergyValue);

        // Assert
        assertNotNull(specificEnergy, "FuelTypeSpecificEnergy should not be null");
        assertEquals(42.8, specificEnergy.specificEnergy(), "FuelTypeSpecificEnergy should have the correct value");
    }

    @Test
    void ensureFuelTypeSpecificEnergyCannotBeNull() {
        // Arrange
        final Double nullSpecificEnergy = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                        FuelTypeSpecificEnergy.valueOf(nullSpecificEnergy),
                "Creating a FuelTypeSpecificEnergy with null value should throw IllegalArgumentException");
    }

    @Test
    void ensureFuelTypeSpecificEnergyCannotBeNegative() {
        // Arrange
        final Double negativeSpecificEnergy = -42.8;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                        FuelTypeSpecificEnergy.valueOf(negativeSpecificEnergy),
                "Creating a FuelTypeSpecificEnergy with negative value should throw IllegalArgumentException");
    }

    @Test
    void ensureFuelTypeSpecificEnergyCannotBeZero() {
        // Arrange
        final Double zeroSpecificEnergy = 0.0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                        FuelTypeSpecificEnergy.valueOf(zeroSpecificEnergy),
                "Creating a FuelTypeSpecificEnergy with zero value should throw IllegalArgumentException");
    }

    @Test
    void ensureTwoFuelTypeSpecificEnergiesWithSameValueAreEqual() {
        // Arrange
        final FuelTypeSpecificEnergy energy1 = FuelTypeSpecificEnergy.valueOf(42.8);
        final FuelTypeSpecificEnergy energy2 = FuelTypeSpecificEnergy.valueOf(42.8);
        final FuelTypeSpecificEnergy energy3 = FuelTypeSpecificEnergy.valueOf(44.0);

        // Act & Assert
        assertEquals(energy1, energy2, "FuelTypeSpecificEnergies with the same value should be equal");
        assertEquals(energy1.hashCode(), energy2.hashCode(), "Equal FuelTypeSpecificEnergies should have the same hash code");
        assertNotEquals(energy1, energy3, "FuelTypeSpecificEnergies with different values should not be equal");
    }

    @Test
    void equals_false_for_null_or_different_type() {
        // Arrange
        final FuelTypeSpecificEnergy e = FuelTypeSpecificEnergy.valueOf(42.8);

        // Act & Assert
        assertFalse(e.equals(null));
        assertFalse(e.equals(42.8));
    }

    @Test
    void protected_no_arg_constructor_is_exposed_for_jpa() throws Exception {
        final Constructor<FuelTypeSpecificEnergy> ctor = FuelTypeSpecificEnergy.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }
}