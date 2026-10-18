package aiSafe.aircraftmanagement.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class TSFCTest {

    @Test
    void ensureTSFCIsCreated() {
        // Arrange
        final TSFC tsfc = TSFC.valueOf(0.5);

        // Act & Assert
        assertNotNull(tsfc, "TSFC should be created successfully");
        assertEquals(0.5, tsfc.value(), "TSFC value should be set correctly");
    }

    @Test
    void ensureTSFCCannotBeNull() {
        // Arrange
        Double nullTSFC = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> TSFC.valueOf(nullTSFC), "Creating a TSFC with null should throw an exception");
    }

    @Test
    void ensureTSFCMustBeGreaterThanZero() {
        // Arrange
        Double zeroTSFC = 0.0;
        Double negativeTSFC = -0.1;

        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> TSFC.valueOf(zeroTSFC), "Creating a TSFC with zero should throw an exception"),
                () -> assertThrows(IllegalArgumentException.class, () -> TSFC.valueOf(negativeTSFC), "Creating a TSFC with a negative value should throw an exception")
        );
    }

    @Test
    void ensureTSFCEqualityAndHashCode() {
        // Arrange
        final TSFC t1 = TSFC.valueOf(0.5);
        final TSFC t2 = TSFC.valueOf(0.5);
        final TSFC t3 = TSFC.valueOf(0.6);

        // Act & Assert
        assertEquals(t1, t2, "TSFCs with the same value should be equal");
        assertEquals(t1.hashCode(), t2.hashCode(), "TSFCs with the same value should have the same hash code");
        assertNotEquals(t1, t3, "TSFCs with different values should not be equal");
    }

    @Test
    void equals_same_instance() {
        // Arrange
        final TSFC t = TSFC.valueOf(0.4);

        // Act & Assert
        assertEquals(t, t);
    }

    @Test
    void equals_false_for_null_or_different_type() {
        // Arrange
        final TSFC t = TSFC.valueOf(0.4);

        // Act & Assert
        assertFalse(t.equals(null));
        assertFalse(t.equals(0.4));
    }

    @Test
    void protected_no_arg_constructor_is_exposed_for_jpa() throws Exception {
        final Constructor<TSFC> ctor = TSFC.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }
}