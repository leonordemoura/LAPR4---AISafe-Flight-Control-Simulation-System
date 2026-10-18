package aiSafe.aircraftmanagement.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class FuelTypeNameTest {

    @Test
    void ensureFuelTypeNameIsCreatedSuccessfully() {
        // Arrange
        final String name = "JET A";

        // Act
        final FuelTypeName fuelTypeName = FuelTypeName.valueOf(name);

        // Assert
        assertNotNull(fuelTypeName, "FuelTypeName should not be null");
        assertEquals("JET A", fuelTypeName.type(), "FuelTypeName should have the correct value");
    }

    @Test
    void ensureFuelTypeNameCannotBeNull() {
        // Arrange
        final String nullName = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                        FuelTypeName.valueOf(nullName),
                "Creating a FuelTypeName with null value should throw IllegalArgumentException");
    }

    @Test
    void ensureFuelTypeNameCannotBeBlank() {
        // Arrange
        final String blankName = "   ";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                        FuelTypeName.valueOf(blankName),
                "Creating a FuelTypeName with blank value should throw IllegalArgumentException");
    }

    @Test
    void ensureTwoFuelTypeNamesWithSameValueAreEqual() {
        // Arrange
        final FuelTypeName name1 = FuelTypeName.valueOf("JET A");
        final FuelTypeName name2 = FuelTypeName.valueOf("JET A");
        final FuelTypeName name3 = FuelTypeName.valueOf("AVGAS");

        // Act & Assert
        assertEquals(name1, name2, "FuelTypeNames with the same value should be equal");
        assertEquals(name1.hashCode(), name2.hashCode(), "Equal FuelTypeNames should have the same hash code");
        assertNotEquals(name1, name3, "FuelTypeNames with different values should not be equal");
    }

    @Test
    void equals_same_instance() {
        // Arrange
        final FuelTypeName name = FuelTypeName.valueOf("JET A");

        // Act & Assert
        assertEquals(name, name);
    }

    @Test
    void equals_false_for_null_or_different_type() {
        // Arrange
        final FuelTypeName name = FuelTypeName.valueOf("JET A");

        // Act & Assert
        assertFalse(name.equals(null));
        assertFalse(name.equals("JET A"));
    }

    @Test
    void protected_no_arg_constructor_is_exposed_for_jpa() throws Exception {
        final Constructor<FuelTypeName> ctor = FuelTypeName.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }
}