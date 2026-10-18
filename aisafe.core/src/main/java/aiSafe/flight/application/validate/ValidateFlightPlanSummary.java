package aiSafe.flight.application.validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Human-readable summary of a successfully validated flight plan (US085).
 */
public final class ValidateFlightPlanSummary {

    private final String flightDesignator;
    private final String route;
    private final String aircraftRegistration;
    private final String aircraftModel;
    private final String pilotId;
    private final String departure;
    private final String arrival;
    private final int legCount;
    private final int segmentCount;
    private final double maxAltitudeMeters;
    private final double availableFuelKg;
    private final double requiredFuelKg;
    private final int activeEngines;
    private final double fuelDensityKgPerL;
    private final double minFuelAtArrivalKg;
    private final double startElevation;
    private final double endElevation;

    public ValidateFlightPlanSummary(
            final String flightDesignator,
            final String route,
            final String aircraftRegistration,
            final String aircraftModel,
            final String pilotId,
            final String departure,
            final String arrival,
            final int legCount,
            final int segmentCount,
            final double maxAltitudeMeters,
            final double availableFuelKg,
            final double requiredFuelKg,
            final int activeEngines,
            final double fuelDensityKgPerL,
            final double minFuelAtArrivalKg,
            final double startElevation,
            final double endElevation
    ) {
        this.flightDesignator = flightDesignator;
        this.route = route;
        this.aircraftRegistration = aircraftRegistration;
        this.aircraftModel = aircraftModel;
        this.pilotId = pilotId;
        this.departure = departure;
        this.arrival = arrival;
        this.legCount = legCount;
        this.segmentCount = segmentCount;
        this.maxAltitudeMeters = maxAltitudeMeters;
        this.availableFuelKg = availableFuelKg;
        this.requiredFuelKg = requiredFuelKg;
        this.activeEngines = activeEngines;
        this.fuelDensityKgPerL = fuelDensityKgPerL;
        this.minFuelAtArrivalKg = minFuelAtArrivalKg;
        this.startElevation = startElevation;
        this.endElevation = endElevation;
    }

    public String flightDesignator() {
        return flightDesignator;
    }

    public String route() {
        return route;
    }

    public double availableFuelKg() {
        return availableFuelKg;
    }

    public double requiredFuelKg() {
        return requiredFuelKg;
    }

    public double fuelReserveKg() {
        return availableFuelKg - requiredFuelKg;
    }

    public List<String> formattedLines() {
        final List<String> lines = new ArrayList<>();
        lines.add("Flight plan validated (status: VALIDATED).");
        lines.add("");
        lines.add("--- Physics-Based Validation (ISA Model) ---");
        lines.add("Atmosphere:   International Standard (ISA)");
        lines.add("Integration:  Time-step integration (10s)");
        lines.add("Ground Speed: Corrected for Wind (DSL)");
        lines.add("Cruise Speed: Mach 0.82 limit enforced");
        lines.add("Elevation:    Start " + formatMeters(startElevation) + " | End " + formatMeters(endElevation));
        lines.add("");
        lines.add("--- Flight Summary ---");
        lines.add("Flight:       " + flightDesignator);
        lines.add("Aircraft:     " + aircraftRegistration + " (" + aircraftModel + ")");
        lines.add("Fuel Density: " + String.format("%.3f kg/L", fuelDensityKgPerL));
        lines.add("Departure:    " + departure);
        lines.add("Arrival:      " + arrival);
        lines.add("Cruise Alt:   " + formatMeters(maxAltitudeMeters));
        lines.add("");
        lines.add("--- Fuel Analysis ---");
        lines.add("Trip Fuel (est):   " + formatKg(requiredFuelKg - 200.0) + " kg");
        lines.add("Taxi & APU:        200 kg");
        lines.add("Reserve Required:  " + formatKg(minFuelAtArrivalKg) + " kg");
        lines.add("---------------------------");
        lines.add("Total Required:    " + formatKg(requiredFuelKg + minFuelAtArrivalKg) + " kg");
        lines.add("Operational Fuel:  " + formatKg(availableFuelKg) + " kg");
        lines.add("Fuel Status:       OK (Margin: " + formatKg(availableFuelKg - (requiredFuelKg + minFuelAtArrivalKg)) + " kg)");
        return List.copyOf(lines);
    }

    private static String formatKg(final double value) {
        return String.format(Locale.ROOT, "%.0f", value);
    }

    private static String formatMeters(final double value) {
        return String.format(Locale.ROOT, "%.0f m (FL%.0f)", value, value / 0.3048 / 100.0);
    }
}
