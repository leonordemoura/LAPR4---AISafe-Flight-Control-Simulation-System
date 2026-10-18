package aiSafe.weatherdata.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// PressureTest
public class PressureTest {

    @Test
    public void ensurePressureCannotBeNegative() {
        // Arrange
        double negativeValue = -1.0;
        PressureUnit unit = PressureUnit.HPA;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new Pressure(negativeValue, unit);
        });
    }

    @Test
    public void ensurePressureCannotBeZero() {
        // Arrange
        double zeroValue = 0.0;
        PressureUnit unit = PressureUnit.HPA;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new Pressure(zeroValue, unit);
        });
    }

    @Test
    public void ensurePressureCannotHaveNullUnit() {
        // Arrange
        double value = 1013.0;
        PressureUnit nullUnit = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new Pressure(value, nullUnit);
        });
    }

    @Test
    public void ensurePressureIsCreatedSuccessfully() {
        // Arrange
        double value = 1013.0;
        PressureUnit unit = PressureUnit.HPA;

        // Act
        Pressure pressure = new Pressure(value, unit);

        // Assert
        assertEquals(value, pressure.value(), 0.001);
        assertEquals(unit, pressure.unit());
    }
}