package aiSafe.reportGeneration.domain;

import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.reportGeneration.ReportTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportDomainTest {

    @Test
    void report_create_buildsAggregateWithOrderedSections() {
        // Arrange & Act
        final Report report = ReportTestFixtures.sampleMonthlyReport();

        // Assert
        assertEquals(ReportType.MONTHLY_STATISTICS, report.type());
        assertEquals(YearMonth.of(2026, 6), report.period().period());
        assertEquals("LIS", report.areaCode().value());
        assertEquals(5, report.sections().size());
        assertEquals("Executive Summary", report.sections().get(0).title());
        assertEquals("Statistical Charts", report.sections().get(4).title());
        assertTrue(report.toString().contains("Monthly Statistics"));
    }

    @Test
    void report_rejectsEmptySections() {
        // Arrange
        final Executable action = () -> Report.create(
                ReportTitle.valueOf("Monthly Statistics – 2026-06 – LIS"),
                ReportPeriod.valueOf(YearMonth.of(2026, 6)),
                ReportHeader.valueOf(LocalDateTime.now(), "operator@company.com"),
                ReportType.MONTHLY_STATISTICS,
                new AreaCode("LIS"),
                List.of());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void report_equalsUsesIdentityWhenPresent() {
        final Report a = ReportTestFixtures.sampleMonthlyReport();
        final Report b = ReportTestFixtures.sampleMonthlyReport();

        assertNotEquals(a, b);
        assertNotEquals(a, null);
        assertEquals(a, a);
        assertTrue(a.sameAs(a));
        assertFalse(a.sameAs(null));
    }

    @Test
    void reportPeriod_rejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> ReportPeriod.valueOf(null));
    }

    @Test
    void reportPeriod_equalsAndToString() {
        final ReportPeriod period = ReportPeriod.valueOf(YearMonth.of(2026, 6));
        assertEquals("2026-06", period.toString());
        assertEquals(period, ReportPeriod.valueOf(YearMonth.of(2026, 6)));
    }

    @Test
    void reportTitle_rejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> ReportTitle.valueOf("  "));
        assertThrows(IllegalArgumentException.class, () -> ReportTitle.valueOf(null));
    }

    @Test
    void reportHeader_rejectsBlankUser() {
        assertThrows(IllegalArgumentException.class,
                () -> ReportHeader.valueOf(LocalDateTime.now(), "  "));
        assertThrows(IllegalArgumentException.class,
                () -> ReportHeader.valueOf(null, "user@test.com"));
    }

    @Test
    void reportHeader_equalsAndToString() {
        final LocalDateTime at = LocalDateTime.of(2026, 6, 1, 10, 0);
        final ReportHeader header = ReportHeader.valueOf(at, "operator@company.com");
        assertEquals(header, ReportHeader.valueOf(at, "operator@company.com"));
        assertTrue(header.toString().contains("operator@company.com"));
    }

    @Test
    void reportSection_equalsByTitleAndContent() {
        final ReportSection first = new ReportSection("A", "content");
        final ReportSection second = new ReportSection("A", "content");
        final ReportSection third = new ReportSection("B", "content");

        assertEquals(first, second);
        assertNotEquals(first, third);
        assertTrue(first.toString().contains("A"));
    }

    @Test
    void reportType_exposesHumanReadableDescription() {
        assertEquals("Monthly Statistics", ReportType.MONTHLY_STATISTICS.toString());
        assertEquals("Incident Analysis", ReportType.INCIDENT_ANALYSIS.toString());
        assertEquals("Compliance Report", ReportType.COMPLIANCE_REPORT.toString());
    }
}
