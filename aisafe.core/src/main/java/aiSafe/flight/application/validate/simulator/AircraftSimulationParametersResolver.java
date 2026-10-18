package aiSafe.flight.application.validate.simulator;

import aiSafe.airport.domain.Airport;
import aiSafe.airport.domain.IataCode_Airport;
import aiSafe.airport.repositories.AirportRepository;
import aiSafe.aircraft.domain.Aircraft;
import aiSafe.aircraft.domain.CurrentEngine;
import aiSafe.aircraftmanagement.domain.EngineModel;
import aiSafe.aircraftmanagement.domain.EngineModelName;
import aiSafe.aircraftmanagement.domain.Maker;
import aiSafe.aircraftmanagement.repositories.EngineModelRepository;
import aiSafe.aircraftModel.domain.AircraftModel;
import aiSafe.aircraftModel.domain.PhysicalSpecs;
import aiSafe.aircraftModel.domain.Speed;
import aiSafe.aircraftModel.domain.SpeedUnit;
import aiSafe.aircraftModel.domain.Weight;
import aiSafe.flight.domain.Flight;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Builds per-flight simulation parameters from the aircraft assigned to the flight.
 * {@link AircraftModel#engines()} lists certified engine models; {@link Aircraft#engineModels()}
 * lists the engines currently active on that aircraft instance.
 */
public final class AircraftSimulationParametersResolver {

    private static final double DEFAULT_ASPECT_RATIO = 9.0;
    private static final double DEFAULT_OSWALD_EFFICIENCY = 0.84;
    private static final double KG_PER_PASSENGER = 100.0;
    /** Typical short-haul load factor for operational fuel estimates. */
    private static final double OPERATIONAL_LOAD_FACTOR = 0.70;
    private static final double THRUST_REFERENCE_SPEED_MPS = 250.0;

    private final EngineModelRepository engineModels;
    private final AirportRepository airports;

    public AircraftSimulationParametersResolver(
            final EngineModelRepository engineModels,
            final AirportRepository airports
    ) {
        this.engineModels = Objects.requireNonNull(engineModels);
        this.airports = Objects.requireNonNull(airports);
    }

    public Optional<SimulatorModelParameters> resolve(final Flight flight, final Aircraft aircraft) {
        Objects.requireNonNull(flight);
        Objects.requireNonNull(aircraft);

        if (aircraft.engineModels().isEmpty()) {
            return Optional.empty();
        }

        final AircraftModel model = aircraft.aircraftModel();
        final PhysicalSpecs specs = model.specs();
        final List<ActiveEngineParameters> activeEngines = new ArrayList<>();
        double fuelDensity = 0.804; // Default fallback

        for (final CurrentEngine currentEngine : aircraft.engineModels()) {
            final Optional<EngineModel> resolved = resolveEngineModel(currentEngine, model.maker());
            if (resolved.isEmpty()) {
                return Optional.empty();
            }
            final EngineModel engine = resolved.get();
            if (!isCertifiedEngine(engine, model)) {
                return Optional.empty();
            }
            activeEngines.add(new ActiveEngineParameters(
                    EngineThrustConverter.thrustSeaLevelNewtons(engine.power(), THRUST_REFERENCE_SPEED_MPS),
                    engine.tsfc().value()));

            // Use the density from the first engine found (they should be the same)
            if (engine.fuelType() != null && engine.fuelType().density() != null) {
                fuelDensity = engine.fuelType().density().density();
            }
        }

        final double minFuelAtArrivalKg = resolveMinFuelAtArrivalKg(flight);
        final double startElevation = resolveStartElevation(flight);
        final double endElevation = resolveEndElevation(flight);

        return Optional.of(new SimulatorModelParameters(
                specs.wingArea().value(),
                DEFAULT_ASPECT_RATIO,
                DEFAULT_OSWALD_EFFICIENCY,
                weightKg(specs.emptyWeight()),
                payloadKg(aircraft),
                specs.dragCoefficient().value(),
                cruiseSpeedKnots(specs.cruiseSpeed()),
                fuelDensity,
                minFuelAtArrivalKg,
                startElevation,
                endElevation,
                activeEngines));
    }

    private double resolveMinFuelAtArrivalKg(final Flight flight) {
        final String destIata = flight.route().routeDesignator().destination();
        final Optional<Airport> airport = airports.findByIataCode(new IataCode_Airport(destIata));
        return airport.map(a -> a.airControlArea().minFuelAtArrival().value()).orElse(0.0);
    }

    private double resolveStartElevation(final Flight flight) {
        final String originIata = flight.route().routeDesignator().origin();
        return airports.findByIataCode(new IataCode_Airport(originIata))
                .map(a -> a.elevation().value())
                .orElse(0.0);
    }

    private double resolveEndElevation(final Flight flight) {
        final String destIata = flight.route().routeDesignator().destination();
        return airports.findByIataCode(new IataCode_Airport(destIata))
                .map(a -> a.elevation().value())
                .orElse(0.0);
    }

    public Optional<String> resolveFailureMessage(final Aircraft aircraft) {
        Objects.requireNonNull(aircraft);

        if (aircraft.engineModels().isEmpty()) {
            return Optional.of("Aircraft has no active engines configured for simulation.");
        }

        final AircraftModel model = aircraft.aircraftModel();
        for (final CurrentEngine currentEngine : aircraft.engineModels()) {
            final Optional<EngineModel> resolved = resolveEngineModel(currentEngine, model.maker());
            if (resolved.isEmpty()) {
                return Optional.of("Active engine not found: " + currentEngine.value());
            }
            if (!isCertifiedEngine(resolved.get(), model)) {
                return Optional.of(
                        "Active engine "
                                + resolved.get().modelName().name()
                                + " is not certified for aircraft model "
                                + model.name().value()
                                + ".");
            }
        }
        return Optional.empty();
    }

    private Optional<EngineModel> resolveEngineModel(final CurrentEngine currentEngine, final Maker maker) {
        final String reference = currentEngine.value().trim();
        if (reference.isEmpty()) {
            return Optional.empty();
        }

        try {
            final Long engineId = Long.parseLong(reference);
            return engineModels.ofIdentity(engineId);
        } catch (final NumberFormatException ignored) {
            final Optional<EngineModel> byMaker = engineModels.findByModelNameAndMaker(
                    new EngineModelName(reference),
                    maker);
            if (byMaker.isPresent()) {
                return byMaker;
            }
            return findEngineByName(reference);
        }
    }

    private Optional<EngineModel> findEngineByName(final String reference) {
        for (final EngineModel engine : engineModels.findAll()) {
            if (engine.modelName().name().equalsIgnoreCase(reference)) {
                return Optional.of(engine);
            }
        }
        return Optional.empty();
    }

    private static boolean isCertifiedEngine(final EngineModel activeEngine, final AircraftModel model) {
        return model.engines().stream().anyMatch(certified -> {
            if (certified.modelName().equals(activeEngine.modelName())) {
                return true;
            }
            return certified.identity() != null
                    && activeEngine.identity() != null
                    && certified.identity().equals(activeEngine.identity());
        });
    }

    private static double weightKg(final Weight weight) {
        return weight.value();
    }

    private static double payloadKg(final Aircraft aircraft) {
        return aircraft.cabinConfiguration().totalSeats() * KG_PER_PASSENGER * OPERATIONAL_LOAD_FACTOR;
    }

    private static double cruiseSpeedKnots(final Speed speed) {
        if (speed.unit() == SpeedUnit.KNOTS) {
            return speed.value();
        }
        return speed.value() / 1.852;
    }
}
