package aiSafe.weatherdata.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

// ObservationDateTest
public class ObservationDateTest {

    @Test
    public void ensureObservationDateCannotHaveNullStart() {
        // Arrange
        LocalDateTime nullStart = null;
        LocalDateTime end = LocalDateTime.of(2026, 4, 19, 15, 0);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new ObservationDate(nullStart, end);
        });
    }

    @Test
    public void ensureObservationDateCannotHaveNullEnd() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2026, 4, 19, 14, 0);
        LocalDateTime nullEnd = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new ObservationDate(start, nullEnd);
        });
    }

    @Test
    public void ensureObservationDateEndCannotBeBeforeStart() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2026, 4, 19, 15, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 19, 14, 0);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new ObservationDate(start, end);
        });
    }

    @Test
    public void ensureObservationDateEndCannotEqualStart() {
        // Arrange
        LocalDateTime same = LocalDateTime.of(2026, 4, 19, 14, 0);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new ObservationDate(same, same);
        });
    }

    @Test
    public void ensureObservationDateIsCreatedSuccessfully() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2026, 4, 19, 14, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 19, 15, 0);

        // Act
        ObservationDate date = new ObservationDate(start, end);

        // Assert
        assertEquals(start, date.startDate());
        assertEquals(end, date.endDate());
    }

    @Test
    void orm_constructor_is_provided() {
        new ObservationDate();
    }

    @Test
    void ensure_equals_and_hashCode_use_start_and_end() {
        final LocalDateTime start = LocalDateTime.of(2026, 1, 2, 3, 4);
        final LocalDateTime end = LocalDateTime.of(2026, 1, 2, 4, 0);
        final ObservationDate a = new ObservationDate(start, end);
        final ObservationDate b = new ObservationDate(start, end);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new ObservationDate(start, end.plusMinutes(1)));
        assertFalse(a.equals("x"));
        assertFalse(a.equals(null));
    }
}