package aiSafe.simulationreport.application;

import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.simulationreport.domain.*;
import aiSafe.simulationreport.repositories.SimulationReportRepository;
import eapli.framework.domain.repositories.TransactionalContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GenerateSimulationReportService {

    private final RepositoryFactory repositories;
    private final ScompSimulationResultAdapter simulationResultAdapter;
    private final SimulationReportFileWriter fileWriter;

    public GenerateSimulationReportService(final RepositoryFactory repositories) {
        this(repositories, new ScompSimulationResultAdapter(), new SimulationReportFileWriter());
    }

    public GenerateSimulationReportService(
            final RepositoryFactory repositories,
            final ScompSimulationResultAdapter simulationResultAdapter,
            final SimulationReportFileWriter fileWriter
    ) {
        this.repositories = Objects.requireNonNull(repositories);
        this.simulationResultAdapter = Objects.requireNonNull(simulationResultAdapter);
        this.fileWriter = Objects.requireNonNull(fileWriter);
    }

    public SimulationReport generateReport(
            final String outputDirectory,
            final String simulationLogPath,
            final TransactionalContext tx
    ) {
        final SimulationRunResultDTO dto = simulationResultAdapter.loadSimulationResult(simulationLogPath);
        final SimulationReport report = buildAggregate(dto);
        final String formatted = SimulationReportTextFormatter.format(report);
        final ReportFilePath path = fileWriter.write(formatted, outputDirectory);
        report.attachFilePath(path);
        return simulationReportRepository(tx).save(report);
    }

    SimulationReport buildAggregate(final SimulationRunResultDTO dto) {
        Objects.requireNonNull(dto, "Simulation result is required.");

        final ValidationStatus status = resolveValidationStatus(dto);
        final List<FlightHistorySnapshot> histories = mapHistories(dto);
        final List<FlightExecutionStatus> statuses = mapStatuses(dto);
        final List<SafetyViolation> violations = mapViolations(dto);

        return new SimulationReport(
                new ReportDate(LocalDateTime.now()),
                new TotalFlights(dto.totalFlights),
                new TotalViolations(dto.totalViolations),
                new ViolationThreshold(dto.violationThreshold),
                status,
                histories,
                statuses,
                violations,
                dto.relatedFlightDesignator,
                dto.validationMessage
        );
    }

    static ValidationStatus resolveValidationStatus(final SimulationRunResultDTO dto) {
        if (dto.validationFailure) {
            return ValidationStatus.ABORTED;
        }
        if (dto.totalViolations >= dto.violationThreshold && dto.violationThreshold > 0) {
            return ValidationStatus.FAILED;
        }
        if (dto.earlyStop) {
            return ValidationStatus.ABORTED;
        }
        return ValidationStatus.SUCCEEDED;
    }

    private static List<FlightHistorySnapshot> mapHistories(final SimulationRunResultDTO dto) {
        final List<FlightHistorySnapshot> result = new ArrayList<>();
        for (final FlightHistorySnapshotDTO historyDto : dto.flightHistories) {
            final List<HistoryPoint> points = new ArrayList<>();
            for (final HistoryPointDTO pointDto : historyDto.points) {
                points.add(new HistoryPoint(
                        new SimulationTimestamp(pointDto.minutesSinceMidnight),
                        new Position(pointDto.x, pointDto.y),
                        pointDto.altitude
                ));
            }
            if (!points.isEmpty()) {
                result.add(new FlightHistorySnapshot(historyDto.flightId, points));
            }
        }
        return result;
    }

    private static List<FlightExecutionStatus> mapStatuses(final SimulationRunResultDTO dto) {
        final List<FlightExecutionStatus> result = new ArrayList<>();
        for (final FlightExecutionStatusDTO statusDto : dto.flightStatuses) {
            result.add(new FlightExecutionStatus(
                    statusDto.flightId,
                    statusDto.violationCount,
                    statusDto.performance,
                    statusDto.summaryMessage
            ));
        }
        return result;
    }

    private static List<SafetyViolation> mapViolations(final SimulationRunResultDTO dto) {
        final List<SafetyViolation> result = new ArrayList<>();
        for (final SafetyViolationEventDTO event : dto.violationEvents) {
            final FlightViolationState flightA = new FlightViolationState(
                    event.flightAId,
                    new Position(event.xA, event.yA),
                    event.altA,
                    new SimulationTimestamp(event.minutesA),
                    new VelocityVector(event.vxA, event.vyA, event.vzA)
            );
            final FlightViolationState flightB = new FlightViolationState(
                    event.flightBId,
                    new Position(event.xB, event.yB),
                    event.altB,
                    new SimulationTimestamp(event.minutesB),
                    new VelocityVector(event.vxB, event.vyB, event.vzB)
            );
            result.add(new SafetyViolation(flightA, flightB, event.horizontalDistance, event.verticalDistance));
        }
        return result;
    }

    private SimulationReportRepository simulationReportRepository(final TransactionalContext tx) {
        return repositories.simulationReports(tx);
    }
}
