package aiSafe.simulationreport.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SimulationReportValueObjectEqualsTest {

    @Test
    void position_equalsHandlesNullAndDifferentTypes() {
        // Arrange
        final Position position = new Position(1, 2);

        // Act & Assert
        assertFalse(position.equals(null));
        assertFalse(position.equals("not-a-position"));
        assertEquals(position, new Position(1, 2));
        assertNotEquals(position, new Position(2, 1));
    }

    @Test
    void velocityVector_equalsHandlesNullAndDifferentTypes() {
        // Arrange
        final VelocityVector vector = new VelocityVector(1, 2, 3);

        // Act & Assert
        assertFalse(vector.equals(null));
        assertFalse(vector.equals("not-a-vector"));
        assertEquals(vector, new VelocityVector(1, 2, 3));
    }

    @Test
    void simulationTimestamp_equalsAndHashCode() {
        // Arrange
        final SimulationTimestamp first = new SimulationTimestamp(60);
        final SimulationTimestamp second = new SimulationTimestamp(60);

        // Act & Assert
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, new SimulationTimestamp(61));
        assertFalse(first.equals(null));
    }

    @Test
    void reportDate_equalsHandlesNullAndDifferentTypes() {
        // Arrange
        final ReportDate date = new ReportDate(LocalDateTime.of(2026, 1, 1, 0, 0));

        // Act & Assert
        assertFalse(date.equals(null));
        assertFalse(date.equals("date"));
        assertEquals(date, new ReportDate(LocalDateTime.of(2026, 1, 1, 0, 0)));
    }

    @Test
    void totalFlights_equalsHandlesNullAndDifferentTypes() {
        // Arrange
        final TotalFlights flights = new TotalFlights(3);

        // Act & Assert
        assertFalse(flights.equals(null));
        assertFalse(flights.equals("3"));
        assertEquals(flights, new TotalFlights(3));
        assertNotEquals(flights, new TotalFlights(4));
    }

    @Test
    void totalViolations_equalsHandlesNullAndDifferentTypes() {
        // Arrange
        final TotalViolations violations = new TotalViolations(2);

        // Act & Assert
        assertFalse(violations.equals(null));
        assertFalse(violations.equals("2"));
        assertEquals(violations, new TotalViolations(2));
    }

    @Test
    void violationThreshold_equalsHandlesNullAndDifferentTypes() {
        // Arrange
        final ViolationThreshold threshold = new ViolationThreshold(50);

        // Act & Assert
        assertFalse(threshold.equals(null));
        assertFalse(threshold.equals("50"));
        assertEquals(threshold, new ViolationThreshold(50));
    }

    @Test
    void reportFilePath_equalsHandlesNullAndDifferentTypes() {
        // Arrange
        final ReportFilePath path = new ReportFilePath("reports/a.txt");

        // Act & Assert
        assertFalse(path.equals(null));
        assertFalse(path.equals("reports/a.txt"));
        assertEquals(path, new ReportFilePath("reports/a.txt"));
        assertNotEquals(path, new ReportFilePath("reports/b.txt"));
    }

    @Test
    void velocityVector_detectsDifferencesOnEachAxis() {
        // Arrange
        final VelocityVector base = new VelocityVector(1, 2, 3);

        // Act & Assert
        assertNotEquals(base, new VelocityVector(0, 2, 3));
        assertNotEquals(base, new VelocityVector(1, 0, 3));
        assertNotEquals(base, new VelocityVector(1, 2, 0));
    }

    @Test
    void position_detectsDifferencesOnEachAxis() {
        // Arrange
        final Position base = new Position(1, 2);

        // Act & Assert
        assertNotEquals(base, new Position(0, 2));
        assertNotEquals(base, new Position(1, 0));
    }
}
