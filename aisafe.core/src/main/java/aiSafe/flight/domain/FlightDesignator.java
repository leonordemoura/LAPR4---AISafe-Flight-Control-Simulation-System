package aiSafe.flight.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FlightDesignator implements ValueObject, Serializable {

    private String airlineCode;   // 2 letters (IATA)
    private int flightNumber;  // 1-4 digits
    private String operationalSuffix; // optional 1 letter

    protected FlightDesignator() {}

    public FlightDesignator(final String airlineCode,
                            final int flightNumber,
                            final String operationalSuffix) {
        Preconditions.nonEmpty(airlineCode, "Airline code is mandatory");
        Preconditions.noneNull(flightNumber, "Flight number is mandatory");
        Preconditions.ensure(airlineCode.length() == 2, "Airline code must be 2 letters");
        Preconditions.ensure(flightNumber >= 1 && flightNumber <= 9999, "Flight number must be 1 to 4 digits");
        Preconditions.ensure(operationalSuffix == null || operationalSuffix.isEmpty() || (operationalSuffix.length() == 1 && Character.isLetter(operationalSuffix.charAt(0))),"Operational suffix must be 1 letter if provided");        this.airlineCode = airlineCode.toUpperCase();
        this.flightNumber = flightNumber;
        this.operationalSuffix = (operationalSuffix != null) ? operationalSuffix.toUpperCase() : "";
    }

    public static FlightDesignator of(final String airlineCode, final int flightNumber, final String operationalSuffix) {
        return new FlightDesignator(airlineCode, flightNumber, operationalSuffix);
    }
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof FlightDesignator that)) return false;
        return Objects.equals(this.airlineCode, that.airlineCode)
                && Objects.equals(this.flightNumber, that.flightNumber)
                && Objects.equals(this.operationalSuffix, that.operationalSuffix);
    }

    public String airlineCode() {
        return airlineCode;
    }

    public int flightNumber() {
        return flightNumber;
    }

    public String operationalSuffix() {
        return operationalSuffix;
    }

    @Override
    public int hashCode() {
        return Objects.hash(airlineCode, flightNumber, operationalSuffix);
    }

    @Override
    public String toString() {
        return airlineCode + flightNumber + operationalSuffix;
    }
}