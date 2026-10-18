package aiSafe.flight.application.validate.simulator;

import java.util.List;

/**
 * Aircraft physics inputs for US085 simulation (from a concrete aircraft instance).
 */
public record SimulatorModelParameters(
        double wingAreaM2,
        double aspectRatio,
        double oswaldEfficiency,
        double emptyWeightKg,
        double payloadKg,
        double dragCoefficientZero,
        double cruiseSpeedKnots,
        double fuelDensityKgPerL,
        double minFuelAtArrivalKg,
        double startElevationMeters,
        double endElevationMeters,
        List<ActiveEngineParameters> activeEngines
) {

    public SimulatorModelParameters {
        activeEngines = List.copyOf(activeEngines);
        if (activeEngines.isEmpty()) {
            throw new IllegalArgumentException("At least one active engine is required for simulation.");
        }
        if (fuelDensityKgPerL <= 0) {
            throw new IllegalArgumentException("Fuel density must be positive.");
        }
    }

    public double totalThrustSeaLevelNewtons() {
        return activeEngines.stream()
                .mapToDouble(ActiveEngineParameters::thrustSeaLevelNewtons)
                .sum();
    }

    public int activeEngineCount() {
        return activeEngines.size();
    }
}
