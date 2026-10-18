package aiSafe.reportGeneration.application;

import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.aircontrolarea.repositories.AirControlAreaRepository;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.reportGeneration.ReportTestFixtures;
import aiSafe.reportGeneration.domain.Report;
import aiSafe.reportGeneration.domain.ReportFactory;
import aiSafe.reportGeneration.domain.ReportSection;
import aiSafe.reportGeneration.domain.ReportType;
import aiSafe.reportGeneration.repositories.ReportRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryTransactionalContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateReportServiceTest {

    @Mock private RepositoryFactory repositoryFactory;
    @Mock private AirControlAreaRepository areaRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private AirControlAreaRepository areasWithoutTx;

    @TempDir
    Path tempDir;

    @Test
    void generateReport_persistsAndRendersMonthlyReport() {
        // Arrange
        final ReportFactory reportFactory = new ReportFactory(Map.of(
                ReportType.MONTHLY_STATISTICS,
                (period, areaCode) -> ReportTestFixtures.sampleMonthlyReport().sections()));

        final CaptureRenderer captureRenderer = new CaptureRenderer();
        final GenerateReportService service =
                new GenerateReportService(repositoryFactory, reportFactory, captureRenderer);
        final TransactionalContext tx = new InMemoryTransactionalContext();

        when(repositoryFactory.airControlAreas(tx)).thenReturn(areaRepository);
        when(areaRepository.findByAreaCode(new AreaCode("LIS")))
                .thenReturn(Optional.of(ReportTestFixtures.sampleArea()));
        when(repositoryFactory.reports(tx)).thenReturn(reportRepository);

        // Act
        final String path = service.generateReport(
                YearMonth.of(2026, 6),
                ReportType.MONTHLY_STATISTICS,
                "LIS",
                tempDir.toString(),
                ReportTestFixtures.operatorEmail(),
                tx);

        // Assert
        assertNotNull(path);
        assertTrue(path.endsWith("monthly_2026-06_LIS.pdf"));
        verify(reportRepository).save(any(Report.class));
        assertEquals(ReportType.MONTHLY_STATISTICS, captureRenderer.lastReport.type());
    }

    @Test
    void generateReport_rejectsUnknownArea() {
        final ReportFactory reportFactory = new ReportFactory(Map.of(
                ReportType.MONTHLY_STATISTICS,
                (period, areaCode) -> List.of(new ReportSection("S", "C"))));
        final GenerateReportService service = new GenerateReportService(
                repositoryFactory, reportFactory, (report, dir) -> dir + "/unused.pdf");
        final TransactionalContext tx = new InMemoryTransactionalContext();

        when(repositoryFactory.airControlAreas(tx)).thenReturn(areaRepository);
        when(areaRepository.findByAreaCode(new AreaCode("XXX"))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.generateReport(
                YearMonth.of(2026, 6),
                ReportType.MONTHLY_STATISTICS,
                "XXX",
                tempDir.toString(),
                ReportTestFixtures.operatorEmail(),
                tx));

        verify(reportRepository, never()).save(any());
    }

    @Test
    void getAirControlArea_delegatesToRepository() {
        final GenerateReportService service = new GenerateReportService(
                repositoryFactory,
                ReportTestFixtures.monthlyReportFactory(),
                (report, dir) -> dir);

        when(repositoryFactory.airControlAreas()).thenReturn(areasWithoutTx);
        when(areasWithoutTx.getAirControlAreasBasedOnUser(ReportTestFixtures.operatorEmail()))
                .thenReturn(List.of(ReportTestFixtures.sampleArea()));

        final Iterable<AirControlArea> areas = service.getAirControlArea(ReportTestFixtures.operatorEmail());

        assertNotNull(areas);
        verify(areasWithoutTx).getAirControlAreasBasedOnUser(ReportTestFixtures.operatorEmail());
    }

    @Test
    void constructor_rejectsNullDependencies() {
        assertThrows(IllegalArgumentException.class, () ->
                new GenerateReportService(null, ReportTestFixtures.monthlyReportFactory(), (r, d) -> d));
        assertThrows(IllegalArgumentException.class, () ->
                new GenerateReportService(repositoryFactory, null, (r, d) -> d));
        assertThrows(IllegalArgumentException.class, () ->
                new GenerateReportService(repositoryFactory, ReportTestFixtures.monthlyReportFactory(), null));
    }

    private static final class CaptureRenderer implements aiSafe.reportGeneration.domain.ReportRenderer {
        private Report lastReport;

        @Override
        public String render(final Report report, final String outputDirectory) {
            this.lastReport = report;
            return outputDirectory + "/monthly_2026-06_LIS.pdf";
        }
    }
}
