package aiSafe.weatherdata.domain;

import aiSafe.aircontrolarea.domain.Coordinate;
import aiSafe.aircontrolarea.domain.GeoBoundary;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeatherSubAreaTest {

    @Test
    void ensureSubAreaCannotHaveInvalidLatitudeBounds() {
        assertThrows(IllegalArgumentException.class,
                () -> new WeatherSubArea(39.0, 39.0, -9.5, -9.0, 0, 1000));
    }

    @Test
    void ensureSubAreaCannotHaveInvalidLongitudeBounds() {
        assertThrows(IllegalArgumentException.class,
                () -> new WeatherSubArea(38.5, 39.0, -9.0, -9.0, 0, 1000));
    }

    @Test
    void ensureNegativeMinimumAltitudeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new WeatherSubArea(38.5, 39.0, -9.5, -9.0, -1, 100));
    }

    @Test
    void ensureSubAreaCannotHaveInvalidAltitudeBounds() {
        assertThrows(IllegalArgumentException.class,
                () -> new WeatherSubArea(38.5, 39.0, -9.5, -9.0, 1000, 500));
    }

    @Test
    void ensureSubAreaContainmentValidationWorks() {
        final GeoBoundary boundary = new GeoBoundary(List.of(
                new Coordinate(38.0, -10.0),
                new Coordinate(38.0, -8.0),
                new Coordinate(40.0, -10.0),
                new Coordinate(40.0, -8.0)
        ));
        final WeatherSubArea inside = new WeatherSubArea(38.5, 39.5, -9.5, -8.5, 0, 2000);
        final WeatherSubArea outsideAltitude = new WeatherSubArea(38.5, 39.5, -9.5, -8.5, 0, 5000);
        final WeatherSubArea outsideLatitude = new WeatherSubArea(36.0, 37.0, -9.5, -8.5, 0, 2000);

        assertTrue(inside.isWithinControlArea(boundary, 3000));
        assertFalse(outsideAltitude.isWithinControlArea(boundary, 3000));
        assertFalse(outsideLatitude.isWithinControlArea(boundary, 3000));
        assertFalse(inside.isWithinControlArea(null, 3000));
    }

    @Test
    void ensureVolumeOverlapRespectsLayerBoundaries() {
        final WeatherSubArea base = new WeatherSubArea(38.5, 39.0, -9.5, -9.0, 0, 1000);
        final WeatherSubArea touchingAltitude = new WeatherSubArea(38.5, 39.0, -9.5, -9.0, 1000, 2000);
        final WeatherSubArea overlappingAltitude = new WeatherSubArea(38.5, 39.0, -9.5, -9.0, 900, 1500);

        assertFalse(base.overlapsVolumeWith(touchingAltitude));
        assertTrue(base.overlapsVolumeWith(overlappingAltitude));
    }

    @Test
    void ensureVolumeOverlapFalseWhenOnlyLatitudeAligns() {
        final WeatherSubArea a = new WeatherSubArea(38.5, 39.0, -9.5, -9.0, 0, 500);
        final WeatherSubArea b = new WeatherSubArea(38.5, 39.0, -8.0, -7.5, 0, 500);
        assertFalse(a.overlapsVolumeWith(b));
    }

    @Test
    void ensureVolumeOverlapFalseWhenOnlyLongitudeAligns() {
        final WeatherSubArea a = new WeatherSubArea(38.5, 39.0, -9.5, -9.0, 0, 500);
        final WeatherSubArea b = new WeatherSubArea(39.5, 40.0, -9.5, -9.0, 0, 500);
        assertFalse(a.overlapsVolumeWith(b));
    }

    @Test
    void ensureEqualsAndHashCodeReflectAllBounds() {
        final WeatherSubArea one = new WeatherSubArea(38.5, 39.0, -9.5, -9.0, 10, 900);
        final WeatherSubArea same = new WeatherSubArea(38.5, 39.0, -9.5, -9.0, 10, 900);
        final WeatherSubArea different = new WeatherSubArea(38.5, 39.0, -9.5, -9.0, 11, 900);
        assertEquals(one, same);
        assertEquals(one.hashCode(), same.hashCode());
        assertNotEquals(one, different);
        assertFalse(one.equals(null));
        assertFalse(one.equals("x"));
    }

    @Test
    void orm_constructor_is_provided() {
        assertNotNull(new WeatherSubArea());
    }
}
