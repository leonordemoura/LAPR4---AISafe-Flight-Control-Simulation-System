package aiSafe.flight.dsl;

/**
 * Tracks structural elements while parsing (required listener for US083).
 */
final class FlightDslStructureListener extends aiSafe.flight.dsl.FlightDSLBaseListener {

    private int legCount;
    private int segmentCount;

    @Override
    public void enterLeg(final aiSafe.flight.dsl.FlightDSLParser.LegContext ctx) {
        legCount++;
    }

    @Override
    public void enterSegment(final aiSafe.flight.dsl.FlightDSLParser.SegmentContext ctx) {
        segmentCount++;
    }

    int legCount() {
        return legCount;
    }

    int segmentCount() {
        return segmentCount;
    }
}
