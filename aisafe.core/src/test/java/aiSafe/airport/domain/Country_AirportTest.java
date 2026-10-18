package aiSafe.airport.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Country_AirportTest {

    // =========================================================
    // SUCCESS CASES
    // =========================================================

    @Test
    void ensureCountryAirportIsCreatedSuccessfully() {

        final var country = new Country_Airport("Portugal", "PT");

        assertNotNull(country);
    }

    @Test
    void ensureValueOfCreatesCountrySuccessfully() {

        final var country = Country_Airport.valueOf("Portugal", "PT");

        assertNotNull(country);
    }

    @Test
    void ensureNameReturnsCorrectValue() {

        final var country = new Country_Airport("Portugal", "PT");

        assertEquals("Portugal", country.name());
    }

    @Test
    void ensureCodeReturnsCorrectValue() {

        final var country = new Country_Airport("Portugal", "PT");

        assertEquals("PT", country.code());
    }

    // =========================================================
    // VALIDATION TESTS
    // =========================================================

    @Test
    void ensureCountryNameCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Country_Airport(null, "PT");
        });
    }

    @Test
    void ensureCountryNameCannotBeEmpty() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Country_Airport("", "PT");
        });
    }

    @Test
    void ensureCountryCodeCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Country_Airport("Portugal", null);
        });
    }

    @Test
    void ensureCountryCodeCannotBeEmpty() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Country_Airport("Portugal", "");
        });
    }

    @Test
    void ensureCountryCodeMustContainExactlyTwoUppercaseLetters() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Country_Airport("Portugal", "P");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Country_Airport("Portugal", "PRT");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Country_Airport("Portugal", "pt");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Country_Airport("Portugal", "P1");
        });
    }

    // =========================================================
    // EQUALITY TESTS
    // =========================================================

    @Test
    void ensureCountriesWithSameValuesAreEqual() {

        final var country1 = new Country_Airport("Portugal", "PT");
        final var country2 = new Country_Airport("Portugal", "PT");

        assertEquals(country1, country2);
    }

    @Test
    void ensureCountriesWithDifferentValuesAreNotEqual() {

        final var country1 = new Country_Airport("Portugal", "PT");
        final var country2 = new Country_Airport("Spain", "ES");

        assertNotEquals(country1, country2);
    }

    @Test
    void ensureHashCodeIsEqualForSameValues() {

        final var country1 = new Country_Airport("Portugal", "PT");
        final var country2 = new Country_Airport("Portugal", "PT");

        assertEquals(country1.hashCode(), country2.hashCode());
    }

    @Test
    void ensureCountryAirportIsNotEqualToDifferentObjectType() {

        final var country = new Country_Airport("Portugal", "PT");

        assertNotEquals(country, "Portugal");
    }

    @Test
    void ensureCountryAirportIsEqualToItself() {

        final var country = new Country_Airport("Portugal", "PT");

        assertEquals(country, country);
    }
}