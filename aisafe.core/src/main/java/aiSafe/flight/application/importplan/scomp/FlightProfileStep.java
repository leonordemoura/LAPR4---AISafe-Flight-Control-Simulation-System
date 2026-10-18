package aiSafe.flight.application.importplan.scomp;

/**
 * One altitude/speed/rate point for SCOMP {@code FlightProfile.Climb} or {@code Descend}.
 */
public record FlightProfileStep(
        double altitudeMeters,
        double speedKnots,
        double rateMetersPerSecond
) {
}
