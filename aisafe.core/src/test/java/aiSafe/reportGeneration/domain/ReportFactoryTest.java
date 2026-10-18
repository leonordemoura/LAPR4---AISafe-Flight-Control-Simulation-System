package aiSafe.reportGeneration.domain;

import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.reportGeneration.ReportTestFixtures;
import aiSafe.usermanagement.domain.Email;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportFactoryTest {

    @Test
    void create_assemblesMonthlyStatisticsReport() {
        // Arrange
        final Map<ReportType, ReportDataCollector> collectors = new EnumMap<>(ReportType.class);
        collectors.put(ReportType.MONTHLY_STATISTICS, (period, areaCode) -> List.of(
                new ReportSection("Executive Summary", "Total flights    : 1")));

        final ReportFactory factory = new ReportFactory(collectors);

        // Act
        final Report report = factory.create(
                ReportType.MONTHLY_STATISTICS,
                YearMonth.of(2026, 6),
                new AreaCode("LIS"),
                ReportTestFixtures.operatorEmail());

        // Assert
        assertEquals(ReportType.MONTHLY_STATISTICS, report.type());
        assertEquals(YearMonth.of(2026, 6), report.period().period());
        assertEquals("LIS", report.areaCode().value());
        assertTrue(report.title().toString().contains("2026-06"));
        assertFalse(report.sections().isEmpty());
    }

    @Test
    void create_rejectsUnregisteredReportType() {
        final ReportFactory factory = new ReportFactory(Map.of(
                ReportType.MONTHLY_STATISTICS,
                (period, areaCode) -> List.of(new ReportSection("S", "C"))));

        assertThrows(IllegalArgumentException.class, () -> factory.create(
                ReportType.INCIDENT_ANALYSIS,
                YearMonth.of(2026, 6),
                new AreaCode("LIS"),
                new Email("op@company.com")));
    }

    @Test
    void create_rejectsNullArguments() {
        final ReportFactory factory = ReportTestFixtures.monthlyReportFactory();

        assertThrows(IllegalArgumentException.class, () -> factory.create(
                null, YearMonth.of(2026, 6), new AreaCode("LIS"), ReportTestFixtures.operatorEmail()));
        assertThrows(IllegalArgumentException.class, () -> factory.create(
                ReportType.MONTHLY_STATISTICS, null, new AreaCode("LIS"), ReportTestFixtures.operatorEmail()));
        assertThrows(NullPointerException.class, () -> factory.create(
                ReportType.MONTHLY_STATISTICS, YearMonth.of(2026, 6), null, ReportTestFixtures.operatorEmail()));
        assertThrows(IllegalArgumentException.class, () -> factory.create(
                ReportType.MONTHLY_STATISTICS, YearMonth.of(2026, 6), new AreaCode("LIS"), null));
    }

    @Test
    void create_rejectsEmptyCollectorOutput() {
        final ReportFactory factory = new ReportFactory(Map.of(
                ReportType.MONTHLY_STATISTICS,
                (period, areaCode) -> List.of()));

        assertThrows(IllegalStateException.class, () -> factory.create(
                ReportType.MONTHLY_STATISTICS,
                YearMonth.of(2026, 6),
                new AreaCode("LIS"),
                ReportTestFixtures.operatorEmail()));
    }

    @Test
    void create_rejectsInvalidFactoryConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new ReportFactory(null));
        assertThrows(IllegalArgumentException.class, () -> new ReportFactory(Map.of()));
    }
}
