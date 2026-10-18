package aiSafe.simulationreport.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimulationRunResultDTO {

    public final int totalFlights;
    public final int totalViolations;
    public final int violationThreshold;
    public final boolean earlyStop;
    public final boolean validationFailure;
    public final String validationMessage;
    public final String relatedFlightDesignator;
    public final List<FlightHistorySnapshotDTO> flightHistories;
    public final List<FlightExecutionStatusDTO> flightStatuses;
    public final List<SafetyViolationEventDTO> violationEvents;

    public SimulationRunResultDTO(
            final int totalFlights,
            final int totalViolations,
            final int violationThreshold,
            final boolean earlyStop,
            final boolean validationFailure,
            final String validationMessage,
            final String relatedFlightDesignator,
            final List<FlightHistorySnapshotDTO> flightHistories,
            final List<FlightExecutionStatusDTO> flightStatuses,
            final List<SafetyViolationEventDTO> violationEvents
    ) {
        this.totalFlights = totalFlights;
        this.totalViolations = totalViolations;
        this.violationThreshold = violationThreshold;
        this.earlyStop = earlyStop;
        this.validationFailure = validationFailure;
        this.validationMessage = validationMessage;
        this.relatedFlightDesignator = relatedFlightDesignator;
        this.flightHistories = flightHistories == null
                ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(flightHistories));
        this.flightStatuses = flightStatuses == null
                ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(flightStatuses));
        this.violationEvents = violationEvents == null
                ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(violationEvents));
    }
}
