package aiSafe.flight.dsl.model;

import aiSafe.flight.domain.FlightType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class DslFlightPlan {

    private final String identifier;
    private final FlightType flightType;
    private final List<DslLeg> legs;
    private final int line;

    public DslFlightPlan(
            final String identifier,
            final FlightType flightType,
            final List<DslLeg> legs,
            final int line) {
        this.identifier = identifier;
        this.flightType = flightType;
        this.legs = List.copyOf(legs);
        this.line = line;
    }

    public String identifier() {
        return identifier;
    }

    public FlightType flightType() {
        return flightType;
    }

    public List<DslLeg> legs() {
        return Collections.unmodifiableList(legs);
    }

    public int line() {
        return line;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof DslFlightPlan that)) return false;
        return line == that.line
                && Objects.equals(identifier, that.identifier)
                && flightType == that.flightType
                && Objects.equals(legs, that.legs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, flightType, legs, line);
    }

    @Override
    public String toString() {
        return "DslFlightPlan[" + identifier + ", " + flightType + ", legs=" + legs.size() + "]";
    }
}
