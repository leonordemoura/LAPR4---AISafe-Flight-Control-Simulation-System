package aiSafe.usermanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FullNameTest {

    // =========================================================
    // SUCCESS CASES
    // =========================================================

    @Test
    void ensureFullNameIsCreatedSuccessfully() {

        final var name = new FullName("John", "Doe");

        assertNotNull(name);
    }

    @Test
    void ensureFirstNameIsReturnedCorrectly() {

        final var name = new FullName("John", "Doe");

        assertEquals("John", name.firstName());
    }

    @Test
    void ensureLastNameIsReturnedCorrectly() {

        final var name = new FullName("John", "Doe");

        assertEquals("Doe", name.lastName());
    }

    @Test
    void ensureToStringReturnsFullName() {

        final var name = new FullName("John", "Doe");

        assertEquals("John Doe", name.toString());
    }

    // =========================================================
    // VALIDATION TESTS
    // =========================================================

    @Test
    void ensureFullNameCannotHaveNullFirstName() {

        assertThrows(IllegalArgumentException.class, () -> {
            new FullName(null, "Doe");
        });
    }

    @Test
    void ensureFullNameCannotHaveNullLastName() {

        assertThrows(IllegalArgumentException.class, () -> {
            new FullName("John", null);
        });
    }

    @Test
    void ensureFullNameCannotHaveEmptyFirstName() {

        assertThrows(IllegalArgumentException.class, () -> {
            new FullName("", "Doe");
        });
    }

    @Test
    void ensureFullNameCannotHaveEmptyLastName() {

        assertThrows(IllegalArgumentException.class, () -> {
            new FullName("John", "");
        });
    }

    @Test
    void ensureFullNameCannotHaveBlankFirstName() {

        assertThrows(IllegalArgumentException.class, () -> {
            new FullName("   ", "Doe");
        });
    }

    @Test
    void ensureFullNameCannotHaveBlankLastName() {

        assertThrows(IllegalArgumentException.class, () -> {
            new FullName("John", "   ");
        });
    }

    // =========================================================
    // NORMALIZATION TESTS
    // =========================================================

    @Test
    void ensureFullNameIsTrimmed() {

        final var name = new FullName("  John  ", "  Doe  ");

        assertEquals("John", name.firstName());
        assertEquals("Doe", name.lastName());
    }

    // =========================================================
    // EQUALITY TESTS
    // =========================================================

    @Test
    void ensureSameFullNamesAreEqual() {

        final var n1 = new FullName("John", "Doe");
        final var n2 = new FullName("John", "Doe");

        assertEquals(n1, n2);
    }

    @Test
    void ensureDifferentFullNamesAreNotEqual() {

        final var n1 = new FullName("John", "Doe");
        final var n2 = new FullName("Jane", "Doe");

        assertNotEquals(n1, n2);
    }

    @Test
    void ensureHashCodeIsEqualForSameFullName() {

        final var n1 = new FullName("John", "Doe");
        final var n2 = new FullName("John", "Doe");

        assertEquals(n1.hashCode(), n2.hashCode());
    }

    @Test
    void ensureFullNameIsNotEqualToDifferentObjectType() {

        final var name = new FullName("John", "Doe");

        assertNotEquals(name, "John Doe");
    }

    @Test
    void ensureFullNameIsEqualToItself() {

        final var name = new FullName("John", "Doe");

        assertEquals(name, name);
    }
}