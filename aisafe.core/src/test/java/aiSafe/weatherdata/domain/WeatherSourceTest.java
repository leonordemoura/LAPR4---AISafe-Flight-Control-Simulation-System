package aiSafe.weatherdata.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// WeatherSourceTest
public class WeatherSourceTest {

    @Test
    public void ensureWeatherSourceCannotBeNull() {
        // Arrange
        String nullValue = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new WeatherSource(nullValue);
        });
    }

    @Test
    public void ensureWeatherSourceCannotBeEmpty() {
        // Arrange
        String emptyValue = "";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new WeatherSource(emptyValue);
        });
    }

    @Test
    public void ensureWeatherSourceCannotBeBlank() {
        // Arrange
        String blankValue = "   ";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new WeatherSource(blankValue);
        });
    }

    @Test
    public void ensureWeatherSourceIsCreatedSuccessfully() {
        // Arrange
        String value = "IPMA";

        // Act
        WeatherSource source = new WeatherSource(value);

        // Assert
        assertEquals(value, source.value());
    }

    @Test
    void orm_constructor_is_provided() {
        new WeatherSource();
    }

    @Test
    void ensureTrimAndEquals() {
        final WeatherSource a = new WeatherSource("  meteo  ");
        assertEquals("meteo", a.value());
        assertEquals(a, new WeatherSource("meteo"));
        assertEquals(a.hashCode(), new WeatherSource("meteo").hashCode());
        assertNotEquals(a, new WeatherSource("other"));
        assertFalse(a.equals(null));
        assertFalse(a.equals(Boolean.TRUE));
    }
}