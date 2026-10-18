package aiSafe.simulationreport.domain;

import eapli.framework.domain.model.ValueObject;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public class FlightViolationState implements ValueObject, Serializable {

    private final int flightId;
    private final Position position;
    private final double altitude;
    private final SimulationTimestamp timestamp;
    private final VelocityVector velocity;

    public FlightViolationState(
            final int flightId,
            final Position position,
            final double altitude,
            final SimulationTimestamp timestamp,
            final VelocityVector velocity
    ) {
        if (flightId <= 0) {
            throw new IllegalArgumentException("Flight id is required.");
        }
        this.flightId = flightId;
        this.position = Objects.requireNonNull(position, "Position is required.");
        this.altitude = altitude;
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp is required.");
        this.velocity = velocity;
    }

    public int flightId() {
        return flightId;
    }

    public Position position() {
        return position;
    }

    public double altitude() {
        return altitude;
    }

    public SimulationTimestamp timestamp() {
        return timestamp;
    }

    public Optional<VelocityVector> velocity() {
        return Optional.ofNullable(velocity);
    }
}
