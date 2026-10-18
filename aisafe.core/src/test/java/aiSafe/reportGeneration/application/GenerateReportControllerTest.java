package aiSafe.reportGeneration.application;

import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircontrolarea.repositories.AirControlAreaRepository;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.reportGeneration.ReportTestFixtures;
import aiSafe.reportGeneration.domain.ReportType;
import aiSafe.reportGeneration.repositories.ReportRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryTransactionalContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.file.Path;
import java.time.YearMonth;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GenerateReportControllerTest {

    @Mock private RepositoryFactory repositoryFactory;
    @Mock private AirControlAreaRepository areaRepository;
    @Mock private ReportRepository reportRepository;

    @TempDir
    Path tempDir;

    @Test
    void generateReport_commitsTransactionOnSuccess() {
        final AtomicBoolean committed = new AtomicBoolean(false);
        final AtomicBoolean rolledBack = new AtomicBoolean(false);
        final TransactionalContext tx = new TrackingTransactionalContext(committed, rolledBack);

        when(repositoryFactory.newTransactionalContext()).thenReturn(tx);
        when(repositoryFactory.airControlAreas(tx)).thenReturn(areaRepository);
        when(areaRepository.findByAreaCode(new AreaCode("LIS")))
                .thenReturn(Optional.of(ReportTestFixtures.sampleArea()));
        when(repositoryFactory.reports(tx)).thenReturn(reportRepository);

        final GenerateReportService service = new GenerateReportService(
                repositoryFactory,
                ReportTestFixtures.monthlyReportFactory(),
                (report, dir) -> dir + "/monthly_2026-06_LIS.pdf");

        final GenerateReportController controller =
                new GenerateReportController(repositoryFactory, service);

        final String path = controller.generateReport(
                YearMonth.of(2026, 6),
                ReportType.MONTHLY_STATISTICS,
                "LIS",
                tempDir.toString());

        assertTrue(path.endsWith(".pdf"));
        assertTrue(committed.get());
        assertFalse(rolledBack.get());
        verify(reportRepository).save(any());
    }

    @Test
    void generateReport_rollsBackOnFailure() {
        final AtomicBoolean committed = new AtomicBoolean(false);
        final AtomicBoolean rolledBack = new AtomicBoolean(false);
        final TransactionalContext tx = new TrackingTransactionalContext(committed, rolledBack);

        when(repositoryFactory.newTransactionalContext()).thenReturn(tx);
        when(repositoryFactory.airControlAreas(tx)).thenReturn(areaRepository);
        when(areaRepository.findByAreaCode(new AreaCode("LIS")))
                .thenReturn(Optional.of(ReportTestFixtures.sampleArea()));
        when(repositoryFactory.reports(tx)).thenReturn(reportRepository);

        final GenerateReportService failingService = new GenerateReportService(
                repositoryFactory,
                ReportTestFixtures.monthlyReportFactory(),
                (report, dir) -> {
                    throw new IllegalArgumentException("render failed");
                });

        final GenerateReportController controller =
                new GenerateReportController(repositoryFactory, failingService);

        assertThrows(IllegalArgumentException.class, () -> controller.generateReport(
                YearMonth.of(2026, 6),
                ReportType.MONTHLY_STATISTICS,
                "LIS",
                tempDir.toString()));

        assertFalse(committed.get());
        assertTrue(rolledBack.get());
        verify(reportRepository).save(any());
    }

    @Test
    void getAirControlArea_delegatesToRepository() {
        when(repositoryFactory.airControlAreas()).thenReturn(areaRepository);
        when(areaRepository.getAirControlAreasBasedOnUser(any()))
                .thenReturn(java.util.List.of(ReportTestFixtures.sampleArea()));

        final GenerateReportService service = new GenerateReportService(
                repositoryFactory,
                ReportTestFixtures.monthlyReportFactory(),
                (report, dir) -> dir);

        final GenerateReportController controller =
                new GenerateReportController(repositoryFactory, service);

        final Iterable<?> areas = controller.getAirControlArea();

        assertNotNull(areas);
        verify(areaRepository).getAirControlAreasBasedOnUser(any());
    }

    private static final class TrackingTransactionalContext extends InMemoryTransactionalContext {
        private final AtomicBoolean committed;
        private final AtomicBoolean rolledBack;

        private TrackingTransactionalContext(final AtomicBoolean committed, final AtomicBoolean rolledBack) {
            this.committed = committed;
            this.rolledBack = rolledBack;
        }

        @Override
        public void commit() {
            committed.set(true);
            super.commit();
        }

        @Override
        public void rollback() {
            rolledBack.set(true);
            super.rollback();
        }
    }
}
