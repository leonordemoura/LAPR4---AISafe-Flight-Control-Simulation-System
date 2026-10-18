package aiSafe.flight.application.importplan.scomp;

import aiSafe.aircraft.domain.Aircraft;
import aiSafe.flight.application.validate.simulator.AircraftSimulationParametersResolver;
import aiSafe.flight.application.validate.simulator.FlightValidationProfileCatalog;
import aiSafe.flight.application.validate.simulator.SimulatorModelParameters;

import aiSafe.flight.domain.Flight;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Builds SCOMP flight profiles from the same inputs as US085 fuel validation.
 */
public final class ScompValidationFlightProfileResolver {

    private final AircraftSimulationParametersResolver parametersResolver;

    public ScompValidationFlightProfileResolver(final AircraftSimulationParametersResolver parametersResolver) {
        this.parametersResolver = Objects.requireNonNull(parametersResolver);
    }

    public Optional<ScompValidationFlightProfile> resolve(final Flight flight, final Aircraft aircraft) {
        Objects.requireNonNull(flight);
        Objects.requireNonNull(aircraft);
        final Optional<SimulatorModelParameters> parameters = parametersResolver.resolve(flight, aircraft);
        if (parameters.isEmpty()) {
            return Optional.empty();
        }

        final FlightValidationProfileCatalog validationProfiles = FlightValidationProfileCatalog.defaults();
        return Optional.of(new ScompValidationFlightProfile(
                validationProfiles.climb(),
                toScompDescentJsonSteps(validationProfiles.descend()),
                parameters.get().cruiseSpeedKnots()));
    }

    public Optional<String> failureMessage(final Aircraft aircraft) {
        return parametersResolver.resolveFailureMessage(aircraft);
    }

    /**
     * US085 stores positive descent rates; SCOMP JSON uses negative {@code RateDescent} values.
     */
    static List<FlightProfileStep> toScompDescentJsonSteps(final List<FlightProfileStep> validationSteps) {
        return validationSteps.stream()
                .map(step -> new FlightProfileStep(
                        step.altitudeMeters(),
                        step.speedKnots(),
                        step.rateMetersPerSecond() > 0
                                ? -step.rateMetersPerSecond()
                                : step.rateMetersPerSecond()))
                .toList();
    }
}
