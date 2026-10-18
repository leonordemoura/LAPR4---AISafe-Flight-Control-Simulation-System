package aiSafe.flight.application.importplan.scomp;

import aiSafe.flight.domain.Flight;
import aiSafe.flight.domain.FlightDesignator;
import aiSafe.flight.domain.FlightType;
import aiSafe.flight.dsl.model.DslCoordinates;
import aiSafe.flight.dsl.model.DslFlightPlan;
import aiSafe.flight.dsl.model.DslLeg;
import aiSafe.flight.dsl.model.DslSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Maps validated flight plans to the JSON format consumed by SCOMP ({@code Flight_Plan_v0c.json}).
 * Flight profiles must match US085 validation ({@link ScompValidationFlightProfile}).
 */
public final class ScompFlightPlanJsonGenerator {

    private static final double GROUND_ALTITUDE_METERS = 69.0;

    public String generate(final ScompFlightExportSource source) {
        Objects.requireNonNull(source);
        return generate(List.of(source));
    }

    public String generate(final List<ScompFlightExportSource> sources) {
        Objects.requireNonNull(sources);
        if (sources.isEmpty()) {
            throw new IllegalArgumentException("At least one flight is required to generate SCOMP JSON.");
        }

        final StringBuilder json = new StringBuilder(4096);
        json.append("{\n    \"Flights\": [\n");
        for (int i = 0; i < sources.size(); i++) {
            if (i > 0) {
                json.append(",\n");
            }
            appendFlight(json, sources.get(i));
        }
        json.append("\n    ]\n}");
        return json.toString();
    }

    private void appendFlight(final StringBuilder json, final ScompFlightExportSource source) {
        final Flight flight = source.flight();
        final DslFlightPlan dslPlan = source.dslPlan();
        final FlightDesignator designator = flight.designator();

        json.append("        {\n");
        json.append("            \"ID\": ").append(designator.flightNumber()).append(",\n");
        json.append("            \"Type\": \"").append(toScompType(dslPlan.flightType())).append("\",\n");
        json.append("            \"Route\": \"").append(escapeJson(routeLabel(flight, dslPlan))).append("\",\n");
        json.append("            \"DepartureDate\": \"").append(escapeJson(departureDate(dslPlan))).append("\",\n");
        json.append("            \"DepartureTime\": \"").append(escapeJson(departureTime(dslPlan))).append("\",\n");
        json.append("\n            \"Leg\": [\n");

        final List<DslLeg> legs = dslPlan.legs();
        for (int i = 0; i < legs.size(); i++) {
            if (i > 0) {
                json.append(",\n");
            }
            appendLeg(json, legs.get(i), source.flightProfile());
        }

        json.append("\n            ]\n        }");
    }

    private void appendLeg(
            final StringBuilder json,
            final DslLeg leg,
            final ScompValidationFlightProfile flightProfile
    ) {
        json.append("                {\n");
        json.append("                    \"Departure\": \"").append(escapeJson(leg.departure().airportCode())).append("\",\n");
        json.append("                    \"Arrival\": \"").append(escapeJson(leg.arrival().airportCode())).append("\",\n");
        json.append("\n                    \"Fuel\": {\n");
        json.append("                        \"Quantity\": ").append(formatNumber(leg.fuel().amount())).append(",\n");
        json.append("                        \"Unit\": \"").append(escapeJson(leg.fuel().unit())).append("\"\n");
        json.append("                    },\n");
        json.append("\n                    \"FlightProfile\": {\n");
        appendFlightProfile(json, flightProfile);
        json.append("                    },\n");
        json.append("\n                    \"Segments\": [\n");
        appendSegments(json, leg);
        json.append("\n                    ]\n");
        json.append("                }");
    }

    private void appendFlightProfile(final StringBuilder json, final ScompValidationFlightProfile flightProfile) {
        json.append("\n                        \"Climb\": [\n");
        appendProfileSteps(json, flightProfile.climb(), true);
        json.append("\n                        ],\n");
        json.append("\n                        \"Descend\": [\n");
        appendProfileSteps(json, flightProfile.descend(), false);
        json.append("\n                        ],\n");
        json.append("\n                        \"Cruise\": {\n");
        json.append("                            \"Speed\": {\n");
        json.append("                                \"Value\": ").append(formatNumber(flightProfile.cruiseSpeedKnots())).append(",\n");
        json.append("                                \"Unit\": \"Knots\"\n");
        json.append("                            }\n");
        json.append("                        }\n");
    }

    private void appendProfileSteps(
            final StringBuilder json,
            final List<FlightProfileStep> steps,
            final boolean climb
    ) {
        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) {
                json.append(",\n");
            }
            final FlightProfileStep step = steps.get(i);
            json.append("                            {\n");
            json.append("                                \"Altitude\": { \"Value\": ")
                    .append(formatNumber(step.altitudeMeters()))
                    .append(", \"Unit\": \"m\" },\n");
            json.append("                                \"Speed\": { \"Value\": ")
                    .append(formatNumber(step.speedKnots()))
                    .append(", \"Unit\": \"Knots\" },\n");
            if (climb) {
                json.append("                                \"RateClimb\": { \"Value\": ")
                        .append(formatNumber(step.rateMetersPerSecond()))
                        .append(", \"Unit\": \"m/s\" }\n");
            } else {
                json.append("                                \"RateDescent\": { \"Value\": ")
                        .append(formatNumber(step.rateMetersPerSecond()))
                        .append(", \"Unit\": \"m/s\" }\n");
            }
            json.append("                            }");
        }
    }

    private void appendSegments(final StringBuilder json, final DslLeg leg) {
        final List<ScompSegment> segments = buildScompSegments(leg);
        for (int i = 0; i < segments.size(); i++) {
            if (i > 0) {
                json.append(",\n");
            }
            final ScompSegment segment = segments.get(i);
            json.append("                        {\n");
            json.append("                            \"Mode\": \"").append(segment.mode()).append("\",\n");
            json.append("\n                            \"Start\": {\n");
            appendPoint(json, segment.startLat(), segment.startLon(), segment.startAlt());
            json.append("\n                            },\n");
            json.append("\n                            \"End\": {\n");
            appendPoint(json, segment.endLat(), segment.endLon(), segment.endAlt());
            json.append("\n                            }\n");
            json.append("                        }");
        }
    }

    private void appendPoint(
            final StringBuilder json,
            final double lat,
            final double lon,
            final double altitudeMeters
    ) {
        json.append("                                \"Latitude\": ").append(formatNumber(lat)).append(",\n");
        json.append("                                \"Longitude\": ").append(formatNumber(lon)).append(",\n");
        json.append("\n                                \"Altitude\": {\n");
        json.append("                                    \"Quantity\": ").append(formatNumber(altitudeMeters)).append(",\n");
        json.append("                                    \"Unit\": \"m\"\n");
        json.append("                                }");
    }

    private List<ScompSegment> buildScompSegments(final DslLeg leg) {
        if (leg.segments().isEmpty()) {
            return List.of();
        }
        if (leg.segments().size() == 1) {
            return expandSingleDslSegment(leg.segments().getFirst());
        }
        final List<ScompSegment> result = new ArrayList<>();
        for (final DslSegment segment : leg.segments()) {
            result.addAll(expandSingleDslSegment(segment));
        }
        return result;
    }

    private List<ScompSegment> expandSingleDslSegment(final DslSegment segment) {
        final DslCoordinates start = segment.start();
        final DslCoordinates end = segment.end();
        final double cruiseAlt = segment.altitudeMeters();
        return List.of(
                new ScompSegment(
                        "climb",
                        start.latitude(), start.longitude(), GROUND_ALTITUDE_METERS,
                        start.latitude(), start.longitude(), cruiseAlt),
                new ScompSegment(
                        "cruise",
                        start.latitude(), start.longitude(), cruiseAlt,
                        end.latitude(), end.longitude(), cruiseAlt),
                new ScompSegment(
                        "descend",
                        end.latitude(), end.longitude(), cruiseAlt,
                        end.latitude(), end.longitude(), GROUND_ALTITUDE_METERS)
        );
    }

    private static String departureDate(final DslFlightPlan dslPlan) {
        if (dslPlan.legs().isEmpty()) {
            return "";
        }
        return dslPlan.legs().getFirst().departure().date();
    }

    private static String departureTime(final DslFlightPlan dslPlan) {
        if (dslPlan.legs().isEmpty()) {
            return "00:00";
        }
        return dslPlan.legs().getFirst().departure().time();
    }

    private static String routeLabel(final Flight flight, final DslFlightPlan dslPlan) {
        if (!dslPlan.legs().isEmpty()) {
            final DslLeg first = dslPlan.legs().getFirst();
            return first.departure().airportCode() + "-" + dslPlan.legs().getLast().arrival().airportCode();
        }
        return flight.route().routeDesignator().toString();
    }

    private static String toScompType(final FlightType type) {
        return type.name().toLowerCase(Locale.ROOT);
    }

    private static String formatNumber(final double value) {
        if (value == (long) value) {
            return Long.toString((long) value);
        }
        return String.format(Locale.US, "%.6f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static String escapeJson(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record ScompSegment(
            String mode,
            double startLat,
            double startLon,
            double startAlt,
            double endLat,
            double endLon,
            double endAlt
    ) {
    }
}
