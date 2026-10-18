package aiSafe.flight.application.importplan.scomp;

import aiSafe.flight.domain.Flight;
import aiSafe.flight.dsl.model.DslFlightPlan;

import java.util.Objects;

/**
 * One flight, parsed DSL, and validation-aligned profile for SCOMP JSON generation.
 */
public record ScompFlightExportSource(
        Flight flight,
        DslFlightPlan dslPlan,
        ScompValidationFlightProfile flightProfile
) {

    public ScompFlightExportSource {
        Objects.requireNonNull(flight);
        Objects.requireNonNull(dslPlan);
        Objects.requireNonNull(flightProfile);
    }
}
