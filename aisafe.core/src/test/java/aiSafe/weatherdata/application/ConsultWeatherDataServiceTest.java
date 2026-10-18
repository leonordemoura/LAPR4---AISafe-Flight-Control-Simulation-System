package aiSafe.weatherdata.application;

import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.weatherdata.domain.*;
import aiSafe.weatherdata.repositories.WeatherDataRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConsultWeatherDataServiceTest {

    @Mock
    private WeatherDataRepository weatherDataRepository;

    @Mock
    private RepositoryFactory repositoryFactory;

    private ConsultWeatherDataService service;

    private MockedStatic<PersistenceContext> persistenceContextMock;

    @BeforeEach
    void setUp() {
        persistenceContextMock = mockStatic(PersistenceContext.class);

        persistenceContextMock.when(PersistenceContext::repositories)
                .thenReturn(repositoryFactory);

        when(repositoryFactory.weatherData())
                .thenReturn(weatherDataRepository);

        service = new ConsultWeatherDataService(weatherDataRepository);
    }

    @AfterEach
    void tearDown() {
        persistenceContextMock.close();
    }

    // =========================================================
    // PUBLIC CONSTRUCTOR
    // =========================================================

    @Test
    void ensurePublicConstructorInitializesService() {
        assertDoesNotThrow(() -> new ConsultWeatherDataService());
    }

    // =========================================================
    // CONSULT WEATHER DATA
    // =========================================================

    @Test
    void ensureConsultWeatherDataReturnsResults() {
        final WeatherData weatherData = buildWeatherData();

        when(weatherDataRepository.findByAreaCodeAndDate(any(AreaCode.class), any(LocalDate.class)))
                .thenReturn(List.of(weatherData));

        final List<ConsultWeatherDataDTO> result =
                service.consultWeatherData("LPPT", LocalDate.of(2025, 6, 1));

        assertNotNull(result);
        assertEquals(1, result.size());

        final ConsultWeatherDataDTO dto = result.get(0);
        assertEquals("LPPT", dto.areaCode);
        assertEquals(180.0, dto.windDirection);
        assertEquals(10.0, dto.windSpeed);
        assertEquals(20.0, dto.temperature);
        assertEquals(1013.0, dto.pressure);
    }

    @Test
    void ensureConsultWeatherDataReturnsEmptyListWhenNoResults() {
        when(weatherDataRepository.findByAreaCodeAndDate(any(AreaCode.class), any(LocalDate.class)))
                .thenReturn(List.of());

        final List<ConsultWeatherDataDTO> result =
                service.consultWeatherData("LPPT", LocalDate.of(2025, 6, 1));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void ensureConsultWeatherDataThrowsWhenAreaCodeIsNull() {
        assertThrows(NullPointerException.class, () ->
                service.consultWeatherData(null, LocalDate.of(2025, 6, 1)));
    }

    @Test
    void ensureConsultWeatherDataThrowsWhenDateIsNull() {
        assertThrows(NullPointerException.class, () ->
                service.consultWeatherData("LPPT", null));
    }

    @Test
    void ensureSubAreaIsFormattedCorrectly() {
        final WeatherData weatherData = buildWeatherData();

        when(weatherDataRepository.findByAreaCodeAndDate(any(AreaCode.class), any(LocalDate.class)))
                .thenReturn(List.of(weatherData));

        final List<ConsultWeatherDataDTO> result =
                service.consultWeatherData("LPPT", LocalDate.of(2025, 6, 1));

        assertEquals(
                "Lat[38.0000,42.0000] Lon[-9.0000,-6.0000] Alt[0.0,3000.0]",
                result.get(0).weatherSubArea
        );
    }

    @Test
    void ensureConsultWeatherDataThrowsWhenAreaCodeIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                service.consultWeatherData("", LocalDate.of(2025, 6, 1)));
    }

    @Test
    void ensureConsultWeatherDataThrowsWhenAreaCodeIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                service.consultWeatherData("   ", LocalDate.of(2025, 6, 1)));
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private WeatherData buildWeatherData() {
        final WeatherData w = mock(WeatherData.class);

        when(w.areaCode()).thenReturn(new AreaCode("LPPT"));
        when(w.observationDate()).thenReturn(
                new ObservationDate(
                        LocalDateTime.of(2025, 6, 1, 0, 0),
                        LocalDateTime.of(2025, 6, 1, 23, 59)
                )
        );
        when(w.windDirection()).thenReturn(new WindDirection(180.0));
        when(w.windSpeed()).thenReturn(new WindSpeed(10.0, WindSpeedUnit.KT));
        when(w.temperature()).thenReturn(new Temperature(20.0, TemperatureUnit.C));
        when(w.pressure()).thenReturn(new Pressure(1013.0, PressureUnit.HPA));

        final WeatherSubArea subArea = mock(WeatherSubArea.class);
        when(subArea.minLatitude()).thenReturn(38.0);
        when(subArea.maxLatitude()).thenReturn(42.0);
        when(subArea.minLongitude()).thenReturn(-9.0);
        when(subArea.maxLongitude()).thenReturn(-6.0);
        when(subArea.minAltitude()).thenReturn(0.0);
        when(subArea.maxAltitude()).thenReturn(3000.0);
        when(w.weatherSubArea()).thenReturn(subArea);

        return w;
    }
}