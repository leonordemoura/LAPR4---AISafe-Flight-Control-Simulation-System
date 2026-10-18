package aiSafe.airport.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Coordinate_AirportTest {

    // =========================================================
    // SUCCESS CASES
    // =========================================================

    @Test
    void ensureCoordinateIsCreatedSuccessfully() {

        final var coordinate = new Coordinate_Airport(41.242, -8.678);

        assertNotNull(coordinate);
    }

    @Test
    void ensureLatitudeReturnsCorrectValue() {

        final var coordinate = new Coordinate_Airport(41.242, -8.678);

        assertEquals(41.242, coordinate.latitude());
    }

    @Test
    void ensureLongitudeReturnsCorrectValue() {

        final var coordinate = new Coordinate_Airport(41.242, -8.678);

        assertEquals(-8.678, coordinate.longitude());
    }

    // =========================================================
    // LATITUDE VALIDATION
    // =========================================================

    @Test
    void ensureLatitudeCannotBeGreaterThan90() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Coordinate_Airport(91.0, -8.678);
        });
    }

    @Test
    void ensureLatitudeCannotBeLowerThanMinus90() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Coordinate_Airport(-91.0, -8.678);
        });
    }

    @Test
    void ensureLatitudeBoundaryValuesAreAccepted() {

        assertDoesNotThrow(() -> {
            new Coordinate_Airport(-90.0, 0.0);
        });

        assertDoesNotThrow(() -> {
            new Coordinate_Airport(90.0, 0.0);
        });
    }

    // =========================================================
    // LONGITUDE VALIDATION
    // =========================================================

    @Test
    void ensureLongitudeCannotBeGreaterThan180() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Coordinate_Airport(41.242, 181.0);
        });
    }

    @Test
    void ensureLongitudeCannotBeLowerThanMinus180() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Coordinate_Airport(41.242, -181.0);
        });
    }

    @Test
    void ensureLongitudeBoundaryValuesAreAccepted() {

        assertDoesNotThrow(() -> {
            new Coordinate_Airport(0.0, -180.0);
        });

        assertDoesNotThrow(() -> {
            new Coordinate_Airport(0.0, 180.0);
        });
    }

    // =========================================================
    // COMBINED VALIDATION
    // =========================================================

    @Test
    void ensureInvalidLatitudeAndLongitudeAreRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Coordinate_Airport(100.0, 200.0);
        });
    }
}