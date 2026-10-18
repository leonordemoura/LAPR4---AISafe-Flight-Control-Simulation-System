package aiSafe.flight.dsl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises lexer/parser paths with varied valid and invalid DSL fragments.
 */
class FlightDslParserStressTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "flight TP1 { type REGULAR leg { departure OPO 2026-01-01 08:00 arrival LIS 2026-01-01 09:00 route { origin OPO destination LIS } segment { start (1,2) end (3,4) altitude 1 m width 10 km wind 0 deg 0 kt } fuel 1 kg } }",
            "flight TP2 { type CHARTER leg { departure LEBL 2026-01-01 08:00 arrival LPPT 2026-01-01 09:00 route { origin LEBL destination LPPT } segment { start (-1,-2) end (3,4) altitude 10.5 m width 5 km wind 180 deg 10 mps } fuel 2 l } }",
            "flight TP3 { type CHARTER leg { departure OPO 2026-01-01 08:00 arrival LIS 2026-01-01 09:00 route { origin OPO destination LIS } segment { start (1,2) end (3,4) altitude 1 m width 10 km wind 0 deg 0 kt } segment { start (3,4) end (5,6) altitude 2 m width 12 km wind 90 deg 5 kmh } fuel 3 kg } }"
    })
    void validFragmentsParse(final String source) {
        assertTrue(FlightDslValidator.validateWithoutSemantic(source).isValid(),
                () -> FlightDslValidator.validateWithoutSemantic(source).errors().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "flight",
            "flight TP {",
            "flight TP { type REGULAR",
            "flight TP { type REGULAR leg {",
            "flight TP { type REGULAR leg { departure OPO",
            "flight TP1 { type UNKNOWN leg { departure OPO 2026-01-01 08:00 arrival LIS 2026-01-01 09:00 route { origin OPO destination LIS } segment { start (1,2) end (3,4) altitude 1 m width 10 km wind 0 deg 0 kt } fuel 1 kg } }",
            "flight TP1 { type REGULAR leg { departure opo 2026-01-01 08:00 arrival LIS 2026-01-01 09:00 route { origin OPO destination LIS } segment { start (1,2) end (3,4) altitude 1 m width 10 km wind 0 deg 0 kt } fuel 1 kg } }",
            "flight TP1 { type REGULAR leg { departure OPO 2026-01-01 08:00 arrival LIS 2026-01-01 09:00 route { origin OPO destination LIS } segment { start (1,2) end (3,4) altitude 1 m width 10 km wind 0 deg 0 kt } fuel 1 gal } }"
    })
    void invalidFragmentsFail(final String source) {
        assertFalse(FlightDslValidator.validate(source).isValid());
    }
}
