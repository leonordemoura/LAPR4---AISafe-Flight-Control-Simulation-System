package aiSafe.costumermanagement.domain;

import aiSafe.usermanagement.domain.Email;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MembershipIdentityTest {

    private static Membership membership(final String positionLiteral) {
        return new Membership(
                new Email("fc@company.com"),
                Position.valueOf(positionLiteral),
                MemberType.FCO);
    }

    @Test
    void equals_requires_same_email_position_and_member_type() {
        final Membership a = membership("Chief Pilot");
        final Membership same = membership("Chief Pilot");
        assertEquals(a, same);
        assertEquals(a.hashCode(), same.hashCode());
    }

    @Test
    void equals_false_when_member_type_differs_even_if_email_equals() {
        final Email email = new Email("p@company.com");
        final Membership fco = new Membership(email, Position.valueOf("Chief Pilot"), MemberType.FCO);
        final Membership pilot = new Membership(email, Position.valueOf("Chief Pilot"), MemberType.PILOT);
        assertNotEquals(fco, pilot);
    }

    @Test
    void equals_false_when_position_differs_for_same_email() {
        assertNotEquals(membership("Chief Pilot"), membership("Chief Executive Officer"));
    }

    @Test
    void orm_constructor_is_empty_and_exposed_for_coverage() throws Exception {
        final Constructor<Membership> ctor = Membership.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        final Membership m = ctor.newInstance();
        assertFalse(m.equals(membership("Chief Pilot")));
    }
}
