package aiSafe.reportGeneration.application;

import aiSafe.reportGeneration.ReportTestFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReportMetricsExtractorTest {

    @Test
    void extract_parsesMetricsFromReportSections() {
        // Arrange
        final var report = ReportTestFixtures.sampleMonthlyReport();

        // Act
        final ReportMetricsExtractor.ReportMetrics metrics = ReportMetricsExtractor.extract(report);

        // Assert
        assertEquals(2, metrics.totalFlights);
        assertEquals(1, metrics.totalSimulations);
        assertEquals(3, metrics.totalViolations);
        assertEquals(1, metrics.regularFlights);
        assertEquals(1, metrics.charterFlights);
        assertEquals(50.0, metrics.validationRate, 0.01);
        assertEquals(1, metrics.simulationsWithViolations);
        assertFalse(metrics.simulationOutcomes.isEmpty());
        assertTrue(metrics.simulationOutcomes.containsKey("FAILED"));
    }

    @Test
    void extract_returnsZerosForMinimalReport() {
        final var report = ReportTestFixtures.minimalMonthlyReport();

        final ReportMetricsExtractor.ReportMetrics metrics = ReportMetricsExtractor.extract(report);

        assertEquals(0, metrics.totalFlights);
        assertEquals(0, metrics.totalSimulations);
        assertEquals(0, metrics.totalViolations);
        assertEquals(0, metrics.simulationsWithViolations);
    }
}
