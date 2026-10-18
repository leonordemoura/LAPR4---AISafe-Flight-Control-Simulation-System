package aiSafe.flight.application;

import java.util.List;
import java.util.Locale;

/**
 * Builds Core Flight DSL text from structured leg/segment data (US080 manual UI path).
 */
public final class FlightPlanDslGenerator {

    private FlightPlanDslGenerator() {}

    public static String generate(
            final String flightIdentifier,
            final String flightType,
            final List<RegisterFlightPlanLegInput> legs
    ) {
        if (flightIdentifier == null || flightIdentifier.isBlank()) {
            throw new IllegalArgumentException("Flight identifier is required for DSL generation.");
        }
        if (flightType == null || flightType.isBlank()) {
            throw new IllegalArgumentException("Flight type is required for DSL generation.");
        }
        if (legs == null || legs.isEmpty()) {
            throw new IllegalArgumentException("At least one leg is required for DSL generation.");
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("flight ").append(flightIdentifier.trim()).append(" {\n");
        sb.append("    type ").append(flightType.trim().toUpperCase(Locale.ROOT)).append("\n");

        for (final RegisterFlightPlanLegInput leg : legs) {
            appendLeg(sb, leg);
        }

        sb.append("}\n");
        return sb.toString();
    }

    private static void appendLeg(final StringBuilder sb, final RegisterFlightPlanLegInput leg) {
        sb.append("    leg {\n");
        sb.append("        departure ").append(leg.departureAirport).append(' ')
                .append(leg.departureDate).append(' ').append(leg.departureTime).append("\n");
        sb.append("        arrival ").append(leg.arrivalAirport).append(' ')
                .append(leg.arrivalDate).append(' ').append(leg.arrivalTime).append("\n");
        sb.append("        route { origin ").append(leg.routeOrigin)
                .append(" destination ").append(leg.routeDestination).append(" }\n");

        for (final RegisterFlightPlanSegmentInput segment : leg.segments()) {
            sb.append("        segment { start (")
                    .append(segment.startLatitude).append(", ").append(segment.startLongitude)
                    .append(") end (")
                    .append(segment.endLatitude).append(", ").append(segment.endLongitude)
                    .append(") altitude ").append(formatNumber(segment.altitudeMeters)).append(" m\n");
            sb.append("            width ").append(formatNumber(segment.widthKm)).append(" km\n");
            sb.append("            wind ")
                    .append(formatNumber(segment.windDirectionDeg))
                    .append(" deg ")
                    .append(formatNumber(segment.windSpeed))
                    .append(' ')
                    .append(segment.windUnit)
                    .append(" }\n");
        }

        sb.append("        fuel ").append(formatFuel(leg.legFuel)).append(' ')
                .append(normalizeFuelUnit(leg.legFuelUnit)).append("\n");
        sb.append("    }\n");
    }

    private static String formatNumber(final double value) {
        if (value == (long) value) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }

    private static String formatFuel(final double fuel) {
        if (fuel == (long) fuel) {
            return Long.toString((long) fuel);
        }
        return Double.toString(fuel);
    }

    private static String normalizeFuelUnit(final String unit) {
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Leg fuel unit is required.");
        }
        final String normalized = unit.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("kg") || normalized.equals("kilogram") || normalized.equals("kilograms")) {
            return "kg";
        }
        if (normalized.equals("l") || normalized.equals("liter") || normalized.equals("liters")) {
            return "l";
        }
        return normalized;
    }
}
