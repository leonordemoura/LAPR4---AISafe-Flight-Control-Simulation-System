package aiSafe.simulationreport.domain;

import eapli.framework.domain.model.ValueObject;

import java.io.Serializable;
import java.util.Objects;

public class Position implements ValueObject, Serializable {

    private final double x;
    private final double y;

    public Position(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Position that)) return false;
        return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
