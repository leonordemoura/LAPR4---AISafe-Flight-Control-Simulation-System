package aiSafe.simulationreport.domain;

import eapli.framework.domain.model.ValueObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FlightHistorySnapshot implements ValueObject, Serializable {

    private final int flightId;
    private final List<HistoryPoint> points;

    public FlightHistorySnapshot(final int flightId, final List<HistoryPoint> points) {
        if (flightId <= 0) {
            throw new IllegalArgumentException("Flight id is required.");
        }
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("Flight history must contain at least one point.");
        }
        this.flightId = flightId;
        this.points = Collections.unmodifiableList(new ArrayList<>(points));
    }

    public int flightId() {
        return flightId;
    }

    public List<HistoryPoint> points() {
        return points;
    }
}
