package aiSafe.airport.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class Country_Airport implements ValueObject, Serializable {
    @Column(nullable = false, unique = true, name = "name")
    private String name;
    @Column(nullable = false, unique = true, name = "code")
    private String code;

    protected Country_Airport(){
        // for ORM
    }

    public Country_Airport(final String name, final String code) {
        Preconditions.ensure(name != null, "Country name cannot be null");
        Preconditions.ensure(code != null, "Country code cannot be null");
        Preconditions.ensure(!name.isBlank(), "Country name must not be empty");
        Preconditions.ensure(!code.isBlank(), "Country code must not be empty");
        Preconditions.ensure(
                code.matches("[A-Z]{2}"),
                "Country code must contain exactly 2 uppercase letters"
        );this.name = name;
        this.code = code;
    }

    public static Country_Airport valueOf(final String name, final String code) {
        return new Country_Airport(name, code);
    }

    public String name() {
        return this.name;
    }

    public String code() {
        return this.code;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Country_Airport that = (Country_Airport) o;
        return name.equals(that.name) && code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + code.hashCode();
    }
}
