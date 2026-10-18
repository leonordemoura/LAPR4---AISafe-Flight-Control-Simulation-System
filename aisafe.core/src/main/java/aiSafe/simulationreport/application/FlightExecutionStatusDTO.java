package aiSafe.simulationreport.application;

public class FlightExecutionStatusDTO {

    public final int flightId;
    public final int violationCount;
    public final Double performance;
    public final String summaryMessage;

    public FlightExecutionStatusDTO(
            final int flightId,
            final int violationCount,
            final Double performance,
            final String summaryMessage
    ) {
        this.flightId = flightId;
        this.violationCount = violationCount;
        this.performance = performance;
        this.summaryMessage = summaryMessage;
    }
}
