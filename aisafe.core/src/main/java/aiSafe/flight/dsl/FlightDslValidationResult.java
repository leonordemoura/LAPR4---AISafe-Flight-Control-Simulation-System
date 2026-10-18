package aiSafe.flight.dsl;

import aiSafe.flight.dsl.model.DslFlightPlan;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class FlightDslValidationResult {

    private final boolean valid;
    private final DslFlightPlan model;
    private final List<FlightDslError> errors;

    private FlightDslValidationResult(
            final boolean valid,
            final DslFlightPlan model,
            final List<FlightDslError> errors) {
        this.valid = valid;
        this.model = model;
        this.errors = List.copyOf(errors);
    }

    public static FlightDslValidationResult success(final DslFlightPlan model) {
        return new FlightDslValidationResult(true, Objects.requireNonNull(model), List.of());
    }

    public static FlightDslValidationResult failure(final List<FlightDslError> errors) {
        return new FlightDslValidationResult(false, null, errors);
    }

    public boolean isValid() {
        return valid;
    }

    public Optional<DslFlightPlan> model() {
        return Optional.ofNullable(model);
    }

    public List<FlightDslError> errors() {
        return Collections.unmodifiableList(errors);
    }

    /** Human-readable error lines for UI or logs (US081). */
    public List<String> formattedErrors() {
        return errors.stream().map(FlightDslError::format).toList();
    }
}
