package aiSafe.simulationreport.domain;

import eapli.framework.domain.model.ValueObject;

import java.io.Serializable;
import java.util.Objects;

public class VelocityVector implements ValueObject, Serializable {

    private final double vx;
    private final double vy;
    private final double vz;

    public VelocityVector(final double vx, final double vy, final double vz) {
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
    }

    public double vx() {
        return vx;
    }

    public double vy() {
        return vy;
    }

    public double vz() {
        return vz;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof VelocityVector that)) return false;
        return Double.compare(that.vx, vx) == 0
                && Double.compare(that.vy, vy) == 0
                && Double.compare(that.vz, vz) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vx, vy, vz);
    }
}
