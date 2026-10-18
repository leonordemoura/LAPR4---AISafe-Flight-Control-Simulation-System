package aiSafe.flight.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DslSourceTest {

    @Test
    void rejects_null_content() {
        assertThrows(IllegalArgumentException.class, () -> new DslSource(null));
    }

    @Test
    void rejects_blank_content() {
        assertThrows(IllegalArgumentException.class, () -> new DslSource("   "));
    }

    @Test
    void trims_content() {
        final DslSource dsl = new DslSource("  flight TP123 { }  ");
        assertEquals("flight TP123 { }", dsl.content());
    }

    @Test
    void equals_and_hashCode() {
        final DslSource a = new DslSource("flight TP123 { }");
        final DslSource b = new DslSource("flight TP123 { }");
        final DslSource c = new DslSource("other");

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a, "not-a-dsl-source");
    }

    @Test
    void orm_default_constructor_can_be_invoked() throws Exception {
        final var ctor = DslSource.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        final DslSource empty = (DslSource) ctor.newInstance();
        assertNotNull(empty);
    }
}
