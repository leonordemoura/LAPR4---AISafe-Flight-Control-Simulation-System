package aiSafe.usermanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneNumberTest {

    // =========================================================
    // SUCCESS CASES
    // =========================================================

    @Test
    void ensurePhoneNumberIsCreatedSuccessfully() {

        final var phone = new PhoneNumber(351, 912345678);

        assertNotNull(phone);
    }

    @Test
    void ensureCodeIsReturnedCorrectly() {

        final var phone = new PhoneNumber(351, 912345678);

        assertEquals(351, phone.code());
    }

    @Test
    void ensureNumberIsReturnedCorrectly() {

        final var phone = new PhoneNumber(351, 912345678);

        assertEquals(912345678, phone.number());
    }

    @Test
    void ensureToStringReturnsCorrectFormat() {

        final var phone = new PhoneNumber(351, 912345678);

        assertEquals("+351 912345678", phone.toString());
    }

    // =========================================================
    // VALIDATION TESTS
    // =========================================================

    @Test
    void ensurePhoneNumberCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () -> new PhoneNumber(351, null));
    }

    @Test
    void ensurePhoneCodeCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () -> new PhoneNumber(null, 912345678));
    }

    @Test
    void ensurePhoneCodeCannotBeNegative() {

        assertThrows(IllegalArgumentException.class, () -> new PhoneNumber(-1, 912345678));
    }

    @Test
    void ensurePhoneNumberCannotBeNegative() {

        assertThrows(IllegalArgumentException.class, () -> new PhoneNumber(351, -123));
    }

    @Test
    void ensurePhoneNumberMustHaveAtLeast8Digits() {

        assertThrows(IllegalArgumentException.class, () -> new PhoneNumber(351, 1234567));
    }


    // =========================================================
    // EQUALITY TESTS
    // =========================================================

    @Test
    void ensureSamePhoneNumbersAreEqual() {

        final var p1 = new PhoneNumber(351, 912345678);
        final var p2 = new PhoneNumber(351, 912345678);

        assertEquals(p1, p2);
    }

    @Test
    void ensureDifferentPhoneNumbersAreNotEqual() {

        final var p1 = new PhoneNumber(351, 912345678);
        final var p2 = new PhoneNumber(351, 999999999);

        assertNotEquals(p1, p2);
    }

    @Test
    void ensurePhoneNumberIsNotEqualToDifferentObjectType() {

        final var phone = new PhoneNumber(351, 912345678);

        assertNotEquals(phone, "+351 912345678");
    }

    @Test
    void ensurePhoneNumberIsEqualToItself() {

        final var phone = new PhoneNumber(351, 912345678);

        assertEquals(phone, phone);
    }

    @Test
    void ensureHashCodeIsConsistentWithEquals() {

        final var p1 = new PhoneNumber(351, 912345678);
        final var p2 = new PhoneNumber(351, 912345678);

        assertEquals(p1.hashCode(), p2.hashCode());
    }
}