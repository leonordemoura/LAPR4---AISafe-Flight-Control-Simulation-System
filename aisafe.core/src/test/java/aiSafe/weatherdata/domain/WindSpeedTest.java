package aiSafe.weatherdata.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// WindSpeedTest
public class WindSpeedTest {

    @Test
    public void ensureWindSpeedCannotBeNegative() {
        // Arrange
        double negativeValue = -1.0;
        WindSpeedUnit unit = WindSpeedUnit.KMH;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new WindSpeed(negativeValue, unit);
        });
    }

    @Test
    public void ensureWindSpeedCannotHaveNullUnit() {
        // Arrange
        double value = 20.0;
        WindSpeedUnit nullUnit = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new WindSpeed(value, nullUnit);
        });
    }

    @Test
    public void ensureWindSpeedAtZeroIsValid() {
        // Arrange
        double zeroValue = 0.0;
        WindSpeedUnit unit = WindSpeedUnit.MPS;

        // Act
        WindSpeed speed = new WindSpeed(zeroValue, unit);

        // Assert
        assertEquals(zeroValue, speed.value(), 0.001);
    }

    @Test
    public void ensureWindSpeedIsCreatedSuccessfully() {
        // Arrange
        double value = 20.0;
        WindSpeedUnit unit = WindSpeedUnit.KMH;

        // Act
        WindSpeed speed = new WindSpeed(value, unit);

        // Assert
        assertEquals(value, speed.value(), 0.001);
        assertEquals(unit, speed.unit());
    }
}