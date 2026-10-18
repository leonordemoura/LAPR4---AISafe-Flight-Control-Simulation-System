package aiSafe.usermanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PhoneNumber implements ValueObject, Serializable {
    @Column(name = "phone_code")
    private Integer code;

    @Column(name = "phone_number")
    private Integer number;

    protected PhoneNumber() {
        // For ORM
    }

    public PhoneNumber(final Integer code, final Integer number) {

        Preconditions.noneNull(number, "Phone number must be non-null");
        Preconditions.noneNull(code, "Phone code must be non-null");
        Preconditions.ensure(code >= 0, "Phone code must be non-negative");
        Preconditions.ensure(number >= 0, "Phone number must be non-negative");
        String digits = number.toString();
        Preconditions.ensure(
                digits.length() >= 8 && digits.length() <= 15,
                "Phone number must have 8 to 15 digits"
        );

        this.code = code;
        this.number = number;
    }

    public Integer code() {
        return code;
    }

    public Integer number() {
        return number;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumber that)) return false;
        return Objects.equals(code, that.code) && Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, number);
    }

    @Override
    public String toString() {
        return "+" + code + " " + number;
    }
}

