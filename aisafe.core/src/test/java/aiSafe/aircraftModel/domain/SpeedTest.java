package aiSafe.aircraftModel.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpeedTest {

    // =========================================================
    // TEST 1
    // =========================================================

    @Test
    void ensureSpeedIsCreatedSuccessfullyWithValidData() {

        final var speed = new Speed(850, SpeedUnit.KMH);

        assertNotNull(speed);
    }

    // =========================================================
    // TEST 2
    // =========================================================

    @Test
    void ensureNegativeSpeedIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Speed(-1, SpeedUnit.KMH);
        });
    }

    // =========================================================
    // TEST 3
    // =========================================================

    @Test
    void ensureNullUnitIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Speed(850, null);
        });
    }

    // =========================================================
    // TEST 4
    // =========================================================

    @Test
    void ensureZeroSpeedIsAccepted() {

        final var speed = new Speed(0, SpeedUnit.KMH);

        assertEquals(0, speed.value());
    }

    // =========================================================
    // TEST 5
    // =========================================================

    @Test
    void ensureValueGetterReturnsCorrectValue() {

        final var speed = new Speed(900, SpeedUnit.KMH);

        assertEquals(900, speed.value());
    }

    // =========================================================
    // TEST 6
    // =========================================================

    @Test
    void ensureUnitGetterReturnsCorrectValue() {

        final var speed = new Speed(900, SpeedUnit.KMH);

        assertEquals(SpeedUnit.KMH, speed.unit());
    }

    // =========================================================
    // TEST 7
    // =========================================================

    @Test
    void ensureEqualsReturnsTrueForSameValues() {

        final var speed1 = new Speed(850, SpeedUnit.KMH);
        final var speed2 = new Speed(850, SpeedUnit.KMH);

        assertEquals(speed1, speed2);
    }

    // =========================================================
    // TEST 8
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentValues() {

        final var speed1 = new Speed(850, SpeedUnit.KMH);
        final var speed2 = new Speed(950, SpeedUnit.KMH);

        assertNotEquals(speed1, speed2);
    }

    // =========================================================
    // TEST 9
    // =========================================================

    @Test
    void ensureHashCodeIsEqualForEqualObjects() {

        final var speed1 = new Speed(850, SpeedUnit.KMH);
        final var speed2 = new Speed(850, SpeedUnit.KMH);

        assertEquals(speed1.hashCode(), speed2.hashCode());
    }

    // =========================================================
    // TEST 10
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentObjectType() {

        final var speed = new Speed(850, SpeedUnit.KMH);

        assertNotEquals(speed, "not a speed");
    }
}