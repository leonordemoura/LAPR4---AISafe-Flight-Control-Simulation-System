package aiSafe.costumermanagement.domain;

import aiSafe.usermanagement.domain.Email;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Objects;

@Embeddable
public class Membership {
    @Column(name = "member_email", nullable = false)
    private Email memberEmail;

    @Column(name = "member_position", nullable = false)
    private Position position;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", nullable = false)
    private MemberType memberType;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    protected Membership() {
        // for ORM
    }

    public Membership(final Email memberEmail, final Position position, final MemberType memberType) {
        if (memberEmail == null || position == null || memberType == null) {
            throw new IllegalArgumentException("Invalid company membership data.");
        }
        this.memberEmail = memberEmail;
        this.position = position;
        this.memberType = memberType;
        this.active = true;
    }

    public Email memberEmail() {
        return memberEmail;
    }

    public Position position() {
        return position;
    }

    public MemberType memberType() {
        return memberType;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Membership that)) return false;
        return Objects.equals(memberEmail, that.memberEmail) && position.equals(that.position) && memberType == that.memberType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberEmail);
    }
}
