package aiSafe.airtransportcompany.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IcaoCodeTest {

    @Test
    void ensureIcaoCodeIsCreatedAndUppercased() {
        // Arrange
        final IcaoCode code = IcaoCode.valueOf("tap");

        // Act & Assert
        assertNotNull(code, "ICAO code should be created successfully");
        assertEquals("TAP", code.code(), "ICAO code should be stored in uppercase");
    }

    @Test
    void ensureIcaoCodeAcceptsTwoOrThreeCharacters() {
        // Arrange
        String firstCode = "ab";
        String secondCode = "abc";
        String firstExpected = "AB";
        String secondExpected = "ABC";

        // Act & Assert
        assertAll(
                () -> assertEquals(firstExpected, IcaoCode.valueOf(firstCode).code(), "ICAO code with two characters should be stored in uppercase"),
                () -> assertEquals(secondExpected, IcaoCode.valueOf(secondCode).code(), "ICAO code with three characters should be stored in uppercase")
        );
    }

    @Test
    void ensureIcaoCodeCannotBeNullOrBlank() {
        // Arrange
        String nullCode = null;
        String blankCode = "   ";

        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> IcaoCode.valueOf(nullCode), "Creating an ICAO code with null should throw an exception"),
                () -> assertThrows(IllegalArgumentException.class, () -> IcaoCode.valueOf(blankCode), "Creating an ICAO code with blank should throw an exception")
        );
    }

    @Test
    void ensureIcaoCodeMustHaveTwoOrThreeCharacters() {
        // Arrange
        String oneCharCode = "A";
        String fourCharCode = "ABCD";

        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> IcaoCode.valueOf(oneCharCode), "Creating an ICAO code with only one character should throw an exception"),
                () -> assertThrows(IllegalArgumentException.class, () -> IcaoCode.valueOf(fourCharCode), "Creating an ICAO code with four characters should throw an exception")
        );
    }

    @Test
    void ensureIcaoCodeEqualityAndHashCode() {
        // Arrange
        final IcaoCode a1 = IcaoCode.valueOf("TAP");
        final IcaoCode a2 = IcaoCode.valueOf("tap");
        final IcaoCode b = IcaoCode.valueOf("BAW");

        // Act & Assert
        assertEquals(a1, a2, "ICAO codes with the same characters should be equal regardless of case");
        assertEquals(a1.hashCode(), a2.hashCode(), "ICAO codes with the same characters should have the same hash code regardless of case");
        assertNotEquals(a1, b, "ICAO codes with different characters should not be equal");
    }
}

