package aiSafe.aircraft.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CabinConfigurationTest {

    // =========================================================
    // TEST 1 - SUCCESS
    // =========================================================

    @Test
    void ensureCabinConfigurationIsCreatedSuccessfully() {

        final var cabin = new CabinConfiguration(150, 30, 10);

        assertNotNull(cabin);
        assertEquals(150, cabin.economySeats());
        assertEquals(30, cabin.businessSeats());
        assertEquals(10, cabin.firstClassSeats());
    }

    // =========================================================
    // TEST 2 - TOTAL SEATS
    // =========================================================

    @Test
    void ensureTotalSeatsIsTheSumOfAllClasses() {

        final var cabin = new CabinConfiguration(150, 30, 10);

        assertEquals(190, cabin.totalSeats());
    }

    // =========================================================
    // TEST 3 - ONLY ECONOMY SEATS
    // =========================================================

    @Test
    void ensureCabinWithOnlyEconomySeatsIsAllowed() {

        final var cabin = new CabinConfiguration(200, 0, 0);

        assertEquals(200, cabin.totalSeats());
    }

    // =========================================================
    // TEST 4 - ONLY BUSINESS SEATS
    // =========================================================

    @Test
    void ensureCabinWithOnlyBusinessSeatsIsAllowed() {

        final var cabin = new CabinConfiguration(0, 50, 0);

        assertEquals(50, cabin.totalSeats());
    }

    // =========================================================
    // TEST 5 - ONLY FIRST CLASS SEATS
    // =========================================================

    @Test
    void ensureCabinWithOnlyFirstClassSeatsIsAllowed() {

        final var cabin = new CabinConfiguration(0, 0, 20);

        assertEquals(20, cabin.totalSeats());
    }

    // =========================================================
    // TEST 6 - NEGATIVE ECONOMY SEATS
    // =========================================================

    @Test
    void ensureNegativeEconomySeatsAreRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new CabinConfiguration(-1, 30, 10);
        });
    }

    // =========================================================
    // TEST 7 - NEGATIVE BUSINESS SEATS
    // =========================================================

    @Test
    void ensureNegativeBusinessSeatsAreRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new CabinConfiguration(150, -1, 10);
        });
    }

    // =========================================================
    // TEST 8 - NEGATIVE FIRST CLASS SEATS
    // =========================================================

    @Test
    void ensureNegativeFirstClassSeatsAreRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new CabinConfiguration(150, 30, -1);
        });
    }

    // =========================================================
    // TEST 9 - ALL ZERO SEATS
    // =========================================================

    @Test
    void ensureAllZeroSeatsAreRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new CabinConfiguration(0, 0, 0);
        });
    }

    // =========================================================
    // TEST 10 - EQUALS TRUE
    // =========================================================

    @Test
    void ensureEqualsReturnsTrueForSameConfiguration() {

        final var cabin1 = new CabinConfiguration(150, 30, 10);
        final var cabin2 = new CabinConfiguration(150, 30, 10);

        assertEquals(cabin1, cabin2);
    }

    // =========================================================
    // TEST 11 - EQUALS FALSE
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentConfigurations() {

        final var cabin1 = new CabinConfiguration(150, 30, 10);
        final var cabin2 = new CabinConfiguration(100, 40, 20);

        assertNotEquals(cabin1, cabin2);
    }

    // =========================================================
    // TEST 12 - EQUALS FALSE
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentType() {

        final var cabin = new CabinConfiguration(150, 30, 10);

        assertNotEquals(cabin, "150-30-10");
    }

    // =========================================================
    // TEST 13 - HASHCODE
    // =========================================================

    @Test
    void ensureHashCodeIsConsistentWithEquals() {

        final var cabin1 = new CabinConfiguration(150, 30, 10);
        final var cabin2 = new CabinConfiguration(150, 30, 10);

        assertEquals(cabin1.hashCode(), cabin2.hashCode());
    }
}