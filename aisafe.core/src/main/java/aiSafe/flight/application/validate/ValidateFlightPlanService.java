package aiSafe.flight.application.validate;

import aiSafe.aircraft.domain.Aircraft;
import aiSafe.aircraft.repositories.AircraftRepository;
import aiSafe.flight.application.validate.simulator.AircraftSimulationParametersResolver;
import aiSafe.flight.application.validate.simulator.FlightPlanSimulationResult;
import aiSafe.flight.application.validate.simulator.FlightPlanSimulator;
import aiSafe.flight.application.validate.simulator.SimulationWeatherConditions;
import aiSafe.flight.application.validate.simulator.SimulatorModelParameters;
import aiSafe.flight.domain.Flight;
import aiSafe.flight.domain.FlightDesignator;
import aiSafe.flight.domain.FlightPlan;
import aiSafe.flight.dsl.FlightDslError;
import aiSafe.flight.dsl.FlightDslValidationResult;
import aiSafe.flight.dsl.FlightDslValidator;
import aiSafe.flight.dsl.model.DslFlightPlan;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.infrastructure.persistence.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * US085 – full DSL validation of the current DRAFT flight plan; promotes it to VALIDATED.
 */
public final class ValidateFlightPlanService {

    private final RepositoryFactory repositories;
    private final FlightPlanSimulator simulator;
    private final AircraftSimulationParametersResolver parametersResolver;

    public ValidateFlightPlanService(final RepositoryFactory repositories) {
        this(
                repositories,
                new FlightPlanSimulator(),
                new AircraftSimulationParametersResolver(repositories.getEngineModelRepository(), repositories.airports()));
    }

    ValidateFlightPlanService(
            final RepositoryFactory repositories,
            final FlightPlanSimulator simulator,
            final AircraftSimulationParametersResolver parametersResolver
    ) {
        this.repositories = Objects.requireNonNull(repositories);
        this.simulator = Objects.requireNonNull(simulator);
        this.parametersResolver = Objects.requireNonNull(parametersResolver);
    }

    public ValidateFlightPlanResult validate(final FlightDesignator flightId) {
        Objects.requireNonNull(flightId);

        final FlightRepository flightRepository = repositories.flights();
        final Optional<Flight> flightOptional = flightRepository.findByDesignator(flightId);
        if (flightOptional.isEmpty()) {
            return ValidateFlightPlanResult.failure(List.of(
                    new ValidateFlightPlanError(
                            ValidateFlightPlanErrorCode.FLIGHT_NOT_FOUND,
                            "Flight not found: " + flightId)));
        }

        final Flight flight = flightOptional.get();
        final FlightPlan plan = flight.flightPlan();
        if (plan == null) {
            return ValidateFlightPlanResult.failure(List.of(
                    new ValidateFlightPlanError(
                            ValidateFlightPlanErrorCode.NO_DRAFT_PLAN,
                            "Flight has no flight plan to validate.")));
        }

        if (plan.isValidated()) {
            return ValidateFlightPlanResult.failure(List.of(
                    new ValidateFlightPlanError(
                            ValidateFlightPlanErrorCode.PLAN_ALREADY_VALIDATED,
                            "Flight plan is already validated.")));
        }

        if (!plan.isDraft()) {
            return ValidateFlightPlanResult.failure(List.of(
                    new ValidateFlightPlanError(
                            ValidateFlightPlanErrorCode.NO_DRAFT_PLAN,
                            "Only draft flight plans can be validated.")));
        }

        final FlightDslValidationResult validation =
                FlightDslValidator.validate(plan.dslSource().content());
        if (!validation.isValid()) {
            return ValidateFlightPlanResult.failure(mapDslErrors(validation));
        }

        final DslFlightPlan dslPlan = validation.model().orElseThrow();

        if (!dslPlan.identifier().equals(flightId.toString())) {
            return ValidateFlightPlanResult.failure(List.of(
                    new ValidateFlightPlanError(
                            ValidateFlightPlanErrorCode.FLIGHT_ID_MISMATCH,
                            "DSL flight identifier ("
                                    + dslPlan.identifier()
                                    + ") does not match selected flight ("
                                    + flightId
                                    + ").")));
        }

        final String dslRoute = dslPlan.legs().getFirst().departure().airportCode()
                + "-"
                + dslPlan.legs().getLast().arrival().airportCode();
        final String flightRoute = flight.route().routeDesignator().toString();
        if (!flightRoute.equals(dslRoute)) {
            return ValidateFlightPlanResult.failure(List.of(
                    new ValidateFlightPlanError(
                            ValidateFlightPlanErrorCode.ROUTE_MISMATCH,
                            "Flight plan route ("
                                    + dslRoute
                                    + ") does not match flight route ("
                                    + flightRoute
                                    + ").")));
        }

        final Optional<String> departureMismatch =
                FlightPlanDepartureConsistencyChecker.mismatchMessage(plan.departureDateTime(), dslPlan);
        if (departureMismatch.isPresent()) {
            return ValidateFlightPlanResult.failure(List.of(
                    new ValidateFlightPlanError(
                            ValidateFlightPlanErrorCode.DEPARTURE_MISMATCH,
                            departureMismatch.get())));
        }

        final AircraftRepository aircraftRepository = repositories.aircrafts();
        final Optional<Aircraft> aircraftOptional =
                aircraftRepository.findByRegistration(flight.aircraftRegistration().value());
        if (aircraftOptional.isEmpty()) {
            return ValidateFlightPlanResult.failure(List.of(
                    new ValidateFlightPlanError(
                            ValidateFlightPlanErrorCode.AIRCRAFT_NOT_FOUND,
                            "Aircraft not found: " + flight.aircraftRegistration().value())));
        }

        final Aircraft aircraft = aircraftOptional.get();
        final Optional<SimulatorModelParameters> simulationParameters = parametersResolver.resolve(flight, aircraft);
        if (simulationParameters.isEmpty()) {
            final String message = parametersResolver.resolveFailureMessage(aircraft)
                    .orElse("Unable to build simulation parameters for the assigned aircraft.");
            return ValidateFlightPlanResult.failure(List.of(
                    new ValidateFlightPlanError(
                            ValidateFlightPlanErrorCode.SIMULATION_FAILED,
                            message)));
        }

        final Optional<SimulationWeatherConditions> weather = plan.hasWeatherData()
                ? Optional.of(SimulationWeatherConditions.from(plan.weatherData()))
                : Optional.empty();

        final FlightPlanSimulationResult simulation = simulator.simulate(
                dslPlan,
                plan.fuelQuantity(),
                simulationParameters.get(),
                weather);
        if (!simulation.isSuccess()) {
            final ValidateFlightPlanErrorCode code = simulation.requiredFuelKg() > simulation.availableFuelKg()
                    ? ValidateFlightPlanErrorCode.INSUFFICIENT_FUEL
                    : ValidateFlightPlanErrorCode.SIMULATION_FAILED;
            return ValidateFlightPlanResult.failure(List.of(
                    new ValidateFlightPlanError(
                            code,
                            simulation.message().orElse("Flight plan simulation failed."))));
        }

        try {
            plan.markAsValidated();
        } catch (final IllegalStateException ex) {
            return ValidateFlightPlanResult.failure(List.of(
                    new ValidateFlightPlanError(
                            ValidateFlightPlanErrorCode.NO_DRAFT_PLAN,
                            ex.getMessage())));
        }

        final Flight saved = flightRepository.save(flight);
        final ValidateFlightPlanSummary summary = buildSummary(
                saved,
                saved.flightPlan(),
                dslPlan,
                aircraft,
                simulationParameters.get(),
                simulation);
        return ValidateFlightPlanResult.success(saved, saved.flightPlan(), summary);
    }

    private static ValidateFlightPlanSummary buildSummary(
            final Flight flight,
            final FlightPlan plan,
            final DslFlightPlan dslPlan,
            final Aircraft aircraft,
            final SimulatorModelParameters simulationParameters,
            final FlightPlanSimulationResult simulation
    ) {
        int segmentCount = 0;
        double maxAltitudeMeters = 0;
        for (final var leg : dslPlan.legs()) {
            segmentCount += leg.segments().size();
            for (final var segment : leg.segments()) {
                maxAltitudeMeters = Math.max(maxAltitudeMeters, segment.altitudeMeters());
            }
        }

        return new ValidateFlightPlanSummary(
                flight.designator().toString(),
                plan.routeSignature(),
                plan.aircraftRegistration().value(),
                aircraft.aircraftModel().name().value(),
                plan.pilotId().value(),
                plan.departureDateTime().value().toString(),
                plan.arrivalDateTime().value().toString(),
                plan.legCount(),
                segmentCount,
                maxAltitudeMeters,
                simulation.availableFuelKg(),
                simulation.requiredFuelKg(),
                simulationParameters.activeEngineCount(),
                simulationParameters.fuelDensityKgPerL(),
                simulationParameters.minFuelAtArrivalKg(),
                simulationParameters.startElevationMeters(),
                simulationParameters.endElevationMeters());
    }

    private static List<ValidateFlightPlanError> mapDslErrors(final FlightDslValidationResult validation) {
        final List<ValidateFlightPlanError> errors = new ArrayList<>();
        for (final FlightDslError error : validation.errors()) {
            errors.add(new ValidateFlightPlanError(
                    ValidateFlightPlanErrorCode.DSL_VALIDATION,
                    error.message(),
                    error.line(),
                    error.column()));
        }
        return errors;
    }
}
