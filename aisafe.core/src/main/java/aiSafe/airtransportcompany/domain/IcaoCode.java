package aiSafe.airtransportcompany.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class IcaoCode implements ValueObject, Serializable {
    @Column(name = "icaoCode", nullable = false, unique = true)
    private String code;

    protected IcaoCode() {
        // for ORM
    }

    private IcaoCode(final String code) {
        this.code = code;
    }

    public static IcaoCode valueOf(final String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("ICAO code cannot be null or empty.");
        }
        if (code.length() > 3 || code.length() < 2) {
            throw new IllegalArgumentException("ICAO code must be 2 or 3 characters long.");
        }
        return new IcaoCode(code.toUpperCase());
    }

    public String code() {
        return this.code;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final IcaoCode that = (IcaoCode) o;
        return code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

}
