package aiSafe.PilotCertification.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PilotID implements Comparable<PilotID> {

    @Column(name = "pilot_id")
    private String value;

    protected PilotID() {}

    public PilotID(String id) {
        Preconditions.ensure(id != null, "id must not be null");
        Preconditions.ensure(!id.isEmpty(), "id must not be empty");
        this.value = id;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PilotID that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public int compareTo(PilotID o) {
        return value.compareTo(o.value);
    }

    @Override
    public String toString() {
        return value;
    }
}