package aiSafe.flight.dsl;

import aiSafe.flight.domain.FlightType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FlightDslValidatorTest {

    @Test
    void ensureValidFlightPlanFromReadmeIsAccepted() throws IOException {
        final String source = readResource("flight-plans/valid-single-leg.flight");

        final FlightDslValidationResult result = FlightDslValidator.validate(source);

        assertTrue(result.isValid(), () -> result.errors().toString());
        assertTrue(result.model().isPresent());
        assertEquals("TP123", result.model().get().identifier());
        assertEquals(FlightType.REGULAR, result.model().get().flightType());
        assertEquals(1, result.model().get().legs().size());
        assertEquals("OPO", result.model().get().legs().getFirst().departure().airportCode());
        assertEquals("LIS", result.model().get().legs().getFirst().arrival().airportCode());
    }

    @Test
    void ensureValidTwoLegFlightPlanIsAccepted() throws IOException {
        final String source = readResource("flight-plans/valid-two-legs.flight");

        final FlightDslValidationResult result = FlightDslValidator.validate(source);

        assertTrue(result.isValid(), () -> result.formattedErrors().toString());
        assertEquals(2, result.model().orElseThrow().legs().size());
        assertEquals("LIS", result.model().get().legs().get(0).arrival().airportCode());
        assertEquals("LIS", result.model().get().legs().get(1).departure().airportCode());
        assertEquals("MAD", result.model().get().legs().get(1).arrival().airportCode());
    }

    @Test
    void ensureValidateFileReadsUtf8Content(@TempDir final Path tempDir) throws IOException {
        final Path file = tempDir.resolve("plan.flight");
        Files.writeString(file, readResource("flight-plans/valid-single-leg.flight"), StandardCharsets.UTF_8);

        final FlightDslValidationResult result = FlightDslValidator.validateFile(file);

        assertTrue(result.isValid());
        assertEquals("TP123", result.model().orElseThrow().identifier());
    }

    @Test
    void ensureNullInputProducesLexicalError() {
        final FlightDslValidationResult result = FlightDslValidator.validate(null);
        assertFalse(result.isValid());
        assertEquals(FlightDslErrorType.LEXICAL, result.errors().getFirst().type());
    }

    @Test
    void ensureComplexPlanWithMultipleLegsSegmentsAndNegativeCoordinatesIsAccepted() {
        final String source = """
        flight TP300 {
            type CHARTER
            leg {
                departure OPO 2026-05-11 08:00
                arrival MAD 2026-05-11 09:30
                route { origin OPO destination MAD }
                segment {
                    start (-41.0, -8.0)
                    end (40.0, -4.0)
                    altitude 10000.5 m
                    width 10 km
                    wind 0 deg 0 kt
                }
                segment {
                    start (40.0, -4.0)
                    end (39.5, -3.5)
                    altitude 11000 m
                    width 10 km
                    wind 0 deg 0 kt
                }
                fuel 4000.5 kg
            }
            leg {
                departure MAD 2026-05-11 10:00
                arrival LIS 2026-05-11 11:00
                route { origin MAD destination LIS }
                segment {
                    start (39.5, -3.5)
                    end (38.774, -9.134)
                    altitude 12000 m
                    width 10 km
                    wind 0 deg 0 kt
                }
                fuel 2500 l
            }
        }
        """;
        final FlightDslValidationResult result = FlightDslValidator.validate(source);
        assertTrue(result.isValid(), () -> result.errors().toString());
        assertEquals(2, result.model().orElseThrow().legs().size());
        assertEquals(FlightType.CHARTER, result.model().orElseThrow().flightType());
        assertEquals(2, result.model().orElseThrow().legs().getFirst().segments().size());
        assertEquals("l", result.model().orElseThrow().legs().get(1).fuel().unit());
    }

    @Test
    void ensureEmptyInputProducesLexicalError() {
        final FlightDslValidationResult result = FlightDslValidator.validate("   ");

        assertFalse(result.isValid());
        assertEquals(FlightDslErrorType.LEXICAL, result.errors().getFirst().type());
        assertTrue(result.errors().getFirst().message().contains("empty"));
        assertEquals(1, result.formattedErrors().size());
    }

    @Test
    void ensureSyntaxErrorIncludesLineAndColumn() {
        final String source = """
        flight TP123 {
            type REGULAR
            leg {
                departure OPO 2026-05-11 08:00
                arrival LIS 2026-05-11 09:00
                route { origin OPO destination LIS }
                segment {
                    start (1, 2)
                    end (3, 4)
                    altitude 100 m
                    width 10 km
                    wind 0 deg 0 kt
                }
                fuel 10 kg
            }
        """;

        final FlightDslValidationResult result = FlightDslValidator.validate(source);

        assertFalse(result.isValid());
        assertEquals(FlightDslErrorType.SYNTAX, result.errors().getFirst().type());
        assertTrue(result.errors().getFirst().line() > 0);
        assertTrue(result.errors().getFirst().column() > 0);
        assertTrue(result.errors().getFirst().format().startsWith("Syntax Error"));
    }

    @Test
    void ensureLexicalErrorForLowercaseAirportCode() {
        final String source = """
        flight TP123 {
            type REGULAR
            leg {
                departure opo 2026-05-11 08:00
                arrival LIS 2026-05-11 09:00
                route { origin OPO destination LIS }
                segment {
                    start (1.0, 2.0)
                    end (3.0, 4.0)
                    altitude 100 m
                    width 10 km
                    wind 0 deg 0 kt
                }
                fuel 10 kg
            }
        }
        """;

        final FlightDslValidationResult result = FlightDslValidator.validate(source);

        assertFalse(result.isValid());
        assertEquals(FlightDslErrorType.LEXICAL, result.errors().getFirst().type());
    }

    @Test
    void ensureSemanticErrorsAreCollectedForInvalidRouteAndFuel() {
        final String source = """
        flight TP999 {
            type CHARTER
            leg {
                departure OPO 2026-05-11 08:00
                arrival LIS 2026-05-11 09:00
                route {
                    origin LIS
                    destination OPO
                }
                segment {
                    start (41.0, -8.0)
                    end (41.0, -8.0)
                    altitude -100 m
                    width 10 km
                    wind 0 deg 0 kt
                }
                fuel 0 kg
            }
        }
        """;

        final FlightDslValidationResult result = FlightDslValidator.validate(source);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().allMatch(e -> e.type() == FlightDslErrorType.SEMANTIC));
        assertTrue(result.errors().size() >= 3);
        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("Route origin")));
        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("coordinates")));
        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("Fuel")));
        assertEquals(result.errors().size(), result.formattedErrors().size());
    }

    @Test
    void ensureArrivalBeforeDepartureOnSameLegProducesSemanticError() {
        final String source = """
        flight TP100 {
            type CHARTER
            leg {
                departure OPO 2026-05-11 10:00
                arrival LIS 2026-05-11 09:00
                route { origin OPO destination LIS }
                segment {
                    start (1.0, 2.0)
                    end (3.0, 4.0)
                    altitude 1000 m
                    width 10 km
                    wind 0 deg 0 kt
                }
                fuel 50 kg
            }
        }
        """;

        final FlightDslValidationResult result = FlightDslValidator.validate(source);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream()
                .anyMatch(e -> e.message().contains("Arrival time must be after departure")));
    }

    @Test
    void ensureInvalidDateProducesSemanticError() {
        final String source = """
        flight TP100 {
            type REGULAR
            leg {
                departure OPO 2026-13-40 08:00
                arrival LIS 2026-05-11 09:00
                route { origin OPO destination LIS }
                segment {
                    start (1.0, 2.0)
                    end (3.0, 4.0)
                    altitude 1000 m
                    width 10 km
                    wind 0 deg 0 kt
                }
                fuel 50 kg
            }
        }
        """;

        final FlightDslValidationResult result = FlightDslValidator.validate(source);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.message().contains("Invalid date or time")));
    }

    @Test
    void ensureRepeatedAirportVisitProducesSemanticError() {
        final String source = """
        flight TP100 {
            type REGULAR
            leg {
                departure OPO 2026-05-11 08:00
                arrival LIS 2026-05-11 09:00
                route { origin OPO destination LIS }
                segment {
                    start (1.0, 2.0)
                    end (3.0, 4.0)
                    altitude 1000 m
                    width 10 km
                    wind 0 deg 0 kt
                }
                fuel 50 kg
            }
            leg {
                departure LIS 2026-05-11 10:00
                arrival OPO 2026-05-11 12:00
                route { origin LIS destination OPO }
                segment {
                    start (3.0, 4.0)
                    end (5.0, 6.0)
                    altitude 1000 m
                    width 10 km
                    wind 0 deg 0 kt
                }
                fuel 50 kg
            }
        }
        """;

        final FlightDslValidationResult result = FlightDslValidator.validate(source);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream()
                .anyMatch(e -> e.message().contains("cannot be visited twice")));
    }

    @Test
    void ensureLegAirportMismatchProducesSemanticError() {
        final String source = """
        flight TP100 {
            type REGULAR
            leg {
                departure OPO 2026-05-11 08:00
                arrival LIS 2026-05-11 09:00
                route { origin OPO destination LIS }
                segment {
                    start (1.0, 2.0)
                    end (3.0, 4.0)
                    altitude 1000 m
                    width 10 km
                    wind 0 deg 0 kt
                }
                fuel 50 kg
            }
            leg {
                departure MAD 2026-05-11 10:00
                arrival LPPT 2026-05-11 12:00
                route { origin MAD destination LPPT }
                segment {
                    start (3.0, 4.0)
                    end (5.0, 6.0)
                    altitude 1000 m
                    width 10 km
                    wind 0 deg 0 kt
                }
                fuel 50 kg
            }
        }
        """;

        final FlightDslValidationResult result = FlightDslValidator.validate(source);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream()
                .anyMatch(e -> e.message().contains("must match departure airport of leg 2")));
    }

    @Test
    void ensureRegularAndCharterTypesAreAccepted() {
        assertTrue(FlightDslValidator.validate(validPlanWithType("REGULAR")).isValid());
        assertTrue(FlightDslValidator.validate(validPlanWithType("CHARTER")).isValid());
        assertTrue(FlightDslValidator.validate(validPlanWithType("regular")).isValid());
    }

    @Test
    void ensureLegacyPassengerCargoPrivateTypesAreRejected() {
        assertFalse(FlightDslValidator.validate(validPlanWithType("PASSENGER")).isValid());
        assertFalse(FlightDslValidator.validate(validPlanWithType("CARGO")).isValid());
        assertFalse(FlightDslValidator.validate(validPlanWithType("PRIVATE")).isValid());
    }

    @Test
    void ensureLiterFuelUnitAndFourLetterAirportAreAccepted() {
        final String source = """
        flight TP200 {
            type REGULAR
            leg {
                departure LEBL 2026-06-01 10:00
                arrival LPPT 2026-06-01 11:00
                route { origin LEBL destination LPPT }
                segment {
                    start (41.0, -8.0)
                    end (38.0, -9.0)
                    altitude 1000 m
                    width 10 km
                    wind 0 deg 0 kt
                }
                segment {
                    start (38.0, -9.0)
                    end (37.0, -8.5)
                    altitude 1100 m
                    width 10 km
                    wind 0 deg 0 kt
                }
                fuel 100 l
            }
        }
        """;
        final FlightDslValidationResult result = FlightDslValidator.validate(source);
        assertTrue(result.isValid(), () -> result.errors().toString());
        assertEquals("l", result.model().orElseThrow().legs().getFirst().fuel().unit());
        assertEquals(2, result.model().orElseThrow().legs().getFirst().segments().size());
    }

    @Test
    void ensureValidateWithoutSemantic_skips_semantic_rules() {
        final String source = semanticallyInvalidButParsableDsl();
        final FlightDslValidationResult result = FlightDslValidator.validateWithoutSemantic(source);
        assertTrue(result.isValid(), () -> result.errors().toString());
        assertTrue(result.model().isPresent());
    }

    @Test
    void ensureValidateWithoutSemantic_rejects_blank_input() {
        final FlightDslValidationResult result = FlightDslValidator.validateWithoutSemantic("  ");
        assertFalse(result.isValid());
        assertEquals(FlightDslErrorType.LEXICAL, result.errors().getFirst().type());
    }

    @Test
    void ensureLexerErrorForInvalidToken() {
        final FlightDslValidationResult result = FlightDslValidator.validate(
                "flight TP123 { type REGULAR leg { departure OPO @invalid } }");
        assertFalse(result.isValid());
        assertFalse(result.errors().isEmpty());
    }

    @Test
    void ensureCaseInsensitiveKeywordsAreAccepted() {
        final String source = """
        FLIGHT TP001 {
            type REGULAR
            LEG {
                DEPARTURE OPO 2026-05-11 08:00
                ARRIVAL LIS 2026-05-11 09:00
                ROUTE { ORIGIN OPO DESTINATION LIS }
                SEGMENT {
                    START (1.0, 2.0)
                    END (3.0, 4.0)
                    ALTITUDE 1000 m
                    WIDTH 10 km
                    WIND 0 deg 0 kt
                }
                FUEL 100 kg
            }
        }
        """;

        final FlightDslValidationResult result = FlightDslValidator.validate(source);

        assertTrue(result.isValid(), () -> result.errors().toString());
    }

    private static String validPlanWithType(final String type) {
        return """
        flight TP150 {
            type %s
            leg {
                departure OPO 2026-05-11 08:00
                arrival LIS 2026-05-11 09:00
                route { origin OPO destination LIS }
                segment {
                    start (1.0, 2.0)
                    end (3.0, 4.0)
                    altitude 1000 m
                    width 10 km
                    wind 0 deg 0 kt
                }
                fuel 50 kg
            }
        }
        """.formatted(type);
    }

    private static String semanticallyInvalidButParsableDsl() {
        return """
        flight TP123 {
            type CHARTER
            leg {
                departure OPO 2026-05-11 08:00
                arrival LIS 2026-05-11 09:00
                route {
                    origin LIS
                    destination OPO
                }
                segment {
                    start (41.0, -8.0)
                    end (41.0, -8.0)
                    altitude -100 m
                    width 10 km
                    wind 0 deg 0 kt
                }
                fuel 0 kg
            }
        }
        """;
    }

    private static String readResource(final String resourcePath) throws IOException {
        final Path path = Path.of("src/test/resources", resourcePath);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
