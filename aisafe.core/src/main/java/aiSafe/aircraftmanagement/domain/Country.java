package aiSafe.aircraftmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class Country implements ValueObject, Serializable {
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false, unique = true)
    private Integer numericCode;

    protected Country(){
        // for ORM
    }

    public Country(final String name, final Integer numericCode) {
        this.name = name;
        this.numericCode = numericCode;
    }

    public static Country valueOf(final String name, final Integer numericCode) {
        return new Country(name, numericCode);
    }

    public String name() {
        return this.name;
    }

    public Integer numericCode() {
        return this.numericCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Country that = (Country) o;
        return name.equals(that.name) && numericCode.equals(that.numericCode);
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + numericCode.hashCode();
    }
}
