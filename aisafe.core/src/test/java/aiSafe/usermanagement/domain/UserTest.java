package aiSafe.usermanagement.domain;

import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    // =========================================================
    // AUX METHOD
    // =========================================================

    private User buildValidUser() {
        return new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.ADMIN
        );
    }

    // =========================================================
    // TEST 1
    // =========================================================

    @Test
    void ensureUserIsCreatedSuccessfullyWithValidData() {

        final var user = buildValidUser();

        assertNotNull(user);
        assertEquals(AisafeRoles.ADMIN, user.assignedRole());
        assertNotNull(user.email());
        assertNotNull(user.fullName());
        assertNotNull(user.phoneNumber());
    }

    // =========================================================
    // TEST 2
    // =========================================================

    @Test
    void ensureUserCannotBeCreatedWithNullFullName() {

        assertThrows(IllegalArgumentException.class, () ->
                new User(
                        null,
                        new Email("john.doe@company.com"),
                        new PhoneNumber(351, 912345678),
                        AisafeRoles.ADMIN
                )
        );
    }

    // =========================================================
    // TEST 3
    // =========================================================

    @Test
    void ensureUserCannotBeCreatedWithNullEmail() {

        assertThrows(IllegalArgumentException.class, () ->
                new User(
                        new FullName("John", "Doe"),
                        null,
                        new PhoneNumber(351, 912345678),
                        AisafeRoles.ADMIN
                )
        );
    }

    // =========================================================
    // TEST 4
    // =========================================================

    @Test
    void ensureUserCannotBeCreatedWithNullPhoneNumber() {

        assertThrows(IllegalArgumentException.class, () ->
                new User(
                        new FullName("John", "Doe"),
                        new Email("john.doe@company.com"),
                        null,
                        AisafeRoles.ADMIN
                )
        );
    }

    // =========================================================
    // TEST 5
    // =========================================================

    @Test
    void ensureUserCannotBeCreatedWithNullRole() {

        assertThrows(IllegalArgumentException.class, () ->
                new User(
                        new FullName("John", "Doe"),
                        new Email("john.doe@company.com"),
                        new PhoneNumber(351, 912345678),
                        null
                )
        );
    }

    // =========================================================
    // TEST 6
    // =========================================================

    @Test
    void ensureInvalidEmailFormatIsRejected() {

        assertThrows(IllegalArgumentException.class, () ->
                new Email("invalid-email-format")
        );
    }

    // =========================================================
    // TEST 7
    // =========================================================

    @Test
    void ensureEmailDomainMustBeAuthorized() {

        assertThrows(IllegalArgumentException.class, () ->
                new Email("john@gmail.com")
        );
    }

    // =========================================================
    // TEST 8
    // =========================================================

    @Test
    void ensureInvalidPhoneNumberIsRejected() {

        assertThrows(IllegalArgumentException.class, () ->
                new PhoneNumber(351, -123456)
        );
    }

    // =========================================================
    // TEST 9
    // =========================================================

    @Test
    void ensureFullNameCannotBeEmpty() {

        assertThrows(IllegalArgumentException.class, () ->
                new FullName("", "")
        );
    }

    // =========================================================
    // TEST 10
    // =========================================================

    @Test
    void ensureAlsafeProfileCanBeAttachedSuccessfully() {

        final var user = buildValidUser();

        final var profile = new AlsafeProfile(
                new SecurityClearance(java.time.LocalDate.now().plusYears(1)),
                new SkillsAssessment(java.time.LocalDate.now().minusYears(1))
        );

        user.attachAlsafeProfile(profile);

        assertNotNull(user.alsafeProfile());
    }

    // =========================================================
    // TEST 11
    // =========================================================

    @Test
    void ensureNullAlsafeProfileIsRejected() {

        final var user = buildValidUser();

        assertThrows(IllegalArgumentException.class, () ->
                user.attachAlsafeProfile(null)
        );
    }

    // =========================================================
    // TEST 12
    // =========================================================

    @Test
    void ensureSameAsReturnsTrueForSameEmail() {

        final var user1 = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.ADMIN
        );

        final var user2 = new User(
                new FullName("Johnny", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 987654321),
                AisafeRoles.BACKOFFICE_OPERATOR
        );

        assertTrue(user1.sameAs(user2));
    }

    // =========================================================
    // TEST 13
    // =========================================================

    @Test
    void ensureSameAsReturnsFalseForDifferentEmail() {

        final var user1 = new User(
                new FullName("John", "Doe"),
                new Email("john1@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.ADMIN
        );

        final var user2 = new User(
                new FullName("John", "Doe"),
                new Email("john2@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.ADMIN
        );

        assertFalse(user1.sameAs(user2));
    }

    // =========================================================
    // TEST 14
    // =========================================================

    @Test
    void ensureAssignedRoleReturnsCorrectRole() {

        final var user = buildValidUser();

        assertEquals(AisafeRoles.ADMIN, user.assignedRole());
    }

    // =========================================================
    // TEST 15
    // =========================================================

    @Test
    void ensureIdentityIsNullBeforePersistence() {

        final var user = buildValidUser();

        assertNull(user.identity());
    }

    @Test
    void ensureEqualsReturnsTrueForSameId() throws Exception {

        final var u1 = buildValidUser();
        final var u2 = buildValidUser();

        var idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);

        idField.set(u1, 1L);
        idField.set(u2, 1L);

        assertEquals(u1, u2);
    }

    @Test
    void ensureEqualsReturnsFalseForDifferentIds() throws Exception {

        final var u1 = buildValidUser();
        final var u2 = buildValidUser();

        var idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);

        idField.set(u1, 1L);
        idField.set(u2, 2L);

        assertNotEquals(u1, u2);
    }

    @Test
    void ensureEqualsReturnsFalseWhenIdIsNull() {

        final var u1 = buildValidUser();
        final var u2 = buildValidUser();

        assertNotEquals(u1, u2);
    }

    @Test
    void ensureHashCodeIsEqualForSameId() throws Exception {

        final var u1 = buildValidUser();
        final var u2 = buildValidUser();

        var idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);

        idField.set(u1, 10L);
        idField.set(u2, 10L);

        assertEquals(u1.hashCode(), u2.hashCode());
    }

    @Test
    void ensureHashCodeDiffersForDifferentIds() throws Exception {

        final var u1 = buildValidUser();
        final var u2 = buildValidUser();

        var idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);

        idField.set(u1, 1L);
        idField.set(u2, 2L);

        assertNotEquals(u1.hashCode(), u2.hashCode());
    }
}