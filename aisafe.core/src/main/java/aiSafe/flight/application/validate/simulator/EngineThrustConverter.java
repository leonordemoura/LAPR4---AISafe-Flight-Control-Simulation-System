package aiSafe.flight.application.validate.simulator;

import aiSafe.aircraftmanagement.domain.Power;
import aiSafe.aircraftmanagement.domain.Unit;

/**
 * Estimates sea-level thrust (N) from rated engine power at a reference airspeed.
 */
final class EngineThrustConverter {

    private static final double HORSEPOWER_TO_WATTS = 745.699872;
    private static final double MIN_REFERENCE_SPEED_MPS = 50.0;

    private EngineThrustConverter() {
    }

    static double thrustSeaLevelNewtons(final Power power, final double referenceSpeedMps) {
        final double watts = switch (power.unit()) {
            case HORSEPOWER -> power.value() * HORSEPOWER_TO_WATTS;
            case KILOWATT -> power.value() * 1000.0;
        };
        return watts / Math.max(referenceSpeedMps, MIN_REFERENCE_SPEED_MPS);
    }
}
