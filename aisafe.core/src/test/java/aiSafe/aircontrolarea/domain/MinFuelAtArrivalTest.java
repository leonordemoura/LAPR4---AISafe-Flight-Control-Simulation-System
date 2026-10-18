package aiSafe.aircontrolarea.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// MinFuelAtArrivalTest
public class MinFuelAtArrivalTest {

    @Test
    public void ensureMinFuelAtArrivalCannotBeNegative() {
        // Arrange
        double negativeValue = -1.0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new MinFuelAtArrival(negativeValue);
        });
    }

    @Test
    public void ensureMinFuelAtArrivalCannotBeZero() {
        // Arrange
        double zeroValue = 0.0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new MinFuelAtArrival(zeroValue);
        });
    }

    @Test
    public void ensureMinFuelAtArrivalIsCreatedSuccessfully() {
        // Arrange
        double value = 100.0;

        // Act
        MinFuelAtArrival minFuel = new MinFuelAtArrival(value);

        // Assert
        assertEquals(value, minFuel.value(), 0.001);
    }

    @Test
    void ensureEqualsHashCodeAndOrmConstructor() throws Exception {
        final MinFuelAtArrival a = new MinFuelAtArrival(50);
        final MinFuelAtArrival b = new MinFuelAtArrival(50);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new MinFuelAtArrival(51));
        assertFalse(a.equals(null));
        assertFalse(a.equals("x"));

        final var ctor = MinFuelAtArrival.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }
}