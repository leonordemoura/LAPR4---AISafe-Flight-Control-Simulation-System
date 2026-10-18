package aiSafe.usermanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Embeddable
public class EmailDomain implements ValueObject, Serializable {
    private static final Set<String> AUTHORIZED_DOMAINS = Set.of(
            "aisafe.local",
            "company.com"
    );

    @Column(name = "email_domain")
    private String domain;

    protected EmailDomain() {
        // For ORM
    }

    public EmailDomain(final String domain) {

        Preconditions.noneNull(domain, "Email domain must not be null");
        Preconditions.ensure(!domain.trim().isEmpty(), "Email domain must not be empty");

        final String normalized = domain.trim().toLowerCase(Locale.ROOT);
        if (!AUTHORIZED_DOMAINS.contains(normalized)) {
            throw new IllegalArgumentException("Email domain is not authorized: " + normalized);
        }
        this.domain = normalized;
    }

    public String value() {
        return domain;
    }

    public static Set<String> authorizedDomains() {
        return AUTHORIZED_DOMAINS;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailDomain that)) return false;
        return Objects.equals(domain, that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain);
    }

    @Override
    public String toString() {
        return domain;
    }
}

