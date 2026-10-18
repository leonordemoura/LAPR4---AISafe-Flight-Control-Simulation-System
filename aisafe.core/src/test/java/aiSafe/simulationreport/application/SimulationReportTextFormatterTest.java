package aiSafe.simulationreport.application;

import aiSafe.simulationreport.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulationReportTextFormatterTest {

    @Test
    void format_includesTotalsAndSucceededVerdict() {
        // Arrange
        final SimulationReport report = succeededReport();

        // Act
        final String text = SimulationReportTextFormatter.format(report);

        // Assert
        assertTrue(text.contains("Total Flights: 1"));
        assertTrue(text.contains("FLIGHT STATUS:"));
        assertTrue(text.contains("SIMULATION SUCCEEDED"));
    }

    @Test
    void format_includesFailedVerdict() {
        // Arrange
        final SimulationReport report = succeededReport();
        final SimulationReport failed = new SimulationReport(
                report.reportDate(),
                report.totalFlights(),
                new TotalViolations(100),
                report.violationThreshold(),
                ValidationStatus.FAILED,
                report.flightHistories(),
                report.flightExecutionStatuses(),
                report.safetyViolations(),
                "1",
                null
        );

        // Act
        final String text = SimulationReportTextFormatter.format(failed);

        // Assert
        assertTrue(text.contains("SIMULATION FAILED"));
        assertTrue(text.contains("Unable to calculate performance"));
    }

    @Test
    void format_includesAbortedVerdict() {
        // Arrange
        final SimulationReport report = succeededReport();
        final SimulationReport aborted = new SimulationReport(
                report.reportDate(),
                report.totalFlights(),
                report.totalViolations(),
                report.violationThreshold(),
                ValidationStatus.ABORTED,
                report.flightHistories(),
                report.flightExecutionStatuses(),
                report.safetyViolations(),
                "1",
                null
        );

        // Act
        final String text = SimulationReportTextFormatter.format(aborted);

        // Assert
        assertTrue(text.contains("SIMULATION ABORTED"));
    }

    @Test
    void format_validationFailureUsesCustomMessage() {
        // Arrange
        final SimulationReport report = new SimulationReport(
                new ReportDate(LocalDateTime.now()),
                new TotalFlights(0),
                new TotalViolations(0),
                new ViolationThreshold(0),
                ValidationStatus.ABORTED,
                List.of(),
                List.of(),
                List.of(),
                null,
                "Missing departure airport."
        );

        // Act
        final String text = SimulationReportTextFormatter.format(report);

        // Assert
        assertTrue(text.contains("Missing departure airport."));
        assertTrue(text.contains("SIMULATION ABORTED"));
    }

    @Test
    void format_validationFailureUsesDefaultMessageWhenNull() {
        // Arrange
        final SimulationReport report = new SimulationReport(
                new ReportDate(LocalDateTime.now()),
                new TotalFlights(0),
                new TotalViolations(0),
                new ViolationThreshold(0),
                ValidationStatus.ABORTED,
                List.of(),
                List.of(),
                List.of(),
                null,
                null
        );

        // Act
        final String text = SimulationReportTextFormatter.format(report);

        // Assert
        assertTrue(text.contains("Simulation validation failed."));
    }

    @Test
    void format_includesViolationSectionWhenPresent() {
        // Arrange
        final SimulationReport report = reportWithViolation();

        // Act
        final String text = SimulationReportTextFormatter.format(report);

        // Assert
        assertTrue(text.contains("SAFETY VIOLATIONS:"));
        assertTrue(text.contains("Violation #1:"));
        assertTrue(text.contains("Horizontal Distance:"));
    }

    @Test
    void format_handlesReportWithoutFlightStatusRows() {
        // Arrange
        final SimulationReport report = new SimulationReport(
                new ReportDate(LocalDateTime.now()),
                new TotalFlights(0),
                new TotalViolations(0),
                new ViolationThreshold(1),
                ValidationStatus.SUCCEEDED,
                List.of(),
                List.of(),
                List.of(),
                null,
                null
        );

        // Act
        final String text = SimulationReportTextFormatter.format(report);

        // Assert
        assertTrue(text.contains("FLIGHT STATUS:"));
        assertTrue(text.contains("SIMULATION SUCCEEDED"));
    }

    @Test
    void format_showsPerFlightViolationCountInStatusSection() {
        // Arrange
        final SimulationReport report = new SimulationReport(
                new ReportDate(LocalDateTime.now()),
                new TotalFlights(1),
                new TotalViolations(2),
                new ViolationThreshold(100),
                ValidationStatus.SUCCEEDED,
                List.of(),
                List.of(new FlightExecutionStatus(1, 2, 90.0, "ignored")),
                List.of(),
                "1",
                null
        );

        // Act
        final String text = SimulationReportTextFormatter.format(report);

        // Assert
        assertTrue(text.contains("  Total Violations: 2"));
    }

    private static SimulationReport succeededReport() {
        return new SimulationReport(
                new ReportDate(LocalDateTime.of(2026, 5, 16, 11, 0)),
                new TotalFlights(1),
                new TotalViolations(0),
                new ViolationThreshold(100),
                ValidationStatus.SUCCEEDED,
                List.of(new FlightHistorySnapshot(1, List.of(
                        new HistoryPoint(
                                new SimulationTimestamp(540),
                                new Position(0, 0),
                                1000
                        )
                ))),
                List.of(new FlightExecutionStatus(1, 0, 100.0, "No violations detected.")),
                List.of(),
                "1",
                null
        );
    }

    private static SimulationReport reportWithViolation() {
        final FlightViolationState flightA = new FlightViolationState(
                1,
                new Position(1, 2),
                1000,
                new SimulationTimestamp(540),
                new VelocityVector(1, 0, 0)
        );
        final FlightViolationState flightB = new FlightViolationState(
                2,
                new Position(3, 4),
                1100,
                new SimulationTimestamp(541),
                null
        );

        return new SimulationReport(
                new ReportDate(LocalDateTime.now()),
                new TotalFlights(2),
                new TotalViolations(1),
                new ViolationThreshold(100),
                ValidationStatus.SUCCEEDED,
                List.of(),
                List.of(),
                List.of(new SafetyViolation(flightA, flightB, 0.5, 100)),
                null,
                null
        );
    }
}
