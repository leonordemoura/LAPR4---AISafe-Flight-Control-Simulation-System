package aiSafe.flight.application;

import aiSafe.flight.dsl.model.DslFlightPlan;
import aiSafe.flight.dsl.model.DslLeg;
import aiSafe.flight.dsl.model.DslSchedulePoint;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Derives departure/arrival instants from DSL legs or text.
 */
public final class FlightPlanScheduleResolver {

    private static final Pattern ARRIVAL_LINE = Pattern.compile(
            "\\barrival\\s+[A-Z]{3,4}\\s+(\\d{4}-\\d{2}-\\d{2})\\s+(\\d{1,2}:\\d{2})\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern DEPARTURE_LINE = Pattern.compile(
            "\\bdeparture\\s+[A-Z]{3,4}\\s+(\\d{4}-\\d{2}-\\d{2})\\s+(\\d{1,2}:\\d{2})\\b",
            Pattern.CASE_INSENSITIVE);

    private FlightPlanScheduleResolver() {
    }

    public static FlightPlanSchedule fromDslFlightPlan(final DslFlightPlan dslPlan) {
        if (dslPlan.legs().isEmpty()) {
            throw new IllegalArgumentException("Flight plan must contain at least one leg.");
        }
        final DslLeg first = dslPlan.legs().getFirst();
        final DslLeg last = dslPlan.legs().getLast();
        return new FlightPlanSchedule(
                toLocalDateTime(first.departure()),
                toLocalDateTime(last.arrival()));
    }

    public static FlightPlanSchedule fromRegisterDto(
            final RegisterFlightPlanDTO dto,
            final String resolvedDslContent
    ) {
        if (!dto.legs().isEmpty()) {
            final RegisterFlightPlanLegInput first = dto.legs().getFirst();
            final RegisterFlightPlanLegInput last = dto.legs().getLast();
            return new FlightPlanSchedule(
                    parseSchedulePoint(first.departureDate, first.departureTime),
                    parseSchedulePoint(last.arrivalDate, last.arrivalTime));
        }
        if (dto.departureDateTime != null && dto.arrivalDateTime != null) {
            return new FlightPlanSchedule(dto.departureDateTime, dto.arrivalDateTime);
        }
        if (resolvedDslContent != null && !resolvedDslContent.isBlank()) {
            return fromDslContent(normalizeDsl(resolvedDslContent));
        }
        throw new IllegalArgumentException("Cannot determine flight plan schedule.");
    }

    public static FlightPlanSchedule fromDslContent(final String dslContent) {
        if (dslContent == null || dslContent.isBlank()) {
            throw new IllegalArgumentException("DSL content is required.");
        }
        final String dsl = normalizeDsl(dslContent);
        return new FlightPlanSchedule(
                firstDepartureFromDsl(dsl),
                lastArrivalFromDsl(dsl));
    }

    private static String normalizeDsl(final String dsl) {
        return dsl.replace("\uFEFF", "")
                .replace("\r\n", "\n")
                .replace('\r', '\n');
    }

    private static LocalDateTime toLocalDateTime(final DslSchedulePoint point) {
        return LocalDateTime.parse(point.date() + "T" + point.time());
    }

    private static LocalDateTime parseSchedulePoint(final String date, final String time) {
        return LocalDateTime.parse(date + "T" + time);
    }

    private static LocalDateTime firstDepartureFromDsl(final String dsl) {
        final Matcher matcher = DEPARTURE_LINE.matcher(dsl);
        if (!matcher.find()) {
            throw new IllegalArgumentException("DSL must declare at least one departure schedule.");
        }
        return LocalDateTime.parse(matcher.group(1) + "T" + matcher.group(2));
    }

    private static LocalDateTime lastArrivalFromDsl(final String dsl) {
        final Matcher matcher = ARRIVAL_LINE.matcher(dsl);
        LocalDateTime last = null;
        while (matcher.find()) {
            last = LocalDateTime.parse(matcher.group(1) + "T" + matcher.group(2));
        }
        if (last == null) {
            throw new IllegalArgumentException("DSL must declare at least one arrival schedule.");
        }
        return last;
    }
}
