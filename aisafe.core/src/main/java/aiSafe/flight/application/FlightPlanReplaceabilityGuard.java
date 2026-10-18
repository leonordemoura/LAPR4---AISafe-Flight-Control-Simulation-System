package aiSafe.flight.application;

import aiSafe.flight.domain.Flight;
import aiSafe.flight.domain.FlightPlan;
import aiSafe.flight.domain.FlightPlanStatus;

import java.util.Optional;

/**
 * Ensures a flight can accept a new DRAFT plan (US080/US081).
 */
public final class FlightPlanReplaceabilityGuard {

    private FlightPlanReplaceabilityGuard() {
    }

    public static void ensureReplaceable(final Flight flight) {
        currentPlan(flight).ifPresent(plan -> {
            if (plan.status() == FlightPlanStatus.VALIDATED) {
                throw new IllegalStateException(validatedPlanMessage(flight, plan));
            }
        });
    }

    public static Optional<String> replacementNotice(final Flight flight) {
        return currentPlan(flight)
                .filter(FlightPlan::isDraft)
                .map(plan -> "Current flight plan: DRAFT"
                        + routeSuffix(plan)
                        + ". A new registration will replace it.");
    }

    private static Optional<FlightPlan> currentPlan(final Flight flight) {
        return Optional.ofNullable(flight).flatMap(Flight::currentFlightPlanOptional);
    }

    private static String validatedPlanMessage(final Flight flight, final FlightPlan plan) {
        return "Flight "
                + flight.designator()
                + " already has a VALIDATED flight plan"
                + routeSuffix(plan)
                + ". Cannot register or import a new plan while validated. "
                + "Insert weather data to revert the plan to DRAFT first.";
    }

    private static String routeSuffix(final FlightPlan plan) {
        if (plan.routeSignature() == null || plan.routeSignature().isBlank()) {
            return "";
        }
        return " (" + plan.routeSignature() + ")";
    }
}
