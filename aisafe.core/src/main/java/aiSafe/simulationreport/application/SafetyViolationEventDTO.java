package aiSafe.simulationreport.application;

public class SafetyViolationEventDTO {

    public final int flightAId;
    public final int flightBId;
    public final int minutesA;
    public final int minutesB;
    public final double xA;
    public final double yA;
    public final double altA;
    public final double xB;
    public final double yB;
    public final double altB;
    public final double vxA;
    public final double vyA;
    public final double vzA;
    public final double vxB;
    public final double vyB;
    public final double vzB;
    public final double horizontalDistance;
    public final double verticalDistance;

    public SafetyViolationEventDTO(
            final int flightAId,
            final int flightBId,
            final int minutesA,
            final int minutesB,
            final double xA,
            final double yA,
            final double altA,
            final double xB,
            final double yB,
            final double altB,
            final double vxA,
            final double vyA,
            final double vzA,
            final double vxB,
            final double vyB,
            final double vzB,
            final double horizontalDistance,
            final double verticalDistance
    ) {
        this.flightAId = flightAId;
        this.flightBId = flightBId;
        this.minutesA = minutesA;
        this.minutesB = minutesB;
        this.xA = xA;
        this.yA = yA;
        this.altA = altA;
        this.xB = xB;
        this.yB = yB;
        this.altB = altB;
        this.vxA = vxA;
        this.vyA = vyA;
        this.vzA = vzA;
        this.vxB = vxB;
        this.vyB = vyB;
        this.vzB = vzB;
        this.horizontalDistance = horizontalDistance;
        this.verticalDistance = verticalDistance;
    }
}
