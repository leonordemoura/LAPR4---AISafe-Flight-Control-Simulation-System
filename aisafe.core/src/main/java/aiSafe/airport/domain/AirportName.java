package aiSafe.airport.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class AirportName {

    @Column(name = "airportName")
    private String airportName;

    protected AirportName() {
        //for ORM
    }

    public AirportName(String airportName) {
        Preconditions.ensure(airportName != null, "AirportName must not be null");
        Preconditions.ensure(!airportName.isBlank(), "AirportName must not be empty");
        this.airportName = airportName;
    }

    public String value(){
        return airportName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AirportName that)) return false;
        return Objects.equals(airportName, that.airportName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(airportName);
    }

    @Override
    public String toString() {
        return airportName;
    }
}
