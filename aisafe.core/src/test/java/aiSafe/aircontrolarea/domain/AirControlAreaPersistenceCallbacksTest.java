package aiSafe.aircontrolarea.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Exercises JPA lifecycle hooks on {@link AirControlArea} (normally invoked by the ORM).
 */
class AirControlAreaPersistenceCallbacksTest {

    private static AirControlArea sampleArea() {
        final GeoBoundary boundary = new GeoBoundary(List.of(
                new Coordinate(0, 0),
                new Coordinate(0, 2),
                new Coordinate(2, 2),
                new Coordinate(2, 0)
        ));
        return new AirControlArea(
                new AreaCode("TST"),
                new AreaName("Test Area"),
                new MinFuelAtArrival(10.0),
                new MaxAltitude(5000),
                boundary
        );
    }

    @Test
    void postLoad_rebuilds_geoBoundary_from_persisted_coordinates() throws Exception {
        final AirControlArea area = sampleArea();
        final List<Coordinate> persisted = List.of(
                new Coordinate(1, 1),
                new Coordinate(1, 3),
                new Coordinate(3, 3),
                new Coordinate(3, 1)
        );

        final Field coordsField = AirControlArea.class.getDeclaredField("geoBoundaryCoordinates");
        coordsField.setAccessible(true);
        final Field boundaryField = AirControlArea.class.getDeclaredField("geoBoundary");
        boundaryField.setAccessible(true);

        boundaryField.set(area, null);
        coordsField.set(area, persisted);

        final Method postLoad = AirControlArea.class.getDeclaredMethod("syncBoundaryFromPersistenceCoordinates");
        postLoad.setAccessible(true);
        postLoad.invoke(area);

        assertNotNull(area.geoBoundary());
        assertEquals(4, area.geoBoundary().coordinates().size());
        assertEquals(persisted, area.geoBoundary().coordinates());
    }

    @Test
    void prePersist_preUpdate_sync_persisted_coordinates_from_boundary() throws Exception {
        final AirControlArea area = sampleArea();
        final GeoBoundary replacement = new GeoBoundary(List.of(
                new Coordinate(10, 10),
                new Coordinate(10, 12),
                new Coordinate(12, 12),
                new Coordinate(12, 10)
        ));

        final Field boundaryField = AirControlArea.class.getDeclaredField("geoBoundary");
        boundaryField.setAccessible(true);
        boundaryField.set(area, replacement);

        final Method syncPersist = AirControlArea.class.getDeclaredMethod("syncPersistenceCoordinatesFromBoundary");
        syncPersist.setAccessible(true);
        syncPersist.invoke(area);

        final Field coordsField = AirControlArea.class.getDeclaredField("geoBoundaryCoordinates");
        coordsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        final List<Coordinate> stored = (List<Coordinate>) coordsField.get(area);

        assertEquals(replacement.coordinates(), stored);
    }
}
