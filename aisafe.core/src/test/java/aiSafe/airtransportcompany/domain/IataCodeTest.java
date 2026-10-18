package aiSafe.airtransportcompany.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IataCodeTest {

    @Test
    void ensureIataCodeIsCreatedAndUppercased() {
        // Arrange
        final IataCode code = IataCode.valueOf("tp");

        // Act & Assert
        assertNotNull(code);
        assertEquals("TP", code.code(), "IATA code should be stored in uppercase");
    }

    @Test
    void ensureIataCodeCannotBeNullOrBlank() {
        // Arrange
        String nullCode = null;
        String blankCode = "   ";

        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> IataCode.valueOf(nullCode), "Creating an IATA code with null should throw an exception"),
                () -> assertThrows(IllegalArgumentException.class, () -> IataCode.valueOf(blankCode), "Creating an IATA code with blank should throw an exception")
        );
    }

    @Test
    void ensureIataCodeMustHaveExactlyTwoCharacters() {
        // Arrange
        String oneCharCode = "A";
        String threeCharCode = "ABC";

        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> IataCode.valueOf(oneCharCode), "Creating an IATA code with only one character should throw an exception"),
                () -> assertThrows(IllegalArgumentException.class, () -> IataCode.valueOf(threeCharCode), "Creating an IATA code with three characters should throw an exception")
        );
    }

    @Test
    void ensureIataCodeEqualityAndHashCode() {
        // Arrange
        final IataCode a1 = IataCode.valueOf("TP");
        final IataCode a2 = IataCode.valueOf("tp");
        final IataCode b = IataCode.valueOf("BA");

        // Act & Assert
        assertEquals(a1, a2, "IATA codes with the same characters should be equal regardless of case");
        assertEquals(a1.hashCode(), a2.hashCode(), "IATA codes with the same characters should have the same hash code regardless of case");
        assertNotEquals(a1, b, "IATA codes with different characters should not be equal");
    }
}

