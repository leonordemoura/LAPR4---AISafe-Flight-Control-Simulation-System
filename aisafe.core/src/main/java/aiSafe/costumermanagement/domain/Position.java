package aiSafe.costumermanagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Position {
    @Column(nullable = false)
    private String position;

    protected Position() {
        // for ORM
    }

    public Position(final String position) {
        if (position == null || position.isBlank()) {
            throw new IllegalArgumentException("Position cannot be null or blank.");
        }
        this.position = position.trim();
    }

    public static Position valueOf(final String position) {
        return new Position(position);
    }

    public String position() {
        return position;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Position that)) return false;
        return position.equals(that.position);
    }

    @Override
    public int hashCode() {
        return position.hashCode();
    }
}
