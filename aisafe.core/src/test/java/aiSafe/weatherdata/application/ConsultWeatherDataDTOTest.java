package aiSafe.weatherdata.application;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ConsultWeatherDataDTOTest {

    // ── Constructor ───────────────────────────────────────────────────────

    @Test
    void ensureDTOIsCreatedWithValidValues() {
        final ConsultWeatherDataDTO dto = buildDTO();

        assertNotNull(dto);
    }

    // ── Field values ──────────────────────────────────────────────────────

    @Test
    void ensureAreaCodeIsStoredCorrectly() {
        final ConsultWeatherDataDTO dto = buildDTO();

        assertEquals("LPPR", dto.areaCode);
    }

    @Test
    void ensureObservationStartDateIsStoredCorrectly() {
        final ConsultWeatherDataDTO dto = buildDTO();

        assertEquals(LocalDateTime.of(2026, 6, 22, 0, 0), dto.observationStartDate);
    }

    @Test
    void ensureObservationEndDateIsStoredCorrectly() {
        final ConsultWeatherDataDTO dto = buildDTO();

        assertEquals(LocalDateTime.of(2026, 6, 22, 23, 59), dto.observationEndDate);
    }

    @Test
    void ensureWindDirectionIsStoredCorrectly() {
        final ConsultWeatherDataDTO dto = buildDTO();

        assertEquals(180.0, dto.windDirection);
    }

    @Test
    void ensureWindSpeedIsStoredCorrectly() {
        final ConsultWeatherDataDTO dto = buildDTO();

        assertEquals(10.0, dto.windSpeed);
    }

    @Test
    void ensureTemperatureIsStoredCorrectly() {
        final ConsultWeatherDataDTO dto = buildDTO();

        assertEquals(20.0, dto.temperature);
    }

    @Test
    void ensurePressureIsStoredCorrectly() {
        final ConsultWeatherDataDTO dto = buildDTO();

        assertEquals(1013.0, dto.pressure);
    }

    @Test
    void ensureWeatherSubAreaIsStoredCorrectly() {
        final ConsultWeatherDataDTO dto = buildDTO();

        assertEquals("Lat[38.0000,42.0000] Lon[-9.0000,-6.0000] Alt[0.0,3000.0]", dto.weatherSubArea);
    }

    // ── Boundary values ───────────────────────────────────────────────────

    @Test
    void ensureZeroWindSpeedIsAccepted() {
        final ConsultWeatherDataDTO dto = new ConsultWeatherDataDTO(
                "LPPR",
                LocalDateTime.of(2026, 6, 22, 0, 0),
                LocalDateTime.of(2026, 6, 22, 23, 59),
                0.0,
                0.0,
                20.0,
                1013.0,
                "Lat[38.0000,42.0000] Lon[-9.0000,-6.0000] Alt[0.0,3000.0]"
        );

        assertEquals(0.0, dto.windSpeed);
        assertEquals(0.0, dto.windDirection);
    }

    @Test
    void ensureNegativeTemperatureIsAccepted() {
        final ConsultWeatherDataDTO dto = new ConsultWeatherDataDTO(
                "LPPR",
                LocalDateTime.of(2026, 6, 22, 0, 0),
                LocalDateTime.of(2026, 6, 22, 23, 59),
                180.0,
                10.0,
                -30.0,
                1013.0,
                "Lat[38.0000,42.0000] Lon[-9.0000,-6.0000] Alt[0.0,3000.0]"
        );

        assertEquals(-30.0, dto.temperature);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private ConsultWeatherDataDTO buildDTO() {
        return new ConsultWeatherDataDTO(
                "LPPR",
                LocalDateTime.of(2026, 6, 22, 0, 0),
                LocalDateTime.of(2026, 6, 22, 23, 59),
                180.0,
                10.0,
                20.0,
                1013.0,
                "Lat[38.0000,42.0000] Lon[-9.0000,-6.0000] Alt[0.0,3000.0]"
        );
    }
}