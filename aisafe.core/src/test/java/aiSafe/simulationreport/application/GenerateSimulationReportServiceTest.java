package aiSafe.simulationreport.application;

import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.simulationreport.domain.*;
import aiSafe.simulationreport.repositories.SimulationReportRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateSimulationReportServiceTest {

    @Mock private RepositoryFactory repositoryFactory;
    @Mock private SimulationReportRepository simulationReportRepository;
    @Mock private TransactionalContext tx;
    @Mock private ScompSimulationResultAdapter adapter;
    @Mock private SimulationReportFileWriter fileWriter;

    @TempDir
    Path tempDir;

    @Test
    void resolveValidationStatus_returnsSucceeded_whenBelowThreshold() {
        // Arrange
        final SimulationRunResultDTO dto = new SimulationRunResultDTO(
                1, 2, 100, false, false, null, null, List.of(), List.of(), List.of());

        // Act
        final ValidationStatus status = GenerateSimulationReportService.resolveValidationStatus(dto);

        // Assert
        assertEquals(ValidationStatus.SUCCEEDED, status);
    }

    @Test
    void resolveValidationStatus_returnsFailed_whenViolationsReachThreshold() {
        // Arrange
        final SimulationRunResultDTO dto = new SimulationRunResultDTO(
                1, 100, 100, true, false, null, null, List.of(), List.of(), List.of());

        // Act
        final ValidationStatus status = GenerateSimulationReportService.resolveValidationStatus(dto);

        // Assert
        assertEquals(ValidationStatus.FAILED, status);
    }

    @Test
    void resolveValidationStatus_returnsAborted_onValidationFailure() {
        // Arrange
        final SimulationRunResultDTO dto = new SimulationRunResultDTO(
                0, 0, 0, true, true, "Invalid plans", null, List.of(), List.of(), List.of());

        // Act
        final ValidationStatus status = GenerateSimulationReportService.resolveValidationStatus(dto);

        // Assert
        assertEquals(ValidationStatus.ABORTED, status);
    }

    @Test
    void resolveValidationStatus_returnsAborted_onEarlyStopWithoutThreshold() {
        // Arrange
        final SimulationRunResultDTO dto = new SimulationRunResultDTO(
                1, 1, 100, true, false, null, null, List.of(), List.of(), List.of());

        // Act
        final ValidationStatus status = GenerateSimulationReportService.resolveValidationStatus(dto);

        // Assert
        assertEquals(ValidationStatus.ABORTED, status);
    }

    @Test
    void buildAggregate_mapsDtoToDomain() {
        // Arrange
        final GenerateSimulationReportService service =
                new GenerateSimulationReportService(repositoryFactory, adapter, fileWriter);

        final SimulationRunResultDTO dto = new SimulationRunResultDTO(
                1, 0, 100, false, false, null, "1",
                List.of(new FlightHistorySnapshotDTO(1, List.of(
                        new HistoryPointDTO(540, 0, 0, 1000)
                ))),
                List.of(new FlightExecutionStatusDTO(1, 0, 100.0, "No violations detected.")),
                List.of(new SafetyViolationEventDTO(
                        1, 2, 540, 541,
                        1, 2, 1000, 3, 4, 1100,
                        1, 0, 0, 0, 1, 0,
                        0.5, 100
                ))
        );

        // Act
        final SimulationReport report = service.buildAggregate(dto);

        // Assert
        assertEquals(ValidationStatus.SUCCEEDED, report.validationStatus());
        assertEquals(1, report.flightHistories().size());
        assertEquals(1, report.flightExecutionStatuses().size());
        assertEquals(1, report.safetyViolations().size());
        assertEquals("1", report.relatedFlightDesignator());
    }

    @Test
    void buildAggregate_rejectsNullDto() {
        // Arrange
        final GenerateSimulationReportService service =
                new GenerateSimulationReportService(repositoryFactory, adapter, fileWriter);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> service.buildAggregate(null));
    }

    @Test
    void generateReport_persistsFormattedReport() {
        // Arrange
        when(repositoryFactory.simulationReports(tx)).thenReturn(simulationReportRepository);
        final GenerateSimulationReportService service =
                new GenerateSimulationReportService(repositoryFactory, adapter, fileWriter);

        final SimulationRunResultDTO dto = new SimulationRunResultDTO(
                1, 0, 100, false, false, null, "1",
                List.of(new FlightHistorySnapshotDTO(1, List.of(
                        new HistoryPointDTO(540, 0, 0, 1000)
                ))),
                List.of(new FlightExecutionStatusDTO(1, 0, 100.0, "No violations detected.")),
                List.of()
        );

        when(adapter.loadSimulationResult("log.txt")).thenReturn(dto);
        when(fileWriter.write(any(), eq("reports"))).thenReturn(
                new ReportFilePath(tempDir.resolve("report.txt").toString()));
        when(simulationReportRepository.save(any(SimulationReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        final SimulationReport saved = service.generateReport("reports", "log.txt", tx);

        // Assert
        assertEquals(ValidationStatus.SUCCEEDED, saved.validationStatus());
        assertNotNull(saved.filePath());
        verify(simulationReportRepository).save(saved);
        verify(fileWriter).write(any(), eq("reports"));
    }

    @Test
    void generateReport_endToEndFromScompFile() {
        // Arrange
        when(repositoryFactory.simulationReports(tx)).thenReturn(simulationReportRepository);
        final GenerateSimulationReportService service = new GenerateSimulationReportService(repositoryFactory);
        final String logPath = Path.of("src/test/resources/scomp-report-succeeded.txt").toString();

        when(simulationReportRepository.save(any(SimulationReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        final SimulationReport saved = service.generateReport(tempDir.toString(), logPath, tx);

        // Assert
        assertEquals(ValidationStatus.SUCCEEDED, saved.validationStatus());
        assertNotNull(saved.filePath());
        verify(simulationReportRepository).save(saved);
    }
}
