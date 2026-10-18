package aiSafe.aircraft.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class CurrentEngine implements ValueObject, Serializable {

    @Column(name = "engine_model_id", nullable = false)
    private String engineModelId;

    protected CurrentEngine() {
        // for ORM
    }

    private CurrentEngine(final String engineModelId) {
        this.engineModelId = engineModelId;
    }

    public static CurrentEngine valueOf(final String engineModelId) {
        if (engineModelId == null || engineModelId.trim().isEmpty()) {
            throw new IllegalArgumentException("Engine model id cannot be null or empty.");
        }
        return new CurrentEngine(engineModelId.trim());
    }

    public String value() { return engineModelId; }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CurrentEngine that = (CurrentEngine) o;
        return engineModelId.equals(that.engineModelId);
    }

    @Override
    public int hashCode() { return engineModelId.hashCode(); }

    @Override
    public String toString() { return engineModelId; }
}