package aiSafe.usermanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@Embeddable
public class Email implements ValueObject, Serializable {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Column(name = "email")
    private String emailAddress;

    @Embedded
    private EmailDomain domain;

    protected Email() {
        // For ORM
    }

    public Email(final String emailAddress) {
        Preconditions.noneNull(emailAddress, "Email must not be null");

        final String normalized = emailAddress.trim().toLowerCase(Locale.ROOT);

        Preconditions.nonEmpty(normalized, "Email must not be empty");

        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        final int at = normalized.lastIndexOf('@');
        if (at < 0 || at == normalized.length() - 1) {
            throw new IllegalArgumentException("Invalid email format");
        }


        final String domainPart = normalized.substring(at + 1);
        this.domain = new EmailDomain(domainPart);
        this.emailAddress = normalized;
    }

    public String value() {
        return emailAddress;
    }

    public EmailDomain domain() {
        return domain;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Email email)) return false;
        return Objects.equals(emailAddress, email.emailAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emailAddress);
    }

    @Override
    public String toString() {
        return emailAddress;
    }
}

