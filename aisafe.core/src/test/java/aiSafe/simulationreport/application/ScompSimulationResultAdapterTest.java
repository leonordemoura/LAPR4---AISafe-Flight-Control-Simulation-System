package aiSafe.simulationreport.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ScompSimulationResultAdapterTest {

    private final ScompSimulationResultAdapter adapter = new ScompSimulationResultAdapter();

    @Test
    void loadSimulationResult_parsesSucceededReportFromClasspath() {
        // Arrange
        final Path sample = Path.of("src/test/resources/scomp-report-succeeded.txt");

        // Act
        final SimulationRunResultDTO dto = adapter.loadSimulationResult(sample.toString());

        // Assert
        assertEquals(1, dto.totalFlights);
        assertEquals(0, dto.totalViolations);
        assertEquals(100, dto.violationThreshold);
        assertFalse(dto.validationFailure);
        assertEquals(1, dto.flightHistories.size());
        assertEquals(1, dto.flightStatuses.size());
        assertFalse(dto.earlyStop);
    }

    @Test
    void loadSimulationResult_parsesFailedReport() {
        // Arrange
        final Path sample = Path.of("src/test/resources/scomp-report-failed.txt");

        // Act
        final SimulationRunResultDTO dto = adapter.loadSimulationResult(sample.toString());

        // Assert
        assertEquals(2, dto.totalFlights);
        assertEquals(100, dto.totalViolations);
        assertTrue(dto.earlyStop);
        assertEquals(2, dto.flightHistories.size());
        assertEquals(2, dto.flightStatuses.size());
    }

    @Test
    void loadSimulationResult_parsesViolationsSection() {
        // Arrange
        final Path sample = Path.of("src/test/resources/scomp-report-with-violations.txt");

        // Act
        final SimulationRunResultDTO dto = adapter.loadSimulationResult(sample.toString());

        // Assert
        assertEquals(1, dto.violationEvents.size());
        assertEquals(1, dto.violationEvents.get(0).flightAId);
        assertEquals(2, dto.violationEvents.get(0).flightBId);
        assertFalse(dto.earlyStop);
    }

    @Test
    void parseReportContent_detectsValidationFailure() {
        // Arrange
        final String content = """
                ========= SIMULATION REPORT =========
                Missing departure airport.

                SIMULATION ABORTED :(

                ========= END OF REPORT =========
                """;

        // Act
        final SimulationRunResultDTO dto = adapter.parseReportContent(content);

        // Assert
        assertTrue(dto.validationFailure);
        assertEquals("Missing departure airport.", dto.validationMessage);
    }

    @Test
    void parseReportContent_usesDefaultValidationMessageWhenHeaderOnly() {
        // Arrange
        final String content = """
                ========= SIMULATION REPORT =========
                SIMULATION ABORTED :(
                """;

        // Act
        final SimulationRunResultDTO dto = adapter.parseReportContent(content);

        // Assert
        assertTrue(dto.validationFailure);
        assertEquals("Simulation validation failed.", dto.validationMessage);
    }

    @Test
    void loadSimulationResult_throwsWhenPathIsBlank() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> adapter.loadSimulationResult("  "));
    }

    @Test
    void loadSimulationResult_throwsWhenFileMissing() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> adapter.loadSimulationResult("does-not-exist.txt"));
    }

    @Test
    void loadSimulationResult_throwsWhenMetricsMissing(@TempDir Path tempDir) throws Exception {
        // Arrange
        final Path file = tempDir.resolve("invalid.txt");
        Files.writeString(file, "========= SIMULATION REPORT =========\n");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> adapter.loadSimulationResult(file.toString()));
    }

    @Test
    void parseReportContent_handlesMissingOptionalSections() {
        // Arrange
        final String content = """
                ========= SIMULATION REPORT =========
                Total Flights: 0
                Total Violations: 0
                Violation Threshold: 10

                -----------------------------------

                Simulation completed successfully without reaching the violation threshold.
                SIMULATION SUCCEEDED :)
                """;

        // Act
        final SimulationRunResultDTO dto = adapter.parseReportContent(content);

        // Assert
        assertTrue(dto.flightHistories.isEmpty());
        assertTrue(dto.flightStatuses.isEmpty());
        assertFalse(dto.earlyStop);
    }

    @Test
    void parseReportContent_ignoresMalformedViolationBlocks() {
        // Arrange
        final String content = """
                ========= SIMULATION REPORT =========
                Total Flights: 1
                Total Violations: 1
                Violation Threshold: 10

                -----------------------------------

                SAFETY VIOLATIONS:

                Violation #1:
                  Time: 09:05
                  Horizontal Distance: 0.5000, Vertical Distance: 1.0000

                -----------------------------------

                FLIGHT STATUS:

                Flight 1:
                  No violations detected.
                  Performance: 100.00

                Simulation completed successfully without reaching the violation threshold.
                SIMULATION SUCCEEDED :)
                """;

        // Act
        final SimulationRunResultDTO dto = adapter.parseReportContent(content);

        // Assert
        assertTrue(dto.violationEvents.isEmpty());
    }

    @Test
    void loadSimulationResult_throwsWhenFileNotReadable(@TempDir Path tempDir) throws Exception {
        // Arrange
        final Path dir = tempDir.resolve("not-a-file");
        Files.createDirectory(dir);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> adapter.loadSimulationResult(dir.toString()));
    }

    @Test
    void loadSimulationResult_parsesSprint3ReportFromClasspath() {
        // Arrange
        final Path sample = Path.of("src/test/resources/scomp-report-sprint3-succeeded.txt");

        // Act
        final SimulationRunResultDTO dto = adapter.loadSimulationResult(sample.toString());

        // Assert
        assertEquals(1, dto.totalFlights);
        assertEquals(0, dto.totalViolations);
        assertEquals(3, dto.violationThreshold);
        assertFalse(dto.validationFailure);
        assertFalse(dto.earlyStop);
        assertEquals(1, dto.flightHistories.size());
        assertEquals(123, dto.flightHistories.get(0).flightId);
        assertEquals(3, dto.flightHistories.get(0).points.size());
        assertEquals(41.242, dto.flightHistories.get(0).points.get(0).x, 0.001);
        assertEquals(1, dto.flightStatuses.size());
        assertEquals(123, dto.flightStatuses.get(0).flightId);
        assertEquals(100.0, dto.flightStatuses.get(0).performance);
        assertEquals("123", dto.relatedFlightDesignator);
    }

    @Test
    void parseReportContent_parsesSprint3ViolationsSection() {
        // Arrange
        final String content = """
                ========= SIMULATION REPORT =========
                Total Flights: 2
                Total Violations: 1
                Violation Threshold: 10

                -----------------------------------

                FLIGHT HISTORIES:
                  Flight 1 (Total Positions: 1, Violations: 1, Performance Score: 90.00%):
                    Pos: (1.000000, 2.000000)  Alt: 1000.000000  Vel: (1.000000, 0.000000, 0.000000)

                -----------------------------------

                SAFETY VIOLATIONS:

                Violation #1 (Global ID: 1):
                  Time: 09:05
                  Flight A: 1
                    Position: (1.000000, 2.000000)  Altitude: 1000.000000
                    Velocity: (1.000000, 0.000000, 0.000000)
                  Flight B: 2
                    Position: (3.000000, 4.000000)  Altitude: 1100.000000
                    Velocity: (0.000000, 1.000000, 0.000000)
                  Distance: Horizontal=0.5000 Vertical=100.0000

                -----------------------------------

                VERDICT:
                SIMULATION SUCCEEDED
                """;

        // Act
        final SimulationRunResultDTO dto = adapter.parseReportContent(content);

        // Assert
        assertEquals(1, dto.violationEvents.size());
        assertEquals(1, dto.violationEvents.get(0).flightAId);
        assertEquals(2, dto.violationEvents.get(0).flightBId);
        assertEquals(0.5, dto.violationEvents.get(0).horizontalDistance, 0.001);
        assertEquals(100.0, dto.violationEvents.get(0).verticalDistance, 0.001);
        assertFalse(dto.earlyStop);
    }

    @Test
    void loadSimulationResult_parsesRealSprint3ReportWhenPresent() {
        // Arrange
        final Path sample = Path.of("../SCOMP_SPRINT3/reports/report_2026-06-12_14-26-38.txt");
        if (!Files.isRegularFile(sample)) {
            return;
        }

        // Act
        final SimulationRunResultDTO dto = adapter.loadSimulationResult(sample.toString());

        // Assert
        assertEquals(1, dto.flightHistories.size());
        assertEquals(153, dto.flightHistories.get(0).points.size());
        assertEquals(123, dto.flightHistories.get(0).flightId);
        assertFalse(dto.earlyStop);
    }
}
