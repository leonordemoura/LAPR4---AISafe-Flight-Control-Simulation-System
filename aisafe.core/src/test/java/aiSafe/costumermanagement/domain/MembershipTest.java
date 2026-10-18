package aiSafe.costumermanagement.domain;

import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.FullName;
import aiSafe.usermanagement.domain.PhoneNumber;
import aiSafe.usermanagement.domain.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MembershipTest {

    @Test
    public void ensureCollaboratorEmailExists() {
        // Arrange
        final var user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.PILOT
        );

        Position position = Position.valueOf("Chief Pilot");
        MemberType memberType = MemberType.PILOT;
        Membership membership = new Membership(user.email(), position, memberType);

        // Act & Assert
        assertNotNull(membership.memberEmail(), "Collaborator email should not be null");
        assertEquals(membership.memberEmail(), user.email(), "Collaborator email should be the same as the user's email");
    }

    @Test
    public void ensureCollaboratorHasPosition() {
        // Arrange
        final var user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.PILOT
        );

        Position position = Position.valueOf("Chief Pilot");
        MemberType memberType = MemberType.PILOT;
        Membership membership = new Membership(user.email(), position, memberType);

        // Act & Assert
        assertNotNull(position, "Collaborator position should not be null");
        assertEquals(position, membership.position(), "Collaborator position should be 'Chief Pilot'");
    }

    @Test
    public void ensureMembershipIsActiveByDefault() {
        // Arrange
        final var user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.PILOT
        );

        Position position = Position.valueOf("Chief Pilot");
        MemberType memberType = MemberType.PILOT;
        Membership membership = new Membership(user.email(), position, memberType);

        // Act & Assert
        assertTrue(membership.isActive(), "Membership should be active by default");
    }

    @Test
    public void ensureMembershipHasCorrectMemberType() {
        // Arrange
        final var user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.PILOT
        );

        Position position = Position.valueOf("Chief Pilot");
        MemberType memberType = MemberType.PILOT;
        Membership membership = new Membership(user.email(), position, memberType);

        // Act & Assert
        assertNotNull(membership.memberType(), "Member type should not be null");
        assertEquals(MemberType.PILOT, membership.memberType(), "Member type should be PILOT");
    }

    @Test
    public void ensureMembershipCannotBeCreatedWithNullEmail() {
        // Arrange
        Position position = Position.valueOf("Chief Pilot");
        MemberType memberType = MemberType.PILOT;

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> new Membership(null, position, memberType),
                "Creating a Membership with null email should throw IllegalArgumentException");
    }

    @Test
    public void ensureMembershipCannotBeCreatedWithNullPosition() {
        // Arrange
        Email email = new Email("john.doe@company.com");
        MemberType memberType = MemberType.PILOT;

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> new Membership(email, null, memberType),
                "Creating a Membership with null position should throw IllegalArgumentException");
    }

    @Test
    public void ensureMembershipCannotBeCreatedWithNullMemberType() {
        // Arrange
        Email email = new Email("john.doe@company.com");
        Position position = Position.valueOf("Chief Pilot");

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> new Membership(email, null, null),
                "Creating a Membership with null member type should throw IllegalArgumentException");
    }

    @Test
    public void ensureMembershipEqualityAndHashCode() {
        // Arrange
        final Email email = new Email("john.doe@company.com");
        final Position position = Position.valueOf("Chief Pilot");
        final MemberType memberType = MemberType.PILOT;

        final Membership m1 = new Membership(email, position, memberType);
        final Membership m2 = new Membership(email, position, memberType);
        final Membership m3 = new Membership(new Email("other@company.com"), Position.valueOf("Co-Pilot"), MemberType.ATCC);

        // Act & Assert
        assertEquals(m1, m2, "Memberships with same data should be equal");
        assertEquals(m1.hashCode(), m2.hashCode(), "Equal Memberships should have same hash code");
        assertNotEquals(m1, m3, "Memberships with different data should not be equal");
    }
}