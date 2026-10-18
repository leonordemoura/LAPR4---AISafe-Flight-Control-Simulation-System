package aiSafe.airtransportcompany.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class IataCode implements ValueObject, Serializable {
    @Column (name = "iataCode", nullable = false, unique = true)
    private String code;

    protected IataCode() {
        // for ORM
    }

    private IataCode(final String code) {
        this.code = code;
    }

    public static IataCode valueOf(final String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("IATA code cannot be null or empty.");
        }
        if (code.length() != 2) {
            throw new IllegalArgumentException("IATA code must be 2  characters long.");
        }
        return new IataCode(code.toUpperCase());
    }

    public String code() {
        return this.code;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final IataCode that = (IataCode) o;
        return code.equals(that.code);
     }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

}
