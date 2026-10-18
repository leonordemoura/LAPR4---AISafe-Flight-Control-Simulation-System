package aiSafe.simulationreport.domain;

import eapli.framework.domain.model.ValueObject;

import java.io.Serializable;
import java.util.Objects;

public class SafetyViolation implements ValueObject, Serializable {

    private final FlightViolationState flightA;
    private final FlightViolationState flightB;
    private final double horizontalDistance;
    private final double verticalDistance;

    public SafetyViolation(
            final FlightViolationState flightA,
            final FlightViolationState flightB,
            final double horizontalDistance,
            final double verticalDistance
    ) {
        this.flightA = Objects.requireNonNull(flightA, "Flight A state is required.");
        this.flightB = Objects.requireNonNull(flightB, "Flight B state is required.");
        this.horizontalDistance = horizontalDistance;
        this.verticalDistance = verticalDistance;
    }

    public FlightViolationState flightA() {
        return flightA;
    }

    public FlightViolationState flightB() {
        return flightB;
    }

    public double horizontalDistance() {
        return horizontalDistance;
    }

    public double verticalDistance() {
        return verticalDistance;
    }
}
