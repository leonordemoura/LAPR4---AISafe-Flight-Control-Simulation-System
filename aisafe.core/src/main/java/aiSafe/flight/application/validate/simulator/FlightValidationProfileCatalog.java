package aiSafe.flight.application.validate.simulator;

import aiSafe.flight.application.importplan.scomp.FlightProfileStep;

import java.util.List;

/**
 * Climb/descent profiles for US085 fuel validation.
 */
public final class FlightValidationProfileCatalog {

    private static final List<FlightProfileStep> BASE_CLIMB = List.of(
            new FlightProfileStep(0, 210, 12.0),
            new FlightProfileStep(1000, 210, 11.5),
            new FlightProfileStep(2000, 220, 11.0),
            new FlightProfileStep(3000, 230, 10.5),
            new FlightProfileStep(4000, 250, 10.0),
            new FlightProfileStep(5000, 260, 9.5),
            new FlightProfileStep(6000, 290, 8.5),
            new FlightProfileStep(7000, 290, 8.0),
            new FlightProfileStep(8000, 290, 7.5),
            new FlightProfileStep(9000, 300, 7.0),
            new FlightProfileStep(10000, 300, 6.5),
            new FlightProfileStep(11000, 300, 6.0),
            new FlightProfileStep(12000, 300, 5.5)
    );

    private static final List<FlightProfileStep> BASE_DESCEND = List.of(
            new FlightProfileStep(0, 140, 5.0),
            new FlightProfileStep(1000, 180, 7.0),
            new FlightProfileStep(2000, 250, 7.0),
            new FlightProfileStep(3000, 250, 8.0),
            new FlightProfileStep(4000, 270, 8.0),
            new FlightProfileStep(5000, 290, 10.0),
            new FlightProfileStep(6000, 300, 10.0),
            new FlightProfileStep(7000, 300, 10.0),
            new FlightProfileStep(8000, 300, 10.0),
            new FlightProfileStep(9000, 300, 10.0),
            new FlightProfileStep(10000, 300, 10.0),
            new FlightProfileStep(11000, 300, 10.0),
            new FlightProfileStep(12000, 300, 10.0)
    );

    private final List<FlightProfileStep> climb;
    private final List<FlightProfileStep> descend;

    public FlightValidationProfileCatalog(final SimulationCalibration calibration) {
        this(scaleSteps(BASE_CLIMB, calibration.climbRateMultiplier()), BASE_DESCEND);
    }

    FlightValidationProfileCatalog(
            final List<FlightProfileStep> climb,
            final List<FlightProfileStep> descend
    ) {
        this.climb = List.copyOf(climb);
        this.descend = List.copyOf(descend);
    }

    public static FlightValidationProfileCatalog defaults() {
        return new FlightValidationProfileCatalog(SimulationCalibration.OPERATIONAL);
    }

    public List<FlightProfileStep> climb() {
        return climb;
    }

    public List<FlightProfileStep> descend() {
        return descend;
    }

    private static List<FlightProfileStep> scaleSteps(
            final List<FlightProfileStep> steps,
            final double climbRateMultiplier
    ) {
        if (Math.abs(climbRateMultiplier - 1.0) < 1e-6) {
            return steps;
        }
        return steps.stream()
                .map(step -> new FlightProfileStep(
                        step.altitudeMeters(),
                        step.speedKnots(),
                        step.rateMetersPerSecond() * climbRateMultiplier))
                .toList();
    }
}
