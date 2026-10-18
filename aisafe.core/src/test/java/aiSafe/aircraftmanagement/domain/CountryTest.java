package aiSafe.aircraftmanagement.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class CountryTest {

    @Test
    void ensureCountryIsCreated() {
        // Arrange
        final Country c = Country.valueOf("USA", 840);

        // Act & Assert
        assertNotNull(c, "Country should not be null");
        assertEquals("USA", c.name(), "Country name should be 'USA'");
        assertEquals(840, c.numericCode(), "Country numeric code should be 840");
    }

    @Test
    void ensureCountryEqualityAndHashCode() {
        // Arrange
        final Country a1 = Country.valueOf("USA", 840);
        final Country a2 = Country.valueOf("USA", 840);
        final Country b1 = Country.valueOf("USA", 841);
        final Country b2 = Country.valueOf("Portugal", 620);

        // Act
        assertEquals(a1, a2, "Countries with same name and code should be equal");
        assertEquals(a1.hashCode(), a2.hashCode(), "Equal countries should have same hash code");
        assertNotEquals(a1, b1, "Countries with same name but different code should not be equal");
        assertNotEquals(a1, b2, "Countries with different name and code should not be equal");
    }

    @Test
    void equals_false_for_null_or_different_type() {
        // Arrange
        final Country c = Country.valueOf("USA", 840);

        // Act & Assert
        assertFalse(c.equals(null));
        assertFalse(c.equals("USA"));
    }

    @Test
    void protected_no_arg_constructor_is_exposed_for_jpa() throws Exception {
        final Constructor<Country> ctor = Country.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }
}