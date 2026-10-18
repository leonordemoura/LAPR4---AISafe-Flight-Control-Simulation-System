package aiSafe.aircraftmanagement.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class FuelTypeDensityTest {

    @Test
    void ensureFuelTypeDensityIsCreatedSuccessfully() {
        // Arrange
        final Double densityValue = 0.84;

        // Act
        final FuelTypeDensity density = FuelTypeDensity.valueOf(densityValue);

        // Assert
        assertNotNull(density, "FuelTypeDensity should not be null");
        assertEquals(0.84, density.density(), "FuelTypeDensity should have the correct value");
    }

    @Test
    void ensureFuelTypeDensityCannotBeNull() {
        // Arrange
        final Double nullDensity = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                        FuelTypeDensity.valueOf(nullDensity),
                "Creating a FuelTypeDensity with null value should throw IllegalArgumentException");
    }

    @Test
    void ensureFuelTypeDensityCannotBeNegative() {
        // Arrange
        final Double negativeDensity = -0.84;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                        FuelTypeDensity.valueOf(negativeDensity),
                "Creating a FuelTypeDensity with negative value should throw IllegalArgumentException");
    }

    @Test
    void ensureFuelTypeDensityCannotBeZero() {
        // Arrange
        final Double zeroDensity = 0.0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                        FuelTypeDensity.valueOf(zeroDensity),
                "Creating a FuelTypeDensity with zero value should throw IllegalArgumentException");
    }

    @Test
    void ensureTwoFuelTypeDensitiesWithSameValueAreEqual() {
        // Arrange
        final FuelTypeDensity density1 = FuelTypeDensity.valueOf(0.84);
        final FuelTypeDensity density2 = FuelTypeDensity.valueOf(0.84);
        final FuelTypeDensity density3 = FuelTypeDensity.valueOf(0.72);

        // Act & Assert
        assertEquals(density1, density2, "FuelTypeDensities with the same value should be equal");
        assertEquals(density1.hashCode(), density2.hashCode(), "Equal FuelTypeDensities should have the same hash code");
        assertNotEquals(density1, density3, "FuelTypeDensities with different values should not be equal");
    }

    @Test
    void equals_false_for_null_or_different_type() {
        // Arrange
        final FuelTypeDensity d = FuelTypeDensity.valueOf(0.84);

        // Act & Assert
        assertFalse(d.equals(null));
        assertFalse(d.equals(0.84));
    }
}