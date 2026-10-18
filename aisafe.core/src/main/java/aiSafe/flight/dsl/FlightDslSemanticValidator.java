package aiSafe.flight.dsl;

import aiSafe.flight.dsl.model.DslFlightPlan;
import aiSafe.flight.dsl.model.DslLeg;
import aiSafe.flight.dsl.model.DslSegment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class FlightDslSemanticValidator {

    private FlightDslSemanticValidator() {}

    static List<FlightDslError> validate(final DslFlightPlan plan) {
        final List<FlightDslError> errors = new ArrayList<>();
        validateLegSequence(plan, errors);
        validateAirportVisits(plan, errors);
        for (int i = 0; i < plan.legs().size(); i++) {
            validateLeg(plan.legs().get(i), i + 1, errors);
        }
        return errors;
    }

    private static void validateLegSequence(final DslFlightPlan plan, final List<FlightDslError> errors) {
        final List<DslLeg> legs = plan.legs();
        for (int i = 0; i < legs.size() - 1; i++) {
            final DslLeg current = legs.get(i);
            final DslLeg next = legs.get(i + 1);

            if (!current.arrival().airportCode().equals(next.departure().airportCode())) {
                errors.add(semantic(
                        "Arrival airport of leg " + (i + 1)
                                + " must match departure airport of leg " + (i + 2) + ".",
                        next.departure().line(),
                        1));
            }

            final LocalDateTime currentArrival = parseDateTime(current.arrival(), errors);
            final LocalDateTime nextDeparture = parseDateTime(next.departure(), errors);
            if (currentArrival != null && nextDeparture != null && !currentArrival.isBefore(nextDeparture)) {
                errors.add(semantic(
                        "Arrival time of leg " + (i + 1)
                                + " must precede departure time of leg " + (i + 2) + ".",
                        next.departure().line(),
                        1));
            }
        }
    }

    private static void validateAirportVisits(final DslFlightPlan plan, final List<FlightDslError> errors) {
        if (plan.legs().isEmpty()) {
            return;
        }

        final Set<String> visited = new HashSet<>();
        final List<DslLeg> legs = plan.legs();

        checkAirportNotRepeated(
                legs.getFirst().departure().airportCode(),
                legs.getFirst().departure().line(),
                visited,
                errors);

        for (final DslLeg leg : legs) {
            checkAirportNotRepeated(leg.arrival().airportCode(), leg.arrival().line(), visited, errors);
        }
    }

    private static void checkAirportNotRepeated(
            final String airport,
            final int line,
            final Set<String> visited,
            final List<FlightDslError> errors) {
        if (!visited.add(airport)) {
            errors.add(semantic(
                    "Airport '" + airport + "' cannot be visited twice in the same flight.",
                    line,
                    1));
        }
    }

    private static void validateLeg(final DslLeg leg, final int legIndex, final List<FlightDslError> errors) {
        final LocalDateTime departure = parseDateTime(leg.departure(), errors);
        final LocalDateTime arrival = parseDateTime(leg.arrival(), errors);
        if (departure != null && arrival != null && !departure.isBefore(arrival)) {
            errors.add(semantic(
                    "Arrival time must be after departure time on leg " + legIndex + ".",
                    leg.arrival().line(),
                    1));
        }

        if (!leg.route().origin().equals(leg.departure().airportCode())) {
            errors.add(semantic(
                    "Route origin must match departure airport of leg " + legIndex + ".",
                    leg.route().line(),
                    1));
        }
        if (!leg.route().destination().equals(leg.arrival().airportCode())) {
            errors.add(semantic(
                    "Route destination must match arrival airport of leg " + legIndex + ".",
                    leg.route().line(),
                    1));
        }

        if (leg.fuel().amount() <= 0) {
            errors.add(semantic("Fuel must be positive.", leg.fuel().line(), 1));
        }

        for (final DslSegment segment : leg.segments()) {
            if (segment.altitudeMeters() <= 0) {
                errors.add(semantic("Altitude must be positive.", segment.line(), 1));
            }
            if (segment.widthKm() <= 0) {
                errors.add(semantic("Width must be positive.", segment.line(), 1));
            }
            if (segment.windDirectionDeg() < 0 || segment.windDirectionDeg() >= 360) {
                errors.add(semantic("Wind direction must be between 0 and 359 degrees.", segment.line(), 1));
            }
            if (segment.windSpeed() < 0) {
                errors.add(semantic("Wind speed must be non-negative.", segment.line(), 1));
            }
            if (segment.start().samePosition(segment.end())) {
                errors.add(semantic("Segment start and end coordinates must differ.", segment.line(), 1));
            }
        }
    }

    private static LocalDateTime parseDateTime(
            final aiSafe.flight.dsl.model.DslSchedulePoint point,
            final List<FlightDslError> errors) {
        try {
            final LocalDate date = LocalDate.parse(point.date());
            final LocalTime time = LocalTime.parse(point.time());
            return LocalDateTime.of(date, time);
        } catch (final DateTimeParseException ex) {
            errors.add(semantic("Invalid date or time: " + point.date() + " " + point.time() + ".", point.line(), 1));
            return null;
        }
    }

    private static FlightDslError semantic(final String message, final int line, final int column) {
        return new FlightDslError(FlightDslErrorType.SEMANTIC, message, line, column);
    }
}
