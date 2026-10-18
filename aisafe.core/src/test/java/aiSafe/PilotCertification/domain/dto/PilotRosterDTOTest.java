package aiSafe.PilotCertification.domain.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class PilotRosterDTOTest {

    @Test
    void ensureDTOStoresAllValuesCorrectly() {

        List<String> models = List.of("A320", "B737");

        PilotRosterDTO dto =
                new PilotRosterDTO("P1", "test@mail.com", true, models);

        assertEquals("P1", dto.pilotId());
        assertEquals("test@mail.com", dto.email());
        assertTrue(dto.active());
        assertEquals(models, dto.certifiedAircraftModels());
    }

    @Test
    void ensureDTOAllowsEmptyCertifiedAircraftList() {

        List<String> models = List.of();

        PilotRosterDTO dto =
                new PilotRosterDTO("P1", "mail@test.com", true, models);

        assertNotNull(dto.certifiedAircraftModels());
        assertTrue(dto.certifiedAircraftModels().isEmpty());
    }

    @Test
    void ensureDTOAcceptsInactivePilot() {

        PilotRosterDTO dto =
                new PilotRosterDTO("P1", "mail@test.com", false, List.of());

        assertFalse(dto.active());
    }

    @Test
    void ensureDTOPreservesEmailValue() {

        PilotRosterDTO dto =
                new PilotRosterDTO("P1", "pilot@company.com", true, List.of());

        assertEquals("pilot@company.com", dto.email());
    }

    @Test
    void ensureDTOPreservesPilotId() {

        PilotRosterDTO dto =
                new PilotRosterDTO("P123", "mail@test.com", true, List.of());

        assertEquals("P123", dto.pilotId());
    }

}