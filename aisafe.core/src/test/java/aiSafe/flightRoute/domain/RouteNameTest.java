package aiSafe.flightRoute.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouteNameTest {

    @Test
    void ensureRouteNameMustFollowFormat() {
        // Arrange
        String invalidRouteName = "INVALID_NAME";
        int invalidRouteNumber = 12345;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> RouteName.valueOf(invalidRouteName, invalidRouteNumber));
    }

    @Test
    void valueOf_creates_route_name_with_valid_prefix_and_number() {
        // Arrange & Act
        final RouteName name = RouteName.valueOf("TP", 123);

        // Assert
        assertEquals("TP", name.value());
        assertEquals(123, name.routeNumber());
    }

    @Test
    void valueOf_accepts_boundary_route_number_1() {
        final RouteName name = RouteName.valueOf("TP", 1);
        assertEquals(1, name.routeNumber());
    }

    @Test
    void valueOf_accepts_boundary_route_number_9999() {
        final RouteName name = RouteName.valueOf("TP", 9999);
        assertEquals(9999, name.routeNumber());
    }

    @Test
    void valueOf_accepts_any_two_uppercase_letter_prefix() {
        // All letter combinations should be valid
        assertDoesNotThrow(() -> RouteName.valueOf("AA", 1));
        assertDoesNotThrow(() -> RouteName.valueOf("ZZ", 9999));
        assertDoesNotThrow(() -> RouteName.valueOf("TP", 500));
    }

    // =========================================================================
    // Construction — prefix validations
    // =========================================================================

    @Test
    void valueOf_throws_when_prefix_is_null() {
        assertThrows(IllegalArgumentException.class, () -> RouteName.valueOf(null, 1));
    }

    @Test
    void valueOf_throws_when_prefix_is_empty() {
        assertThrows(IllegalArgumentException.class, () -> RouteName.valueOf("", 1));
    }

    @Test
    void valueOf_throws_when_prefix_is_blank() {
        assertThrows(IllegalArgumentException.class, () -> RouteName.valueOf("  ", 1));
    }

    @Test
    void valueOf_throws_when_prefix_has_only_one_letter() {
        assertThrows(IllegalArgumentException.class, () -> RouteName.valueOf("T", 123));
    }

    @Test
    void valueOf_throws_when_prefix_has_three_letters() {
        assertThrows(IllegalArgumentException.class, () -> RouteName.valueOf("TAP", 123));
    }

    @Test
    void valueOf_throws_when_prefix_contains_lowercase_letters() {
        assertThrows(IllegalArgumentException.class, () -> RouteName.valueOf("tp", 123));
    }

    @Test
    void valueOf_throws_when_prefix_contains_digits() {
        assertThrows(IllegalArgumentException.class, () -> RouteName.valueOf("T1", 123));
    }

    @Test
    void valueOf_throws_when_prefix_contains_special_characters() {
        assertThrows(IllegalArgumentException.class, () -> RouteName.valueOf("T!", 123));
    }

    // =========================================================================
    // Construction — route number validations
    // =========================================================================

    @Test
    void valueOf_throws_when_route_number_is_zero() {
        assertThrows(IllegalArgumentException.class, () -> RouteName.valueOf("TP", 0));
    }

    @Test
    void valueOf_throws_when_route_number_is_negative() {
        assertThrows(IllegalArgumentException.class, () -> RouteName.valueOf("TP", -1));
    }

    @Test
    void valueOf_throws_when_route_number_exceeds_9999() {
        assertThrows(IllegalArgumentException.class, () -> RouteName.valueOf("TP", 10000));
    }

    // =========================================================================
    // Accessors
    // =========================================================================

    @Test
    void value_returns_the_prefix() {
        final RouteName name = RouteName.valueOf("TP", 456);
        assertEquals("TP", name.value());
    }

    @Test
    void routeNumber_returns_the_number() {
        final RouteName name = RouteName.valueOf("TP", 456);
        assertEquals(456, name.routeNumber());
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    void toString_concatenates_prefix_and_number() {
        final RouteName name = RouteName.valueOf("TP", 123);
        assertEquals("TP123", name.toString());
    }

    @Test
    void toString_does_not_pad_route_number_with_leading_zeros() {
        final RouteName name = RouteName.valueOf("TP", 1);
        assertEquals("TP1", name.toString());
    }

    @Test
    void toString_represents_maximum_route_number_correctly() {
        final RouteName name = RouteName.valueOf("TP", 9999);
        assertEquals("TP9999", name.toString());
    }

    // =========================================================================
    // equals & hashCode
    // =========================================================================

    @Test
    void equals_returns_true_for_same_prefix_and_number() {
        final RouteName a = RouteName.valueOf("TP", 123);
        final RouteName b = RouteName.valueOf("TP", 123);
        assertEquals(a, b);
    }

    @Test
    void equals_returns_false_when_prefix_differs() {
        final RouteName a = RouteName.valueOf("TP", 123);
        final RouteName b = RouteName.valueOf("AB", 123);
        assertNotEquals(a, b);
    }

    @Test
    void equals_returns_false_when_number_differs() {
        final RouteName a = RouteName.valueOf("TP", 123);
        final RouteName b = RouteName.valueOf("TP", 456);
        assertNotEquals(a, b);
    }

    @Test
    void equals_returns_false_for_null() {
        final RouteName a = RouteName.valueOf("TP", 123);
        assertNotEquals(null, a);
    }

    @Test
    void equals_returns_false_for_different_type() {
        final RouteName a = RouteName.valueOf("TP", 123);
        assertNotEquals("TP123", a);
    }

    @Test
    void equals_is_reflexive() {
        final RouteName a = RouteName.valueOf("TP", 123);
        assertEquals(a, a);
    }

    @Test
    void equals_is_symmetric() {
        final RouteName a = RouteName.valueOf("TP", 123);
        final RouteName b = RouteName.valueOf("TP", 123);
        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    void hashCode_is_equal_for_equal_route_names() {
        final RouteName a = RouteName.valueOf("TP", 123);
        final RouteName b = RouteName.valueOf("TP", 123);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCode_differs_when_prefix_differs() {
        final RouteName a = RouteName.valueOf("TP", 123);
        final RouteName b = RouteName.valueOf("AB", 123);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCode_differs_when_number_differs() {
        final RouteName a = RouteName.valueOf("TP", 1);
        final RouteName b = RouteName.valueOf("TP", 2);
        assertNotEquals(a.hashCode(), b.hashCode());
    }
}