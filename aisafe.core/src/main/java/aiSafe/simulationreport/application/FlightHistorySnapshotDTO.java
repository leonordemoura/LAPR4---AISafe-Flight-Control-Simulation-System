package aiSafe.simulationreport.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlightHistorySnapshotDTO {

    public final int flightId;
    public final List<HistoryPointDTO> points;

    public FlightHistorySnapshotDTO(final int flightId, final List<HistoryPointDTO> points) {
        this.flightId = flightId;
        this.points = points == null
                ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(points));
    }
}
