package aiSafe.simulationreport.domain;

import eapli.framework.domain.model.ValueObject;

import java.io.Serializable;
import java.util.Objects;

public class HistoryPoint implements ValueObject, Serializable {

    private final SimulationTimestamp timestamp;
    private final Position position;
    private final double altitude;

    public HistoryPoint(
            final SimulationTimestamp timestamp,
            final Position position,
            final double altitude
    ) {
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp is required.");
        this.position = Objects.requireNonNull(position, "Position is required.");
        this.altitude = altitude;
    }

    public SimulationTimestamp timestamp() {
        return timestamp;
    }

    public Position position() {
        return position;
    }

    public double altitude() {
        return altitude;
    }
}
