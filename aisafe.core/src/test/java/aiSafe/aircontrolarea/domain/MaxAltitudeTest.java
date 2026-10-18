package aiSafe.aircontrolarea.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MaxAltitudeTest {
    @Test
    void ensureMaxAltitudeCannotBeNegative() {
        assertThrows(IllegalArgumentException.class, () -> new MaxAltitude(-1));
    }

    @Test
    void ensureMaxAltitudeCannotBeZero() {
        assertThrows(IllegalArgumentException.class, () -> new MaxAltitude(0));
    }

    @Test
    void ensureMaxAltitudeIsCreatedSuccessfully() {
        final var maxAltitude = new MaxAltitude(14000);
        assertEquals(14000, maxAltitude.value());
    }

    @Test
    void ensureEqualsHashCodeAndOrmConstructor() throws Exception {
        final MaxAltitude a = new MaxAltitude(100);
        final MaxAltitude b = new MaxAltitude(100);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new MaxAltitude(101));
        assertFalse(a.equals(null));

        final var ctor = MaxAltitude.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }
}
