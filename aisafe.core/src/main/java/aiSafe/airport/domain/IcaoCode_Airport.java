package aiSafe.airport.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class IcaoCode_Airport {

    @Column(name = "icao_code", nullable = false, unique = true)
    private String icaoCode;

    protected IcaoCode_Airport() {
        // for ORM
    }

    public IcaoCode_Airport(final String icaoCode) {

        Preconditions.ensure(icaoCode != null, "icaoCode must not be null");
        Preconditions.ensure(!icaoCode.isBlank(), "icaoCode must not be empty");

        Preconditions.ensure(
                icaoCode.matches("[A-Z]{4}"),
                "icaoCode must contain exactly 4 uppercase letters"
        );

        this.icaoCode = icaoCode;
    }

    public String value() {
        return icaoCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IcaoCode_Airport that)) return false;
        return Objects.equals(icaoCode, that.icaoCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(icaoCode);
    }

    @Override
    public String toString() {
        return icaoCode;
    }
}