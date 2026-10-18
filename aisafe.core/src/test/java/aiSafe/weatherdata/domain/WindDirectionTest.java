package aiSafe.weatherdata.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// WindDirectionTest
public class WindDirectionTest {

    @Test
    public void ensureWindDirectionCannotBeNegative() {
        // Arrange
        double negativeValue = -1.0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new WindDirection(negativeValue);
        });
    }

    @Test
    public void ensureWindDirectionCannotBeEqualTo360() {
        // Arrange
        double value360 = 360.0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new WindDirection(value360);
        });
    }

    @Test
    public void ensureWindDirectionCannotBeAbove360() {
        // Arrange
        double aboveValue = 361.0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new WindDirection(aboveValue);
        });
    }

    @Test
    public void ensureWindDirectionAtZeroIsValid() {
        // Arrange
        double zeroValue = 0.0;

        // Act
        WindDirection direction = new WindDirection(zeroValue);

        // Assert
        assertEquals(zeroValue, direction.value(), 0.001);
    }

    @Test
    public void ensureWindDirectionIsCreatedSuccessfully() {
        // Arrange
        double value = 180.0;

        // Act
        WindDirection direction = new WindDirection(value);

        // Assert
        assertEquals(value, direction.value(), 0.001);
    }
}