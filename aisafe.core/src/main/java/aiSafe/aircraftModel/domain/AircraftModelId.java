package aiSafe.aircraftModel.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class AircraftModelId implements Comparable<AircraftModelId> {

    @Column(name = "aircraft_model_id")
    private String id;

    protected AircraftModelId() {}

    public AircraftModelId(String id) {
        Preconditions.ensure(id != null, "id must not be null");
        Preconditions.ensure(!id.isEmpty(), "id must not be empty");
        this.id = id;
    }

    public String value() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AircraftModelId that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(AircraftModelId o) {
        return id.compareTo(o.id);
    }

    @Override
    public String toString() {
        return id;
    }
}