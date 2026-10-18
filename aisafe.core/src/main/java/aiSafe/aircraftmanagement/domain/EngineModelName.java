package aiSafe.aircraftmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class EngineModelName implements ValueObject, Serializable {
    @Column (nullable = false)
    private String name;

    protected EngineModelName(){
        // for ORM
    }

    public EngineModelName(final String name) {
        Preconditions.nonEmpty(name, "Engine model name cannot be null or empty.");
        this.name = name;
    }

    public static EngineModelName valueOf(final String name) {
        return new EngineModelName(name);
    }

    public String name() {
        return this.name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final EngineModelName that = (EngineModelName) o;
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
