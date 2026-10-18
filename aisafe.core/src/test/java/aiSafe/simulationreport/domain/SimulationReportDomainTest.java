package aiSafe.simulationreport.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulationReportDomainTest {

    @Test
    void embeddables_exposeProtectedConstructorsForOrm() throws Exception {
        // Arrange & Act
        final ReportDate reportDate = newReportDateViaReflection();
        final TotalFlights totalFlights = newTotalFlightsViaReflection();
        final TotalViolations totalViolations = newTotalViolationsViaReflection();
        final ViolationThreshold threshold = newViolationThresholdViaReflection();
        final ReportFilePath filePath = newReportFilePathViaReflection();
        final SimulationReport report = newSimulationReportViaReflection();

        // Assert
        assertNotNull(reportDate);
        assertEquals(reportDate, newReportDateViaReflection());
        assertEquals(totalFlights, newTotalFlightsViaReflection());
        assertEquals(totalViolations, newTotalViolationsViaReflection());
        assertEquals(threshold, newViolationThresholdViaReflection());
        assertEquals(filePath, newReportFilePathViaReflection());
        assertNull(report.identity());
    }

    @Test
    void reportDate_rejectsNull() {
        // Arrange
        final Executable action = () -> new ReportDate(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void reportDate_equalsAndHashCode() {
        // Arrange
        final LocalDateTime when = LocalDateTime.of(2026, 5, 16, 10, 0);
        final ReportDate first = new ReportDate(when);
        final ReportDate second = new ReportDate(when);

        // Act & Assert
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void totalFlights_rejectsNegative() {
        // Arrange
        final Executable action = () -> new TotalFlights(-1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void totalViolations_rejectsNegative() {
        // Arrange
        final Executable action = () -> new TotalViolations(-1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void violationThreshold_rejectsNegative() {
        // Arrange
        final Executable action = () -> new ViolationThreshold(-1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void reportFilePath_rejectsBlank() {
        // Arrange
        final Executable action = () -> new ReportFilePath("  ");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void simulationTimestamp_rejectsInvalidMinutes() {
        // Arrange
        final Executable action = () -> new SimulationTimestamp(24 * 60 + 1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void simulationTimestamp_formatsTimeLabel() {
        // Arrange
        final SimulationTimestamp timestamp = new SimulationTimestamp(9 * 60 + 5);

        // Act
        final String label = timestamp.asTimeLabel();

        // Assert
        assertEquals("09:05", label);
    }

    @Test
    void flightExecutionStatus_rejectsInvalidFlightId() {
        // Arrange
        final Executable action = () ->
                new FlightExecutionStatus(0, 0, 100.0, "ok");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void flightHistorySnapshot_rejectsEmptyPoints() {
        // Arrange
        final Executable action = () -> new FlightHistorySnapshot(1, List.of());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void flightViolationState_allowsNullVelocity() {
        // Arrange
        final FlightViolationState state = new FlightViolationState(
                1,
                new Position(1, 2),
                1000,
                new SimulationTimestamp(540),
                null
        );

        // Act
        final boolean hasVelocity = state.velocity().isPresent();

        // Assert
        assertFalse(hasVelocity);
    }

    @Test
    void simulationReport_attachFilePathAndSameAs() {
        // Arrange
        final SimulationReport report = sampleReport(ValidationStatus.SUCCEEDED);

        // Act
        report.attachFilePath(new ReportFilePath("reports/report_test.txt"));

        // Assert
        assertEquals("reports/report_test.txt", report.filePath().value());
        assertTrue(report.sameAs(report));
        assertFalse(report.sameAs("not-a-report"));
    }

    @Test
    void simulationReport_rejectsNullAttachFilePath() {
        // Arrange
        final SimulationReport report = sampleReport(ValidationStatus.SUCCEEDED);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> report.attachFilePath(null));
    }

    @Test
    void position_equalsAndHashCode() {
        // Arrange
        final Position first = new Position(1.0, 2.0);
        final Position second = new Position(1.0, 2.0);
        final Position third = new Position(3.0, 4.0);

        // Act & Assert
        assertEquals(first, second);
        assertNotEquals(first, third);
    }

    @Test
    void velocityVector_equalsAndHashCode() {
        // Arrange
        final VelocityVector first = new VelocityVector(1, 2, 3);
        final VelocityVector second = new VelocityVector(1, 2, 3);

        // Act & Assert
        assertEquals(first, second);
        assertNotEquals(first, new VelocityVector(0, 0, 0));
    }

    @Test
    void totalFlights_equalsAndAcceptsZero() {
        // Arrange
        final TotalFlights flights = new TotalFlights(0);

        // Act & Assert
        assertEquals(0, flights.value());
        assertEquals(flights, new TotalFlights(0));
    }

    @Test
    void embeddableValueObjects_supportEquals() {
        // Arrange
        final TotalViolations violations = new TotalViolations(1);
        final ViolationThreshold threshold = new ViolationThreshold(10);
        final ReportFilePath path = new ReportFilePath("reports/a.txt");

        // Act & Assert
        assertEquals(violations, new TotalViolations(1));
        assertEquals(threshold, new ViolationThreshold(10));
        assertEquals(path, new ReportFilePath("reports/a.txt"));
    }

    @Test
    void flightExecutionStatus_rejectsNegativeViolationCount() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> new FlightExecutionStatus(1, -1, null, "msg"));
    }

    @Test
    void flightViolationState_rejectsNullPosition() {
        // Arrange & Act & Assert
        assertThrows(NullPointerException.class, () -> new FlightViolationState(
                1, null, 1000, new SimulationTimestamp(540), null));
    }

    @Test
    void simulationReport_constructorAllowsNullCollections() {
        // Arrange & Act
        final SimulationReport report = new SimulationReport(
                new ReportDate(LocalDateTime.now()),
                new TotalFlights(0),
                new TotalViolations(0),
                new ViolationThreshold(1),
                ValidationStatus.ABORTED,
                null,
                null,
                null,
                null,
                null
        );

        // Assert
        assertTrue(report.flightHistories().isEmpty());
        assertTrue(report.flightExecutionStatuses().isEmpty());
        assertTrue(report.safetyViolations().isEmpty());
    }

    @Test
    void flightViolationState_rejectsInvalidFlightId() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new FlightViolationState(
                0,
                new Position(0, 0),
                0,
                new SimulationTimestamp(0),
                null
        ));
    }

    @Test
    void flightHistorySnapshot_rejectsInvalidFlightId() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new FlightHistorySnapshot(
                -1,
                List.of(new HistoryPoint(
                        new SimulationTimestamp(0),
                        new Position(0, 0),
                        0
                ))
        ));
    }

    private static ReportDate newReportDateViaReflection() throws Exception {
        final Constructor<ReportDate> constructor = ReportDate.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static TotalFlights newTotalFlightsViaReflection() throws Exception {
        final Constructor<TotalFlights> constructor = TotalFlights.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static TotalViolations newTotalViolationsViaReflection() throws Exception {
        final Constructor<TotalViolations> constructor = TotalViolations.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static ViolationThreshold newViolationThresholdViaReflection() throws Exception {
        final Constructor<ViolationThreshold> constructor = ViolationThreshold.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static ReportFilePath newReportFilePathViaReflection() throws Exception {
        final Constructor<ReportFilePath> constructor = ReportFilePath.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static SimulationReport newSimulationReportViaReflection() throws Exception {
        final Constructor<SimulationReport> constructor = SimulationReport.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static SimulationReport sampleReport(final ValidationStatus status) {
        return new SimulationReport(
                new ReportDate(LocalDateTime.now()),
                new TotalFlights(1),
                new TotalViolations(0),
                new ViolationThreshold(100),
                status,
                List.of(),
                List.of(),
                List.of(),
                "1",
                null
        );
    }
}
