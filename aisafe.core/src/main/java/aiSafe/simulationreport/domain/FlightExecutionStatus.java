package aiSafe.simulationreport.domain;

import eapli.framework.domain.model.ValueObject;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public class FlightExecutionStatus implements ValueObject, Serializable {

    private final int flightId;
    private final int violationCount;
    private final Double performance;
    private final String summaryMessage;

    public FlightExecutionStatus(
            final int flightId,
            final int violationCount,
            final Double performance,
            final String summaryMessage
    ) {
        if (flightId <= 0) {
            throw new IllegalArgumentException("Flight id is required.");
        }
        if (violationCount < 0) {
            throw new IllegalArgumentException("Violation count cannot be negative.");
        }
        this.flightId = flightId;
        this.violationCount = violationCount;
        this.performance = performance;
        this.summaryMessage = Objects.requireNonNull(summaryMessage, "Summary message is required.");
    }

    public int flightId() {
        return flightId;
    }

    public int violationCount() {
        return violationCount;
    }

    public Optional<Double> performance() {
        return Optional.ofNullable(performance);
    }

    public String summaryMessage() {
        return summaryMessage;
    }
}
