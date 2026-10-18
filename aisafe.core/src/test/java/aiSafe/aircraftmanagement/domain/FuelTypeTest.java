package aiSafe.aircraftmanagement.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class FuelTypeTest {

    @Test
    void ensureFuelTypeIsCreatedSuccessfully() {
        // Arrange
        FuelTypeName name = FuelTypeName.valueOf("JET A");
        FuelTypeDensity density = FuelTypeDensity.valueOf(0.84);
        FuelTypeSpecificEnergy specificEnergy = FuelTypeSpecificEnergy.valueOf(42.8);

        // Act
        FuelType fuel = FuelType.valueOf(name, density, specificEnergy);

        // Assert
        assertNotNull(fuel, "FuelType should be created successfully with valid attributes");
    }

    @Test
    void ensureFuelTypeNameCannotBeNull() {
        // Arrange
        FuelTypeDensity density = FuelTypeDensity.valueOf(0.84);
        FuelTypeSpecificEnergy specificEnergy = FuelTypeSpecificEnergy.valueOf(42.8);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                        FuelType.valueOf(FuelTypeName.valueOf(null), density, specificEnergy),
                "FuelType name cannot be null");
    }

    @Test
    void ensureFuelTypeDensityCannotBeNull() {
        // Arrange
        FuelTypeName name = FuelTypeName.valueOf("JET A");
        FuelTypeDensity density = null;
        FuelTypeSpecificEnergy specificEnergy = FuelTypeSpecificEnergy.valueOf(42.8);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                        FuelType.valueOf(name, density, specificEnergy),
                "FuelType density cannot be null");
    }

    @Test
    void ensureFuelTypeSpecificEnergyCannotBeNull() {
        // Arrange
        FuelTypeName name = FuelTypeName.valueOf("JET A");
        FuelTypeDensity density = FuelTypeDensity.valueOf(0.84);
        FuelTypeSpecificEnergy specificEnergy = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                        FuelType.valueOf(name, density, specificEnergy),
                "FuelType specific energy cannot be null");
    }

    @Test
    void ensureFuelTypeDensityCannotBeNegative() {
        // Arrange
        FuelTypeName name = FuelTypeName.valueOf("JET A");
        Double density = -0.84;
        FuelTypeSpecificEnergy specificEnergy = FuelTypeSpecificEnergy.valueOf(42.8);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                        FuelType.valueOf(name, FuelTypeDensity.valueOf(density), specificEnergy),
                "FuelType density cannot be negative");
    }

    @Test
    void ensureFuelTypeDensityCannotBeZero() {
        // Arrange
        FuelTypeName name = FuelTypeName.valueOf("JET A");
        Double density = 0.0;
        FuelTypeSpecificEnergy specificEnergy = FuelTypeSpecificEnergy.valueOf(42.8);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                        FuelType.valueOf(name, FuelTypeDensity.valueOf(density), specificEnergy),
                "FuelType density cannot be zero");
    }

    @Test
    void ensureFuelTypeSpecificEnergyCannotBeNegative() {
        // Arrange
        FuelTypeName name = FuelTypeName.valueOf("JET A");
        FuelTypeDensity density = FuelTypeDensity.valueOf(0.84);
        Double specificEnergy = -42.8;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                        FuelType.valueOf(name, density, FuelTypeSpecificEnergy.valueOf(specificEnergy)),
                "FuelType specific energy cannot be negative");
    }

    @Test
    void ensureFuelTypeSpecificEnergyCannotBeZero() {
        // Arrange
        FuelTypeName name = FuelTypeName.valueOf("JET A");
        FuelTypeDensity density = FuelTypeDensity.valueOf(0.84);
        Double specificEnergy = 0.0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                        FuelType.valueOf(name, density, FuelTypeSpecificEnergy.valueOf(specificEnergy)),
                "FuelType specific energy cannot be zero");
    }

    @Test
    void ensureTwoFuelTypesWithSameNameAreEqual() {
        // Arrange
        FuelType fuel1 = FuelType.valueOf(FuelTypeName.valueOf("JET A"), FuelTypeDensity.valueOf(0.84), FuelTypeSpecificEnergy.valueOf(42.8));
        FuelType fuel2 = FuelType.valueOf(FuelTypeName.valueOf("JET A"), FuelTypeDensity.valueOf(0.84), FuelTypeSpecificEnergy.valueOf(42.8));

        // Act & Assert
        assertEquals(fuel1, fuel2, "Two FuelTypes with the same name should be equal");
        assertEquals(fuel1.hashCode(), fuel2.hashCode(), "Two FuelTypes with the same name should have the same hash code");
        assertTrue(fuel1.sameAs(fuel2), "Two FuelTypes with the same name should be considered the same");
    }

    @Test
    void ensureTwoFuelTypesWithDifferentNamesAreNotEqual() {
        // Arrange
        FuelType jetA = FuelType.valueOf(FuelTypeName.valueOf("JET A"), FuelTypeDensity.valueOf(0.84), FuelTypeSpecificEnergy.valueOf(42.8));
        FuelType avgas = FuelType.valueOf(FuelTypeName.valueOf("AVGAS"), FuelTypeDensity.valueOf(0.72), FuelTypeSpecificEnergy.valueOf(44.0));

        // Act & Assert
        assertNotEquals(jetA, avgas, "Two FuelTypes with different names should not be equal");
        assertFalse(jetA.sameAs(avgas), "Two FuelTypes with different names should not be considered the same");
    }

    @Test
    void equals_same_instance() {
        // Arrange
        final FuelType jetA = FuelType.valueOf(
                FuelTypeName.valueOf("JET A"), FuelTypeDensity.valueOf(0.84), FuelTypeSpecificEnergy.valueOf(42.8));

        // Act & Assert
        assertEquals(jetA, jetA);
    }

    @Test
    void equals_false_for_null_or_different_type() {
        // Arrange
        final FuelType jetA = FuelType.valueOf(
                FuelTypeName.valueOf("JET A"), FuelTypeDensity.valueOf(0.84), FuelTypeSpecificEnergy.valueOf(42.8));

        // Act & Assert
        assertFalse(jetA.equals(null));
        assertFalse(jetA.equals("JET A"));
    }

    @Test
    void sameAs_false_for_null_or_different_type() {
        // Arrange
        final FuelType jetA = FuelType.valueOf(
                FuelTypeName.valueOf("JET A"), FuelTypeDensity.valueOf(0.84), FuelTypeSpecificEnergy.valueOf(42.8));

        // Act & Assert
        assertFalse(jetA.sameAs(null));
        assertFalse(jetA.sameAs("x"));
    }

    @Test
    void type_and_identity_exposed() {
        // Arrange
        final FuelTypeName name = FuelTypeName.valueOf("JET A");
        final FuelType jetA = FuelType.valueOf(name, FuelTypeDensity.valueOf(0.84), FuelTypeSpecificEnergy.valueOf(42.8));

        // Act & Assert
        assertEquals(name, jetA.type());
        assertNull(jetA.identity());
    }

    @Test
    void protected_no_arg_constructor_is_exposed_for_jpa() throws Exception {
        final Constructor<FuelType> ctor = FuelType.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }
}