package aiSafe.aircraftmanagement.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class MakerIDTest {

    @Test
    void ensureMakerIdIsCreated() {
        // Arrange
        final MakerID id = MakerID.valueOf(124L);

        // Act & Assert
        assertNotNull(id, "MakerID should not be null");
        assertEquals(124L, id.id(), "MakerID should have the correct value");
    }

    @Test
    void ensureMakerIdCannotBeNullOrNegative() {
        // Arrange
        Long nullId = null;
        Long negativeId = -1L;

        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> MakerID.valueOf(nullId), "Creating a MakerID with null value should throw IllegalArgumentException"),
                () -> assertThrows(IllegalArgumentException.class, () -> MakerID.valueOf(negativeId), "Creating a MakerID with null or negative value should throw IllegalArgumentException")
        );
    }

    @Test
    void ensureMakerIdEqualityHashCodeAndComparable() {
        // Arrange
        final MakerID a1 = MakerID.valueOf(124L);
        final MakerID a2 = MakerID.valueOf(124L);
        final MakerID b = MakerID.valueOf(125L);

        // Act & Assert
        assertEquals(a1, a2, "MakerIDs with same value should be equal");
        assertEquals(a1.hashCode(), a2.hashCode(), "Equal MakerIDs should have same hash code");
        assertNotEquals(a1, b, "MakerIDs with different values should not be equal");

        assertTrue(a1.compareTo(b) < 0, "MakerID with smaller value should be less than MakerID with larger value");
        assertTrue(b.compareTo(a1) > 0, "MakerID with larger value should be greater than MakerID with smaller value");
        assertEquals(0, a1.compareTo(a2), "MakerIDs with same value should be equal in comparison");
    }

    @Test
    void equals_same_instance() {
        // Arrange
        final MakerID id = MakerID.valueOf(10L);

        // Act & Assert
        assertEquals(id, id);
    }

    @Test
    void equals_false_for_null_or_different_type() {
        // Arrange
        final MakerID id = MakerID.valueOf(10L);

        // Act & Assert
        assertFalse(id.equals(null));
        assertFalse(id.equals(10L));
    }

    @Test
    void toString_returns_decimal_id() {
        // Arrange
        final MakerID id = MakerID.valueOf(42L);

        // Act & Assert
        assertEquals("42", id.toString());
    }

    @Test
    void protected_no_arg_constructor_is_exposed_for_jpa() throws Exception {
        final Constructor<MakerID> ctor = MakerID.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }
}

