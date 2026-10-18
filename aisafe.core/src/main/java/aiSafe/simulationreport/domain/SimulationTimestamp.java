package aiSafe.simulationreport.domain;

import eapli.framework.domain.model.ValueObject;

import java.io.Serializable;
import java.util.Objects;

public class SimulationTimestamp implements ValueObject, Serializable {

    private final int minutesSinceMidnight;

    public SimulationTimestamp(final int minutesSinceMidnight) {
        if (minutesSinceMidnight < 0 || minutesSinceMidnight > 24 * 60) {
            throw new IllegalArgumentException("Invalid simulation timestamp.");
        }
        this.minutesSinceMidnight = minutesSinceMidnight;
    }

    public int minutesSinceMidnight() {
        return minutesSinceMidnight;
    }

    public String asTimeLabel() {
        final int hours = minutesSinceMidnight / 60;
        final int minutes = minutesSinceMidnight % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof SimulationTimestamp that)) return false;
        return minutesSinceMidnight == that.minutesSinceMidnight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minutesSinceMidnight);
    }
}
