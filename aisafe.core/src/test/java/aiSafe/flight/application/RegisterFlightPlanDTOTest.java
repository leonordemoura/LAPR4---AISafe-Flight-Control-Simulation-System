package aiSafe.flight.application;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegisterFlightPlanDTOTest {

    @Test
    void flightDesignatorText_without_suffix() {
        final RegisterFlightPlanDTO dto = RegisterFlightPlanTestFixtures.dtoWithDslContent("x");
        assertEquals("TP123", dto.flightDesignatorText());
    }

    @Test
    void flightDesignatorText_treats_blank_suffix_as_empty() {
        final LocalDateTime dep = LocalDateTime.now().plusDays(1);
        final RegisterFlightPlanDTO dto = new RegisterFlightPlanDTO(
                "TP", 123, "   ",
                "P-001",
                dep, dep.plusHours(1), 1000, "kg",
                null, List.of(), null
        );
        assertEquals("TP123", dto.flightDesignatorText());
    }

    @Test
    void flightDesignatorText_with_suffix() {
        final LocalDateTime dep = LocalDateTime.now().plusDays(1);
        final RegisterFlightPlanDTO dto = new RegisterFlightPlanDTO(
                "TP", 123, "A",
                "P-001",
                dep, dep.plusHours(1), 1000, "kg",
                "dsl", List.of(), null
        );
        assertEquals("TP123A", dto.flightDesignatorText());
    }

    @Test
    void legs_returns_unmodifiable_copy() {
        final RegisterFlightPlanDTO dto = RegisterFlightPlanTestFixtures.dtoWithLegs();
        assertEquals(1, dto.legs().size());
        assertThrows(UnsupportedOperationException.class,
                () -> dto.legs().add(RegisterFlightPlanTestFixtures.sampleLeg()));
    }

    @Test
    void null_legs_becomes_empty_list() {
        final LocalDateTime dep = LocalDateTime.now().plusDays(1);
        final RegisterFlightPlanDTO dto = new RegisterFlightPlanDTO(
                "TP", 123, null,
                "P-001",
                dep, dep.plusHours(1), 1000, "kg",
                null, null, null
        );
        assertTrue(dto.legs().isEmpty());
    }
}
