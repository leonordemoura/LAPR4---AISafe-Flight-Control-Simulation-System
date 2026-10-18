package aiSafe.aircraftmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class MakerName implements ValueObject, Serializable {
    @Column(nullable = false, unique = true)
    private String name;

    protected MakerName(){
        // for ORM
    }

    public MakerName(final String name) {
        if (name == null || name.isEmpty()){
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name;
    }

    public static MakerName valueOf(final String name) {
        Preconditions.nonEmpty(name, "Maker name should neither be null nor empty");
        return new MakerName(name);
    }

    public String name() {
        return this.name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MakerName that = (MakerName) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
