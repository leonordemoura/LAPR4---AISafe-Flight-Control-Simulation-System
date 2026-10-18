package aiSafe.flight.dsl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlightDslStructureListenerTest {

    @Test
    void counts_legs_and_segments() {
        final FlightDslStructureListener listener = new FlightDslStructureListener();

        // Listener ignores ctx; null is enough to exercise counters (ANTLR inner classes cannot be mocked).
        listener.enterLeg(null);
        listener.enterSegment(null);
        listener.enterSegment(null);

        assertEquals(1, listener.legCount());
        assertEquals(2, listener.segmentCount());
    }
}
