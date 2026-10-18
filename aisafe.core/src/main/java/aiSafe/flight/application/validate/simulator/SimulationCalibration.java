package aiSafe.flight.application.validate.simulator;

/**
 * Tunes fuel simulation for operational short-haul validation (A320-class aircraft from DB).
 */
public record SimulationCalibration(
        double fuelFlowFactor,
        double climbRateMultiplier
) {

    /**
     * Calibrated for realistic short-haul trip fuel (~2–2.5 t OPO–LIS on A320).
     * TSFC from the engine model is scaled to reflect operational thrust settings,
     * not max continuous thrust.
     */
    public static final SimulationCalibration OPERATIONAL =
            new SimulationCalibration(0.14, 2.0);
}
