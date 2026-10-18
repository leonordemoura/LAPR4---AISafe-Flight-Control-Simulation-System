package aiSafe.flight.application.validate.simulator;

/**
 * One engine currently active on the aircraft ({@code CurrentEngine} resolved to {@code EngineModel}).
 */
public record ActiveEngineParameters(
        double thrustSeaLevelNewtons,
        double tsfcNewtonPerNewtonSecond
) {
}
