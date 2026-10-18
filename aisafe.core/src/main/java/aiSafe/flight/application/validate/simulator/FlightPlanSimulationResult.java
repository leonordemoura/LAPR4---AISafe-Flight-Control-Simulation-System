package aiSafe.flight.application.validate.simulator;

import java.util.Locale;
import java.util.Optional;

public final class FlightPlanSimulationResult {

    private final boolean success;
    private final double availableFuelKg;
    private final double requiredFuelKg;
    private final String message;

    private FlightPlanSimulationResult(
            final boolean success,
            final double availableFuelKg,
            final double requiredFuelKg,
            final String message
    ) {
        this.success = success;
        this.availableFuelKg = availableFuelKg;
        this.requiredFuelKg = requiredFuelKg;
        this.message = message;
    }

    public static FlightPlanSimulationResult success(
            final double availableFuelKg,
            final double requiredFuelKg
    ) {
        return new FlightPlanSimulationResult(
                true,
                availableFuelKg,
                requiredFuelKg,
                "Simulation succeeded. Estimated fuel required: "
                        + formatKg(requiredFuelKg)
                        + " kg; available: "
                        + formatKg(availableFuelKg)
                        + " kg.");
    }

    public static FlightPlanSimulationResult insufficientFuel(
            final double availableFuelKg,
            final double requiredFuelKg
    ) {
        return new FlightPlanSimulationResult(
                false,
                availableFuelKg,
                requiredFuelKg,
                "Insufficient fuel for the planned trajectory. Estimated required: "
                        + formatKg(requiredFuelKg)
                        + " kg; available: "
                        + formatKg(availableFuelKg)
                        + " kg.");
    }

    public static FlightPlanSimulationResult failure(final String message) {
        return new FlightPlanSimulationResult(false, 0, 0, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public double availableFuelKg() {
        return availableFuelKg;
    }

    public double requiredFuelKg() {
        return requiredFuelKg;
    }

    public Optional<String> message() {
        return message == null || message.isBlank() ? Optional.empty() : Optional.of(message);
    }

    private static String formatKg(final double value) {
        return String.format(Locale.ROOT, "%.0f", value);
    }
}
