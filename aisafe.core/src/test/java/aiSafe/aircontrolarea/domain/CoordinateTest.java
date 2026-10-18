package aiSafe.aircontrolarea.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// CoordinateTest
public class CoordinateTest {

    @Test
    public void ensureLatitudeCannotBeAbove90() {
        // Arrange
        double invalidLatitude = 91.0;
        double validLongitude = 0.0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new Coordinate(invalidLatitude, validLongitude);
        });
    }

    @Test
    public void ensureLatitudeCannotBeBelow90() {
        // Arrange
        double invalidLatitude = -91.0;
        double validLongitude = 0.0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new Coordinate(invalidLatitude, validLongitude);
        });
    }

    @Test
    public void ensureLongitudeCannotBeAbove180() {
        // Arrange
        double validLatitude = 0.0;
        double invalidLongitude = 181.0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new Coordinate(validLatitude, invalidLongitude);
        });
    }

    @Test
    public void ensureLongitudeCannotBeBelow180() {
        // Arrange
        double validLatitude = 0.0;
        double invalidLongitude = -181.0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new Coordinate(validLatitude, invalidLongitude);
        });
    }

    @Test
    public void ensureCoordinateIsCreatedSuccessfully() {
        // Arrange
        double latitude = 38.7;
        double longitude = -9.1;

        // Act
        Coordinate coordinate = new Coordinate(latitude, longitude);

        // Assert
        assertEquals(latitude, coordinate.latitude(), 0.001);
        assertEquals(longitude, coordinate.longitude(), 0.001);
    }

    @Test
    public void ensureLatitudeAndLongitudeInclusiveBoundsAtPolesAndAntimeridian() {
        assertEquals(90.0, new Coordinate(90, 180).latitude(), 0.001);
        assertEquals(-90.0, new Coordinate(-90, -180).latitude(), 0.001);
    }

    @Test
    void ensureEqualsHashCodeAndOrmConstructor() throws Exception {
        final Coordinate a = new Coordinate(1, -2);
        final Coordinate b = new Coordinate(1, -2);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new Coordinate(1, 2));

        final var ctor = Coordinate.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }
}