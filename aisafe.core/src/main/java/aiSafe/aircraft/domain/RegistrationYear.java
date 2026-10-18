package aiSafe.aircraft.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
public class RegistrationYear implements ValueObject, Serializable {

    @Column(name = "registration_year")
    private int year;

    protected RegistrationYear() {
    }

    public RegistrationYear(final int year) {

        final int currentYear = LocalDate.now().getYear();

        Preconditions.ensure(
                year > 1900 && year <= currentYear,
                "Invalid registration year"
        );

        this.year = year;
    }

    public int value() {
        return year;
    }

    public int age() {
        return LocalDate.now().getYear() - year;
    }

    @Override
    public String toString() {
        return String.valueOf(year);
    }
}