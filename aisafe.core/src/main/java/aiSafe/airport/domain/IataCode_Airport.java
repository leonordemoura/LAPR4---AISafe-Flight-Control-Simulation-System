package aiSafe.airport.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class IataCode_Airport {

    @Column(name = "iata_code", nullable = false, unique = true)
    private String iataCode;

    protected IataCode_Airport() {
        // for ORM
    }

    public IataCode_Airport(final String iataCode) {

        Preconditions.ensure(iataCode != null, "IataCode must not be null");
        Preconditions.ensure(!iataCode.isBlank(), "IataCode must not be empty");

        Preconditions.ensure(
                iataCode.matches("[A-Z]{3}"),
                "IataCode must contain exactly 3 uppercase letters"
        );

        this.iataCode = iataCode;
    }

    public String value() {
        return iataCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IataCode_Airport that)) return false;
        return Objects.equals(iataCode, that.iataCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iataCode);
    }

    @Override
    public String toString() {
        return iataCode;
    }
}