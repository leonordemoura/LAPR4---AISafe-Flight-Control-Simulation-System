package aiSafe.aircraftmanagement.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class EngineModelNameTest {

    @Test
    void ensureEngineModelNameIsCreated() {
        // Arrange
        final EngineModelName name = EngineModelName.valueOf("GE90B");

        // Act & Assert
        assertNotNull(name, "Name should not be null");
        assertEquals("GE90B", name.name(), "Engine model name should be 'GE90B'");
    }

    @Test
    void ensureEngineModelNameCannotBeNullOrBlank() {
        // Arrange
        String nullName = null;
        String blankName = "   ";

        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> EngineModelName.valueOf(nullName), "Creating an EngineModelName with null should throw IllegalArgumentException"),
                () -> assertThrows(IllegalArgumentException.class, () -> EngineModelName.valueOf(blankName), "Creating an EngineModelName with blank should throw IllegalArgumentException")
        );
    }

    @Test
    void ensureEngineModelNameEqualityAndHashCode() {
        // Arrange
        final EngineModelName a1 = EngineModelName.valueOf("GE90B");
        final EngineModelName a2 = EngineModelName.valueOf("GE90B");
        final EngineModelName b = EngineModelName.valueOf("GE9X");

        // Act & Assert
        assertEquals(a1, a2, "EngineModelNames with same name should be equal");
        assertEquals(a1.hashCode(), a2.hashCode(), "Equal EngineModelNames should have same hash code");
        assertNotEquals(a1, b, "EngineModelNames with different names should not be equal");
    }

    @Test
    void toString_returns_raw_name() {
        // Arrange
        final EngineModelName name = EngineModelName.valueOf("PW4000");

        // Act & Assert
        assertEquals("PW4000", name.toString());
    }

    @Test
    void equals_false_for_null_or_different_type() {
        // Arrange
        final EngineModelName name = EngineModelName.valueOf("PW4000");

        // Act & Assert
        assertFalse(name.equals(null));
        assertFalse(name.equals("PW4000"));
    }

    @Test
    void protected_no_arg_constructor_is_exposed_for_jpa() throws Exception {
        final Constructor<EngineModelName> ctor = EngineModelName.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }
}

