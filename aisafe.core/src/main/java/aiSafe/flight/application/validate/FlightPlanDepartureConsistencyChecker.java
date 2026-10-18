package aiSafe.flight.application.validate;

import aiSafe.flight.application.FlightPlanSchedule;
import aiSafe.flight.application.FlightPlanScheduleResolver;
import aiSafe.flight.domain.DepartureDateTime;
import aiSafe.flight.dsl.model.DslFlightPlan;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * US085 – ensures operational departure on the flight plan matches the first leg in the DSL.
 */
public final class FlightPlanDepartureConsistencyChecker {

    private FlightPlanDepartureConsistencyChecker() {
    }

    public static Optional<String> mismatchMessage(
            final DepartureDateTime planDeparture,
            final DslFlightPlan dslPlan
    ) {
        Objects.requireNonNull(planDeparture, "Plan departure is required.");
        Objects.requireNonNull(dslPlan, "DSL plan is required.");

        final FlightPlanSchedule schedule = FlightPlanScheduleResolver.fromDslFlightPlan(dslPlan);
        final LocalDateTime dslDeparture = schedule.departure();
        final LocalDateTime operationalDeparture = planDeparture.value();

        if (operationalDeparture.equals(dslDeparture)) {
            return Optional.empty();
        }

        return Optional.of(
                "Operational departure ("
                        + operationalDeparture
                        + ") does not match DSL first-leg departure ("
                        + dslDeparture
                        + ").");
    }
}
