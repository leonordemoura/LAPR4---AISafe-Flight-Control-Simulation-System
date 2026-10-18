package aiSafe.airtransportcompany.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompanyNameTest {

    @Test
    void ensureCompanyNameIsCreated() {
        // Arrange
        final CompanyName name = CompanyName.valueOf("TAP Air Portugal");

        // Act & Assert
        assertNotNull(name, "CompanyName should be created successfully");
        assertEquals("TAP Air Portugal", name.name());
    }

    @Test
    void ensureCompanyNameCannotBeNullOrBlank() {
        // Arrange
        String nullName = null;
        String blankName = "   ";

        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> CompanyName.valueOf(nullName), "Creating a CompanyName with null should throw an exception"),
                () -> assertThrows(IllegalArgumentException.class, () -> CompanyName.valueOf(blankName), "Creating a CompanyName with blank should throw an exception")
        );
    }

    @Test
    void ensureCompanyNameEqualityAndHashCode() {
        // Arrange
        final CompanyName a1 = CompanyName.valueOf("AirTest");
        final CompanyName a2 = CompanyName.valueOf("AirTest");
        final CompanyName b = CompanyName.valueOf("Different");

        // Act & Assert
        assertEquals(a1, a2, "CompanyNames with the same name should be equal");
        assertEquals(a1.hashCode(), a2.hashCode(), "CompanyNames with the same name should have the same hash code");
        assertNotEquals(a1, b, "CompanyNames with different names should not be equal");
    }
}

