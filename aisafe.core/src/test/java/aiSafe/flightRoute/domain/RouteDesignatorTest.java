package aiSafe.flightRoute.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouteDesignatorTest {

    @Test
    void ensureOriginAndDestinationCannotBeTheSame() {
        // Arrange
        String origin = "OPO";
        String destination = "OPO";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new RouteDesignator(origin, destination));
    }

    @Test
    void ensureOriginAirportCannotBeNull() {
        // Arrange
        String nullOrigin = null;
        String destination = "LIS";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new RouteDesignator(nullOrigin, destination));
    }

    @Test
    void ensureDestinationAirportCannotBeNull() {
        // Arrange
        String origin = "OPO";
        String nullDestination = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new RouteDesignator(origin, nullDestination));
    }


    @Test
    void valueOf_creates_designator_with_uppercase_origin_and_destination() {
        // Arrange & Act
        final RouteDesignator designator = RouteDesignator.valueOf("lis", "opo");

        // Assert
        assertEquals("LIS", designator.origin());
        assertEquals("OPO", designator.destination());
    }

    @Test
    void valueOf_accepts_already_uppercase_codes() {
        // Arrange & Act
        final RouteDesignator designator = RouteDesignator.valueOf("LIS", "OPO");

        // Assert
        assertEquals("LIS", designator.origin());
        assertEquals("OPO", designator.destination());
    }

    @Test
    void valueOf_accepts_mixed_case_codes() {
        // Arrange & Act
        final RouteDesignator designator = RouteDesignator.valueOf("Lis", "Opo");

        // Assert
        assertEquals("LIS", designator.origin());
        assertEquals("OPO", designator.destination());
    }

    // =========================================================================
    // Construction — origin validations
    // =========================================================================

    @Test
    void valueOf_throws_when_origin_is_null() {
        assertThrows(Exception.class, () -> RouteDesignator.valueOf(null, "OPO"));
    }

    @Test
    void valueOf_throws_when_origin_is_empty() {
        assertThrows(IllegalArgumentException.class, () -> RouteDesignator.valueOf("", "OPO"));
    }

    @Test
    void valueOf_throws_when_origin_is_blank() {
        assertThrows(IllegalArgumentException.class, () -> RouteDesignator.valueOf("   ", "OPO"));
    }

    @Test
    void valueOf_throws_when_origin_has_fewer_than_3_letters() {
        assertThrows(IllegalArgumentException.class, () -> RouteDesignator.valueOf("LI", "OPO"));
    }

    @Test
    void valueOf_throws_when_origin_has_more_than_3_letters() {
        assertThrows(IllegalArgumentException.class, () -> RouteDesignator.valueOf("LISB", "OPO"));
    }

    @Test
    void valueOf_throws_when_origin_contains_digits() {
        assertThrows(IllegalArgumentException.class, () -> RouteDesignator.valueOf("L1S", "OPO"));
    }

    @Test
    void valueOf_throws_when_origin_contains_special_characters() {
        assertThrows(IllegalArgumentException.class, () -> RouteDesignator.valueOf("LI!", "OPO"));
    }

    // =========================================================================
    // Construction — destination validations
    // =========================================================================

    @Test
    void valueOf_throws_when_destination_is_null() {
        assertThrows(Exception.class, () -> RouteDesignator.valueOf("LIS", null));
    }

    @Test
    void valueOf_throws_when_destination_is_empty() {
        assertThrows(IllegalArgumentException.class, () -> RouteDesignator.valueOf("LIS", ""));
    }

    @Test
    void valueOf_throws_when_destination_is_blank() {
        assertThrows(IllegalArgumentException.class, () -> RouteDesignator.valueOf("LIS", "   "));
    }

    @Test
    void valueOf_throws_when_destination_has_fewer_than_3_letters() {
        assertThrows(IllegalArgumentException.class, () -> RouteDesignator.valueOf("LIS", "OP"));
    }

    @Test
    void valueOf_throws_when_destination_has_more_than_3_letters() {
        assertThrows(IllegalArgumentException.class, () -> RouteDesignator.valueOf("LIS", "OPOR"));
    }

    @Test
    void valueOf_throws_when_destination_contains_digits() {
        assertThrows(IllegalArgumentException.class, () -> RouteDesignator.valueOf("LIS", "0PO"));
    }

    // =========================================================================
    // Construction — same origin/destination
    // =========================================================================

    @Test
    void valueOf_throws_when_origin_and_destination_are_the_same_uppercase() {
        assertThrows(IllegalArgumentException.class, () -> RouteDesignator.valueOf("LIS", "LIS"));
    }

    @Test
    void valueOf_throws_when_origin_and_destination_are_the_same_case_insensitive() {
        // "lis" and "LIS" are the same airport
        assertThrows(IllegalArgumentException.class, () -> RouteDesignator.valueOf("lis", "LIS"));
    }

    @Test
    void valueOf_throws_when_origin_and_destination_are_the_same_mixed_case() {
        assertThrows(IllegalArgumentException.class, () -> RouteDesignator.valueOf("Lis", "liS"));
    }

    // =========================================================================
    // Accessors
    // =========================================================================

    @Test
    void origin_returns_the_origin_code() {
        final RouteDesignator designator = RouteDesignator.valueOf("LIS", "OPO");
        assertEquals("LIS", designator.origin());
    }

    @Test
    void destination_returns_the_destination_code() {
        final RouteDesignator designator = RouteDesignator.valueOf("LIS", "OPO");
        assertEquals("OPO", designator.destination());
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    void toString_returns_origin_hyphen_destination() {
        final RouteDesignator designator = RouteDesignator.valueOf("LIS", "OPO");
        assertEquals("LIS-OPO", designator.toString());
    }

    @Test
    void toString_always_uses_uppercase_regardless_of_input_case() {
        final RouteDesignator designator = RouteDesignator.valueOf("lis", "opo");
        assertEquals("LIS-OPO", designator.toString());
    }

    // =========================================================================
    // equals & hashCode
    // =========================================================================

    @Test
    void equals_returns_true_for_designators_with_same_origin_and_destination() {
        final RouteDesignator a = RouteDesignator.valueOf("LIS", "OPO");
        final RouteDesignator b = RouteDesignator.valueOf("LIS", "OPO");
        assertEquals(a, b);
    }

    @Test
    void equals_returns_false_when_origin_differs() {
        final RouteDesignator a = RouteDesignator.valueOf("LIS", "OPO");
        final RouteDesignator b = RouteDesignator.valueOf("FAO", "OPO");
        assertNotEquals(a, b);
    }

    @Test
    void equals_returns_false_when_destination_differs() {
        final RouteDesignator a = RouteDesignator.valueOf("LIS", "OPO");
        final RouteDesignator b = RouteDesignator.valueOf("LIS", "FAO");
        assertNotEquals(a, b);
    }

    @Test
    void equals_returns_false_when_origin_and_destination_are_swapped() {
        final RouteDesignator a = RouteDesignator.valueOf("LIS", "OPO");
        final RouteDesignator b = RouteDesignator.valueOf("OPO", "LIS");
        assertNotEquals(a, b);
    }

    @Test
    void equals_returns_false_for_null() {
        final RouteDesignator a = RouteDesignator.valueOf("LIS", "OPO");
        assertNotEquals(null, a);
    }

    @Test
    void equals_returns_false_for_different_type() {
        final RouteDesignator a = RouteDesignator.valueOf("LIS", "OPO");
        assertNotEquals("LIS-OPO", a);
    }

    @Test
    void hashCode_is_equal_for_equal_designators() {
        final RouteDesignator a = RouteDesignator.valueOf("LIS", "OPO");
        final RouteDesignator b = RouteDesignator.valueOf("LIS", "OPO");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCode_differs_for_swapped_origin_destination() {
        final RouteDesignator a = RouteDesignator.valueOf("LIS", "OPO");
        final RouteDesignator b = RouteDesignator.valueOf("OPO", "LIS");
        assertNotEquals(a.hashCode(), b.hashCode());
    }
}
