package aiSafe.flight.application.importplan;

import aiSafe.flight.domain.Flight;
import aiSafe.flight.domain.FlightPlan;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ImportFlightPlanResult {

    private final boolean success;
    private final Flight flight;
    private final FlightPlan flightPlan;
    private final Path scompJsonPath;
    private final List<ImportFlightPlanError> errors;

    private ImportFlightPlanResult(
            final boolean success,
            final Flight flight,
            final FlightPlan flightPlan,
            final Path scompJsonPath,
            final List<ImportFlightPlanError> errors) {
        this.success = success;
        this.flight = flight;
        this.flightPlan = flightPlan;
        this.scompJsonPath = scompJsonPath;
        this.errors = List.copyOf(errors);
    }

    public static ImportFlightPlanResult success(final Flight flight, final FlightPlan plan) {
        return new ImportFlightPlanResult(true, flight, plan, null, List.of());
    }

    public static ImportFlightPlanResult failure(final List<ImportFlightPlanError> errors) {
        return new ImportFlightPlanResult(false, null, null, null, errors);
    }

    public boolean isSuccess() {
        return success;
    }

    public Optional<Flight> flight() {
        return Optional.ofNullable(flight);
    }

    public Optional<FlightPlan> flightPlan() {
        return Optional.ofNullable(flightPlan);
    }

    public Optional<Path> scompJsonPath() {
        return Optional.ofNullable(scompJsonPath);
    }

    public List<ImportFlightPlanError> errors() {
        return Collections.unmodifiableList(errors);
    }

    public List<String> formattedErrors() {
        return errors.stream().map(ImportFlightPlanError::format).toList();
    }
}
