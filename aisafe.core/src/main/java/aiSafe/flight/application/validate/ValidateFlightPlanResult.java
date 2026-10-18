package aiSafe.flight.application.validate;

import aiSafe.flight.domain.Flight;
import aiSafe.flight.domain.FlightPlan;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ValidateFlightPlanResult {

    private final boolean success;
    private final Flight flight;
    private final FlightPlan flightPlan;
    private final ValidateFlightPlanSummary summary;
    private final List<ValidateFlightPlanError> errors;

    private ValidateFlightPlanResult(
            final boolean success,
            final Flight flight,
            final FlightPlan flightPlan,
            final ValidateFlightPlanSummary summary,
            final List<ValidateFlightPlanError> errors
    ) {
        this.success = success;
        this.flight = flight;
        this.flightPlan = flightPlan;
        this.summary = summary;
        this.errors = List.copyOf(errors);
    }

    public static ValidateFlightPlanResult success(
            final Flight flight,
            final FlightPlan plan,
            final ValidateFlightPlanSummary summary
    ) {
        return new ValidateFlightPlanResult(true, flight, plan, summary, List.of());
    }

    public static ValidateFlightPlanResult failure(final List<ValidateFlightPlanError> errors) {
        return new ValidateFlightPlanResult(false, null, null, null, errors);
    }

    public Optional<ValidateFlightPlanSummary> summary() {
        return Optional.ofNullable(summary);
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

    public List<ValidateFlightPlanError> errors() {
        return Collections.unmodifiableList(errors);
    }

    public List<String> formattedErrors() {
        return errors.stream().map(ValidateFlightPlanError::format).toList();
    }
}
