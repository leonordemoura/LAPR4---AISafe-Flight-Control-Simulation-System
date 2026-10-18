package aiSafe.flight.application;

import aiSafe.flight.domain.DslSource;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Shared fixtures for US080 register flight plan tests.
 */
final class RegisterFlightPlanTestFixtures {

    private RegisterFlightPlanTestFixtures() {}

    static RegisterFlightPlanSegmentInput sampleSegment() {
        return new RegisterFlightPlanSegmentInput(41.242, -8.678, 38.774, -9.134, 12000, 10, 180, 15, "kt");
    }

    static RegisterFlightPlanLegInput sampleLeg() {
        return new RegisterFlightPlanLegInput(
                "OPO", "2026-06-15", "10:00",
                "LIS", "2026-06-15", "11:00",
                "OPO", "LIS",
                5000, "kg",
                List.of(sampleSegment())
        );
    }

    private static LocalDateTime sampleDeparture() {
        return LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);
    }

    private static LocalDateTime sampleArrival() {
        return sampleDeparture().plusHours(1);
    }

    static RegisterFlightPlanDTO dtoWithDslContent(final String dslContent) {
        return new RegisterFlightPlanDTO(
                "TP",
                123,
                null,
                "P-001",
                sampleDeparture(),
                sampleArrival(),
                1500,
                "kg",
                dslContent,
                List.of(),
                null
        );
    }

    static RegisterFlightPlanDTO dtoWithLegs() {
        return new RegisterFlightPlanDTO(
                "TP",
                123,
                null,
                "P-001",
                sampleDeparture(),
                sampleArrival(),
                1500,
                "kg",
                null,
                List.of(sampleLeg()),
                "REGULAR"
        );
    }

    static RegisterFlightPlanDTO dtoWithLegsDefaultFlightType() {
        return new RegisterFlightPlanDTO(
                "TP",
                123,
                null,
                "P-001",
                sampleDeparture(),
                sampleArrival(),
                1500,
                "kg",
                "   ",
                List.of(sampleLeg()),
                "   "
        );
    }

    static DslSource sampleDslSource() {
        return new DslSource(sampleDslText());
    }

    static String sampleDslText() {
        return """
                flight TP123 {
                    type REGULAR
                    leg {
                        departure OPO 2026-05-11 08:00
                        arrival LIS 2026-05-11 09:00
                        route { origin OPO destination LIS }
                        segment { start (41.0, -8.0) end (38.0, -9.0) altitude 10000 m
                            width 10 km
                            wind 180 deg 15 kt }
                        fuel 1000 kg
                    }
                }
                """;
    }
}
