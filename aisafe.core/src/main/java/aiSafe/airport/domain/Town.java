package aiSafe.airport.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class Town {
    @Column(name = "town")
    private String town;

    protected Town() {
        //for ORM
    }

    public Town(String town) {
        Preconditions.ensure(town != null, "Town must not be null");
        Preconditions.ensure(!town.isBlank(), "Town must not be empty");
        this.town = town;
    }

    public String value(){
        return town;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Town that)) return false;
        return Objects.equals(town, that.town);
    }

    @Override
    public int hashCode() {
        return Objects.hash(town);
    }

    @Override
    public String toString() {
        return town;
    }
}
