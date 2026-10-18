package aiSafe.aircontrolarea.domain;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

// GeoBoundaryTest
public class GeoBoundaryTest {

    @Test
    public void ensureGeoBoundaryCannotBeNull() {
        // Arrange
        List<Coordinate> nullCoords = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new GeoBoundary(nullCoords);
        });
    }

    @Test
    public void ensureGeoBoundaryCannotHaveLessThan4Coordinates() {
        // Arrange
        List<Coordinate> coords = List.of(
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(1, 1)
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new GeoBoundary(coords);
        });
    }

    @Test
    public void ensureGeoBoundaryCannotHaveMoreThan4Coordinates() {
        // Arrange
        List<Coordinate> coords = List.of(
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 1),
                new Coordinate(0, 0)
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new GeoBoundary(coords);
        });
    }

    @Test
    public void ensureGeoBoundaryCannotHaveDuplicateCoordinates() {
        // Arrange
        List<Coordinate> coords = List.of(
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 0)
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new GeoBoundary(coords);
        });
    }

    @Test
    public void ensureGeoBoundaryCannotHaveZeroArea() {
        // Arrange
        List<Coordinate> coords = List.of(
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(2, 0),
                new Coordinate(3, 0)
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new GeoBoundary(coords);
        });
    }

    @Test
    public void ensureGeoBoundaryIsCreatedSuccessfully() {
        // Arrange
        List<Coordinate> coords = List.of(
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 1)
        );

        // Act
        GeoBoundary boundary = new GeoBoundary(coords);

        // Assert
        assertEquals(4, boundary.coordinates().size());
    }

    @Test
    public void ensureOverlappingRectanglesAreDetected() {
        final GeoBoundary first = new GeoBoundary(List.of(
                new Coordinate(0, 0),
                new Coordinate(0, 2),
                new Coordinate(2, 0),
                new Coordinate(2, 2)
        ));

        final GeoBoundary second = new GeoBoundary(List.of(
                new Coordinate(1, 1),
                new Coordinate(1, 3),
                new Coordinate(3, 1),
                new Coordinate(3, 3)
        ));

        assertTrue(first.overlapsWith(second));
    }

    @Test
    public void ensureTouchingRectanglesAreNotConsideredOverlapping() {
        final GeoBoundary first = new GeoBoundary(List.of(
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 0),
                new Coordinate(1, 1)
        ));

        final GeoBoundary second = new GeoBoundary(List.of(
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(2, 0),
                new Coordinate(2, 1)
        ));

        assertFalse(first.overlapsWith(second));
    }

    @Test
    void ensureDisjointLongitudeRangesDoNotOverlap() {
        final GeoBoundary west = new GeoBoundary(List.of(
                new Coordinate(0, 0),
                new Coordinate(0, 2),
                new Coordinate(2, 2),
                new Coordinate(2, 0)
        ));
        final GeoBoundary east = new GeoBoundary(List.of(
                new Coordinate(0, 4),
                new Coordinate(0, 6),
                new Coordinate(2, 6),
                new Coordinate(2, 4)
        ));
        assertFalse(west.overlapsWith(east));
    }

    @Test
    void ensureContainsDetectsPointInsideAndOutside() {
        final GeoBoundary boundary = new GeoBoundary(List.of(
                new Coordinate(0, 0),
                new Coordinate(0, 2),
                new Coordinate(2, 2),
                new Coordinate(2, 0)
        ));
        assertTrue(boundary.contains(new Coordinate(1, 1)));
        assertFalse(boundary.contains(new Coordinate(-0.1, 1)));
    }

    @Test
    void ensureFourDistinctPointsThatAreNotRectangleCornersAreRejected() {
        final List<Coordinate> skew = List.of(
                new Coordinate(0, 0),
                new Coordinate(2, 0),
                new Coordinate(0, 2),
                new Coordinate(3, 2)
        );
        assertThrows(IllegalArgumentException.class, () -> new GeoBoundary(skew));
    }

    @Test
    void ensureEqualsAndHashCodeUseCoordinateList() {
        final List<Coordinate> corners = List.of(
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 0),
                new Coordinate(1, 1)
        );
        final GeoBoundary a = new GeoBoundary(corners);
        final GeoBoundary b = new GeoBoundary(List.of(
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 0),
                new Coordinate(1, 1)
        ));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.equals(null));
        assertFalse(a.equals("not-a-boundary"));
    }

    @Test
    void orm_constructor_is_provided_for_persistence_layers() throws Exception {
        final var ctor = GeoBoundary.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }
}
