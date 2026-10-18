package aiSafe.aircraftModel.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class AircraftModelName {

    private String aircraftModelName;

    protected AircraftModelName() {
        //for ORM
    }

    public AircraftModelName(String aircraftModelName) {
        Preconditions.ensure(aircraftModelName != null, "aircraftModelName must not be null");
        Preconditions.ensure(!aircraftModelName.isEmpty(), "aircraftModelName must not be empty");
        this.aircraftModelName = aircraftModelName;
    }

    public String value(){
        return aircraftModelName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AircraftModelName that)) return false;
        return Objects.equals(aircraftModelName, that.aircraftModelName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aircraftModelName);
    }

    @Override
    public String toString() {
        return aircraftModelName;
    }
}
