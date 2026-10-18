package aiSafe.costumermanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    @Test
    void ensurePositionIsCreated() {
        // Arrange
        final Position position = Position.valueOf("Chief Pilot");

        // Act & Assert
        assertNotNull(position, "Position should not be null");
        assertEquals("Chief Pilot", position.position(), "Position should be 'Chief Pilot'");
    }

    @Test
    void ensurePositionCannotBeNullOrBlank() {
        // Arrange
        String nullPosition = null;
        String blankPosition = "   ";

        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> Position.valueOf(nullPosition), "Creating a Position with null should throw IllegalArgumentException"),
                () -> assertThrows(IllegalArgumentException.class, () -> Position.valueOf(blankPosition), "Creating a Position with blank should throw IllegalArgumentException")
        );
    }

    @Test
    void ensurePositionIsTrimmed() {
        // Arrange
        final Position position = Position.valueOf("  Chief Pilot  ");

        // Act & Assert
        assertEquals("Chief Pilot", position.position(), "Position should be trimmed");
    }

    @Test
    void ensurePositionEqualityAndHashCode() {
        // Arrange
        final Position p1 = Position.valueOf("Chief Pilot");
        final Position p2 = Position.valueOf("Chief Pilot");
        final Position p3 = Position.valueOf("Co-Pilot");

        // Act & Assert
        assertEquals(p1, p2, "Positions with same value should be equal");
        assertEquals(p1.hashCode(), p2.hashCode(), "Equal Positions should have same hash code");
        assertNotEquals(p1, p3, "Positions with different values should not be equal");
        assertFalse(p1.equals(null), "Position should not be equal to null");
        assertFalse(p1.equals("Chief Pilot"), "Position should not be equal to a different type");
    }
}