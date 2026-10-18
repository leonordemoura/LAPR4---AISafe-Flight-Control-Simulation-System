package aiSafe.flight.application.importplan.scomp;

import aiSafe.aircraft.domain.Aircraft;
import aiSafe.aircraft.repositories.AircraftRepository;
import aiSafe.flight.application.validate.simulator.AircraftSimulationParametersResolver;
import aiSafe.flight.domain.Flight;
import aiSafe.flight.domain.FlightDesignator;
import aiSafe.flight.domain.FlightPlan;
import aiSafe.flight.dsl.FlightDslValidationResult;
import aiSafe.flight.dsl.FlightDslValidator;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.infrastructure.persistence.RepositoryFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Lists VALIDATED flight plans and exports selected ones to SCOMP-ready JSON.
 */
public final class ExportFlightPlansToScompService {

    private final FlightRepository flightRepository;
    private final AircraftRepository aircraftRepository;
    private final ScompValidationFlightProfileResolver profileResolver;
    private final ScompFlightPlanJsonGenerator jsonGenerator;
    private final ScompFlightPlanJsonWriter jsonWriter;

    public ExportFlightPlansToScompService(final RepositoryFactory repositories) {
        this(
                repositories.flights(),
                repositories.aircrafts(),
                new ScompValidationFlightProfileResolver(
                        new AircraftSimulationParametersResolver(
                                repositories.getEngineModelRepository(),
                                repositories.airports())),
                new ScompFlightPlanJsonGenerator(),
                new ScompFlightPlanJsonWriter());
    }

    ExportFlightPlansToScompService(
            final FlightRepository flightRepository,
            final AircraftRepository aircraftRepository,
            final ScompValidationFlightProfileResolver profileResolver,
            final ScompFlightPlanJsonGenerator jsonGenerator,
            final ScompFlightPlanJsonWriter jsonWriter
    ) {
        this.flightRepository = Objects.requireNonNull(flightRepository);
        this.aircraftRepository = Objects.requireNonNull(aircraftRepository);
        this.profileResolver = Objects.requireNonNull(profileResolver);
        this.jsonGenerator = Objects.requireNonNull(jsonGenerator);
        this.jsonWriter = Objects.requireNonNull(jsonWriter);
    }

    public List<Flight> validatedFlights() {
        return StreamSupport.stream(flightRepository.findAll().spliterator(), false)
                .filter(f -> {
                    final FlightPlan plan = f.flightPlan();
                    return plan != null && plan.isValidated();
                })
                .toList();
    }

    public ExportFlightPlansToScompResult export(final List<FlightDesignator> designators) {
        Objects.requireNonNull(designators);
        if (designators.isEmpty()) {
            return ExportFlightPlansToScompResult.failure(
                    List.of("Select at least one validated flight plan to export."));
        }

        final LinkedHashSet<FlightDesignator> uniqueDesignators = new LinkedHashSet<>(designators);
        final List<String> errors = new ArrayList<>();
        final List<ScompFlightExportSource> sources = new ArrayList<>();

        for (final FlightDesignator designator : uniqueDesignators) {
            final Optional<Flight> flightOptional = flightRepository.findByDesignator(designator);
            if (flightOptional.isEmpty()) {
                errors.add("Flight not found: " + designator);
                continue;
            }

            final Flight flight = flightOptional.get();
            final FlightPlan plan = flight.flightPlan();
            if (plan == null || !plan.isValidated()) {
                errors.add("Flight " + designator + " has no validated flight plan.");
                continue;
            }

            final FlightDslValidationResult validation =
                    FlightDslValidator.validate(plan.dslSource().content());
            if (!validation.isValid()) {
                errors.add("Unable to parse DSL for flight " + designator + ": "
                        + validation.formattedErrors());
                continue;
            }

            final Optional<Aircraft> aircraftOptional =
                    aircraftRepository.findByRegistration(flight.aircraftRegistration().value());
            if (aircraftOptional.isEmpty()) {
                errors.add("Aircraft not found: " + flight.aircraftRegistration().value());
                continue;
            }

            final Optional<ScompValidationFlightProfile> profileOptional =
                    profileResolver.resolve(flight, aircraftOptional.get());
            if (profileOptional.isEmpty()) {
                final String message = profileResolver.failureMessage(aircraftOptional.get())
                        .orElse("Unable to build validation flight profile for aircraft "
                                + flight.aircraftRegistration().value() + ".");
                errors.add(message);
                continue;
            }

            sources.add(new ScompFlightExportSource(
                    flight,
                    validation.model().orElseThrow(),
                    profileOptional.get()));
        }

        if (!errors.isEmpty()) {
            return ExportFlightPlansToScompResult.failure(errors);
        }

        try {
            final String json = jsonGenerator.generate(sources);
            final Path outputPath = sources.size() == 1
                    ? jsonWriter.write(sources.getFirst().flight().designator(), json)
                    : jsonWriter.writeMultiFlightExport(json);
            final List<String> exported = sources.stream()
                    .map(source -> source.flight().designator().toString())
                    .toList();
            return ExportFlightPlansToScompResult.success(outputPath, exported);
        } catch (final IOException ex) {
            return ExportFlightPlansToScompResult.failure(
                    List.of("Failed to write SCOMP JSON file: " + ex.getMessage()));
        }
    }
}
