package aiSafe.flight.dsl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlightDslErrorTest {

    @Test
    void format_includes_type_label_for_each_error_kind() {
        assertTrue(new FlightDslError(FlightDslErrorType.LEXICAL, "lex", 1, 2).format().startsWith("Lexical Error"));
        assertTrue(new FlightDslError(FlightDslErrorType.SYNTAX, "syn", 3, 4).format().startsWith("Syntax Error"));
        assertTrue(new FlightDslError(FlightDslErrorType.SEMANTIC, "sem", 5, 6).format().startsWith("Semantic Error"));
    }

    @Test
    void accessors_equals_hashCode_and_toString() {
        final FlightDslError a = new FlightDslError(FlightDslErrorType.SYNTAX, "msg", 2, 3);
        final FlightDslError b = new FlightDslError(FlightDslErrorType.SYNTAX, "msg", 2, 3);

        assertEquals(FlightDslErrorType.SYNTAX, a.type());
        assertEquals("msg", a.message());
        assertEquals(2, a.line());
        assertEquals(3, a.column());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a.format(), a.toString());
        assertNotEquals(a, new FlightDslError(FlightDslErrorType.LEXICAL, "msg", 2, 3));
    }
}
