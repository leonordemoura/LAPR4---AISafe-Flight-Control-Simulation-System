package aiSafe.aircraftmanagement.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class PowerTest {

    @Test
    void ensurePowerIsCreated() {
        // Arrange
        final Power power = Power.valueOf(27000, Unit.HORSEPOWER);

        // Act & Assert
        assertNotNull(power, "Power should be created successfully");
        assertEquals(27000, power.value(), "Power value should be set correctly");
        assertEquals(Unit.HORSEPOWER, power.unit(), "Power unit should be set correctly");
    }

    @Test
    void ensurePowerValueCannotBeNull() {
        // Arrange
        Integer value = null;
        Unit unit = Unit.HORSEPOWER;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> Power.valueOf(value, unit));
    }

    @Test
    void ensurePowerValueCannotBeNegative() {
        // Arrange
        Integer negativeValue = -1;
        Unit unit = Unit.HORSEPOWER;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> Power.valueOf(negativeValue, unit), "Creating a Power with a negative value should throw an exception");
    }

    @Test
    void ensurePowerUnitCannotBeNull() {
        // Arrange
        Integer value = 27000;
        Unit unit = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> Power.valueOf(value, unit));
    }

    @Test
    void ensurePowerEqualityAndHashCode() {
        // Arrange
        final Power p1 = Power.valueOf(100, Unit.HORSEPOWER);
        final Power p2 = Power.valueOf(100, Unit.HORSEPOWER);
        final Power p3 = Power.valueOf(101, Unit.HORSEPOWER);
        final Power p4 = Power.valueOf(100, Unit.KILOWATT);

        // Act & Assert
        assertEquals(p1, p2, "Powers with the same value and unit should be equal");
        assertEquals(p1.hashCode(), p2.hashCode(), "Powers with the same value and unit should have the same hash code");
        assertNotEquals(p1, p3, "Powers with different values should not be equal");
        assertNotEquals(p1, p4, "Powers with different units should not be equal");
    }

    @Test
    void equals_same_instance() {
        // Arrange
        final Power p = Power.valueOf(50, Unit.KILOWATT);

        // Act & Assert
        assertEquals(p, p);
    }

    @Test
    void equals_false_for_null_or_different_type() {
        // Arrange
        final Power p = Power.valueOf(50, Unit.KILOWATT);

        // Act & Assert
        assertFalse(p.equals(null));
        assertFalse(p.equals("50"));
    }

    @Test
    void protected_no_arg_constructor_is_exposed_for_jpa() throws Exception {
        final Constructor<Power> ctor = Power.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }
}