package aiSafe.aircraftmanagement.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class MakerNameTest {

    @Test
    void ensureMakerNameIsCreated() {
        // Arrange
        final MakerName name = MakerName.valueOf("GE Aviation");

        // Act & Assert
        assertNotNull(name, "MakerName should not be null");
        assertEquals("GE Aviation", name.name(), "MakerName should be 'GE Aviation'");
    }

    @Test
    void ensureMakerNameCannotBeNullOrBlank() {
        // Arrange
        String nullName = null;
        String blankName = "   ";

        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> MakerName.valueOf(nullName), "Creating a MakerName with null should throw IllegalArgumentException"),
                () -> assertThrows(IllegalArgumentException.class, () -> MakerName.valueOf(blankName), "Creating a MakerName with blank should throw IllegalArgumentException")
        );
    }

    @Test
    void ensureMakerNameEqualityAndHashCode() {
        // Arrange
        final MakerName a1 = MakerName.valueOf("GE Aviation");
        final MakerName a2 = MakerName.valueOf("GE Aviation");
        final MakerName b = MakerName.valueOf("Rolls-Royce");

        // Act & Assert
        assertEquals(a1, a2, "MakerNames with same name should be equal");
        assertEquals(a1.hashCode(), a2.hashCode(), "Equal MakerNames should have same hash code");
        assertNotEquals(a1, b, "MakerNames with different names should not be equal");
    }

    @Test
    void toString_returns_raw_name() {
        // Arrange
        final MakerName name = MakerName.valueOf("Rolls-Royce");

        // Act & Assert
        assertEquals("Rolls-Royce", name.toString());
    }

    @Test
    void equals_false_for_null_or_different_type() {
        // Arrange
        final MakerName name = MakerName.valueOf("Acme");

        // Act & Assert
        assertFalse(name.equals(null));
        assertFalse(name.equals("Acme"));
    }

    @Test
    void protected_no_arg_constructor_is_exposed_for_jpa() throws Exception {
        final Constructor<MakerName> ctor = MakerName.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }
}