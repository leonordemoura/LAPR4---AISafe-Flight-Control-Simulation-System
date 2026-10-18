package aiSafe.weatherdata.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// TemperatureTest
public class TemperatureTest {

    @Test
    public void ensureTemperatureCannotHaveNullUnit() {
        // Arrange
        double value = 15.0;
        TemperatureUnit nullUnit = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new Temperature(value, nullUnit);
        });
    }

    @Test
    public void ensureTemperatureIsCreatedSuccessfully() {
        // Arrange
        double value = 15.0;
        TemperatureUnit unit = TemperatureUnit.C;

        // Act
        Temperature temp = new Temperature(value, unit);

        // Assert
        assertEquals(value, temp.value(), 0.001);
        assertEquals(unit, temp.unit());
    }
}