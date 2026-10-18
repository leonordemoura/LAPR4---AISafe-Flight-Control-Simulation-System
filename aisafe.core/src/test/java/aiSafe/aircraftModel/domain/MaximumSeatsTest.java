package aiSafe.aircraftModel.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaximumSeatsTest {

    // =========================================================
    // TEST 1
    // =========================================================

    @Test
    void ensureMaximumSeatsIsCreatedSuccessfullyWithValidData() {

        final var seats = new MaximumSeats(180);

        assertNotNull(seats);
    }

    // =========================================================
    // TEST 2
    // =========================================================

    @Test
    void ensureNegativeMaximumSeatsIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new MaximumSeats(-10);
        });
    }

    // =========================================================
    // TEST 3
    // =========================================================

    @Test
    void ensureZeroMaximumSeatsIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new MaximumSeats(0);
        });
    }

    // =========================================================
    // TEST 4
    // =========================================================

    @Test
    void ensureSeatsGetterReturnsCorrectValue() {

        final var seats = new MaximumSeats(220);

        assertEquals(220, seats.seats());
    }

    // =========================================================
    // TEST 5
    // =========================================================

    @Test
    void ensureEqualsReturnsTrueForSameValues() {

        final var seats1 = new MaximumSeats(150);
        final var seats2 = new MaximumSeats(150);

        assertEquals(seats1, seats2);
    }

    // =========================================================
    // TEST 6
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentValues() {

        final var seats1 = new MaximumSeats(150);
        final var seats2 = new MaximumSeats(300);

        assertNotEquals(seats1, seats2);
    }

    // =========================================================
    // TEST 7
    // =========================================================

    @Test
    void ensureHashCodeIsEqualForEqualObjects() {

        final var seats1 = new MaximumSeats(180);
        final var seats2 = new MaximumSeats(180);

        assertEquals(seats1.hashCode(), seats2.hashCode());
    }

    // =========================================================
    // TEST 8
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentObjectType() {

        final var seats = new MaximumSeats(180);

        assertNotEquals(seats, "not maximum seats");
    }
}