package aiSafe.simulationreport.application;

public class HistoryPointDTO {

    public final int minutesSinceMidnight;
    public final double x;
    public final double y;
    public final double altitude;

    public HistoryPointDTO(final int minutesSinceMidnight, final double x, final double y, final double altitude) {
        this.minutesSinceMidnight = minutesSinceMidnight;
        this.x = x;
        this.y = y;
        this.altitude = altitude;
    }
}
