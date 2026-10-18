package aiSafe.usermanagement.domain;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;

/**
 * AISafe domain user (US031). Persisted beside the framework auth {@code SystemUser} (US30).
 * <ul>
 *   <li>Business email is in {@link #email()} with domain authorization rules ({@link Email}).</li>
 *   <li>Login username should match {@link Email#value()} — this is the natural link between tables.</li>
 *   </ul>
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"email"}),
                @UniqueConstraint(columnNames = {"phone_code", "phone_number"})
        }
)
public class User implements AggregateRoot<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "firstName", column = @Column(name = "first_name", nullable = false)),
            @AttributeOverride(name = "lastName", column = @Column(name = "last_name", nullable = false))
    })
    private FullName fullName;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "emailAddress", column = @Column(name = "email", nullable = false, unique = true))
    })
    private Email email;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "code", column = @Column(name = "phone_code", nullable = false)),
            @AttributeOverride(name = "number", column = @Column(name = "phone_number", nullable = false))
    })
    private PhoneNumber phoneNumber;

    /** Authority string compatible with {@link eapli.framework.infrastructure.authz.domain.model.Role#valueOf(String)}. */
    @Column(name = "role_authority", nullable = false)
    private String roleAuthority;

    @Embedded
    private AlsafeProfile alsafeProfile;

    protected User() {
        // For ORM
    }

    public User(
            final FullName fullName,
            final Email email,
            final PhoneNumber phoneNumber,
            final Role assignedRole
    ) {
        Preconditions.noneNull(fullName, email, phoneNumber, assignedRole);

        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.roleAuthority = AisafeRoles.authorityKey(assignedRole);
    }

    @Override
    public Long identity() {
        return id;
    }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof User that)) return false;
        return Objects.equals(this.email, that.email);
    }

    public FullName fullName() {
        return fullName;
    }

    public Email email() {
        return email;
    }

    public PhoneNumber phoneNumber() {
        return phoneNumber;
    }

    public Role assignedRole() {
        return Role.valueOf(roleAuthority);
    }

    public AlsafeProfile alsafeProfile() {
        return alsafeProfile;
    }

    public void attachAlsafeProfile(final AlsafeProfile profile) {
        Preconditions.noneNull(profile, "Profile must be non-null");
        profile.ensureSecurityReadiness();
        this.alsafeProfile = profile;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof User that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
