package aiSafe.aircraft.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class CabinConfiguration implements ValueObject, Serializable {

    @Column(name = "economy_seats", nullable = false)
    private int economySeats;

    @Column(name = "business_seats", nullable = false)
    private int businessSeats;

    @Column(name = "first_class_seats", nullable = false)
    private int firstClassSeats;

    protected CabinConfiguration() {
        // for ORM
    }

    public CabinConfiguration(int economySeats, int businessSeats, int firstClassSeats) {
        if (economySeats < 0 || businessSeats < 0 || firstClassSeats < 0) {
            throw new IllegalArgumentException("Seat values cannot be negative");
        }
        if (economySeats + businessSeats + firstClassSeats == 0) {
            throw new IllegalArgumentException("Aircraft must have at least one seat");
        }
        this.economySeats = economySeats;
        this.businessSeats = businessSeats;
        this.firstClassSeats = firstClassSeats;
    }

    public int totalSeats() { return economySeats + businessSeats + firstClassSeats; }
    public int economySeats() { return economySeats; }
    public int businessSeats() { return businessSeats; }
    public int firstClassSeats() { return firstClassSeats; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CabinConfiguration that)) return false;
        return economySeats == that.economySeats &&
                businessSeats == that.businessSeats &&
                firstClassSeats == that.firstClassSeats;
    }

    @Override
    public int hashCode() {
        return 31 * economySeats + 17 * businessSeats + firstClassSeats;
    }
}