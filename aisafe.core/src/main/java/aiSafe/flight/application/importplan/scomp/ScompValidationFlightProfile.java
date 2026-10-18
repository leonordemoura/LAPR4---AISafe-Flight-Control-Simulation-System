package aiSafe.flight.application.importplan.scomp;

import java.util.List;
import java.util.Objects;

/**
 * Climb/descend/cruise profile aligned with US085 validation simulation.
 */
public record ScompValidationFlightProfile(
        List<FlightProfileStep> climb,
        List<FlightProfileStep> descend,
        double cruiseSpeedKnots
) {

    public ScompValidationFlightProfile {
        climb = List.copyOf(Objects.requireNonNull(climb));
        descend = List.copyOf(Objects.requireNonNull(descend));
    }
}
