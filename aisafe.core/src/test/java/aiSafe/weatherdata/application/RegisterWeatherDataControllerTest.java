package aiSafe.weatherdata.application;

import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircontrolarea.domain.AreaName;
import aiSafe.aircontrolarea.domain.Coordinate;
import aiSafe.aircontrolarea.domain.GeoBoundary;
import aiSafe.aircontrolarea.domain.MaxAltitude;
import aiSafe.aircontrolarea.domain.MinFuelAtArrival;
import aiSafe.aircontrolarea.repositories.AirControlAreaRepository;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.weatherdata.domain.WeatherData;
import aiSafe.weatherdata.domain.WindSpeedUnit;
import aiSafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers {@link RegisterWeatherDataController} with AAA style and mocked infrastructure.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegisterWeatherDataControllerTest {

    @Mock
    private TransactionalContext transactionalContext;

    @Mock
    private AuthorizationService authz;

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private WeatherDataRepository weatherDataRepository;

    @Mock
    private AirControlAreaRepository airControlAreaRepository;

    private RegisterWeatherDataController controller;

    @BeforeEach
    void setUp() {
        when(repositoryFactory.newTransactionalContext()).thenReturn(transactionalContext);
        when(repositoryFactory.weatherData(transactionalContext)).thenReturn(weatherDataRepository);
        when(repositoryFactory.airControlAreas(transactionalContext)).thenReturn(airControlAreaRepository);

        doNothing().when(transactionalContext).beginTransaction();
        doNothing().when(transactionalContext).commit();
        doNothing().when(transactionalContext).rollback();
        doNothing().when(transactionalContext).close();

        controller = new RegisterWeatherDataController(authz, repositoryFactory);

        doNothing().when(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.WEATHER_PERSON, AisafeRoles.ADMIN);
    }

    private static AirControlArea sampleControlArea() {
        final GeoBoundary boundary = new GeoBoundary(List.of(
                new Coordinate(38.0, -10.0),
                new Coordinate(38.0, -8.0),
                new Coordinate(40.0, -10.0),
                new Coordinate(40.0, -8.0)
        ));
        return new AirControlArea(
                new AreaCode("LP"),
                new AreaName("Lisbon"),
                new MinFuelAtArrival(50.0),
                new MaxAltitude(5000),
                boundary
        );
    }

    @Test
    void controlAreaByAreaCode_returns_aggregate_with_expected_bounds() {
        // Arrange
        final AirControlArea area = sampleControlArea();
        when(airControlAreaRepository.findByAreaCode(any(AreaCode.class))).thenReturn(Optional.of(area));

        // Act
        final AirControlArea result = controller.controlAreaByAreaCode("LP");

        // Assert
        assertEquals(area, result);
        assertEquals("LP", result.areaCode().value());
        assertEquals("Lisbon", result.areaName().value());
        assertEquals(38.0, result.geoBoundary().minLatitude());
        assertEquals(40.0, result.geoBoundary().maxLatitude());
        assertEquals(-10.0, result.geoBoundary().minLongitude());
        assertEquals(-8.0, result.geoBoundary().maxLongitude());
        assertEquals(5000.0, result.maxAltitude().value());

        verify(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.WEATHER_PERSON, AisafeRoles.ADMIN);
        verify(airControlAreaRepository).findByAreaCode(any(AreaCode.class));
    }

    @Test
    void controlAreaByAreaCode_throws_when_area_unknown() {
        // Arrange
        when(airControlAreaRepository.findByAreaCode(any(AreaCode.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.controlAreaByAreaCode("XX"));
    }

    @Test
    void validateSubArea_doesNothing_when_inside_boundary() {
        // Arrange
        final AirControlArea area = sampleControlArea();

        when(airControlAreaRepository.findByAreaCode(any(AreaCode.class))).thenReturn(Optional.of(area));

        // Act
        controller.validateSubArea("LP", 38.5, 39.5, -9.5, -8.5, 0, 4500);

        // Assert — no exception; auth verified
        verify(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.WEATHER_PERSON, AisafeRoles.ADMIN);
    }

    @Test
    void validateSubArea_throws_when_outside_boundary() {
        // Arrange
        final AirControlArea area = sampleControlArea();

        when(airControlAreaRepository.findByAreaCode(any(AreaCode.class))).thenReturn(Optional.of(area));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.validateSubArea("LP", 41.0, 42.0, -9.5, -8.5, 0, 1000));
    }

    @Test
    void registerWeatherData_saves_when_valid_and_no_duplicates() {
        // Arrange
        final AirControlArea area = sampleControlArea();
        when(airControlAreaRepository.findByAreaCode(any(AreaCode.class))).thenReturn(Optional.of(area));
        when(weatherDataRepository.existsByAreaCodeAndObservationDateAndWeatherSource(any(), any(), any(), any()))
                .thenReturn(false);
        when(weatherDataRepository.existsByAreaCodeAndObservationDateAndWeatherSourceWithOverlappingSubArea(any(), any(), any(), any()))
                .thenReturn(false);

        final LocalDateTime start = LocalDateTime.of(2026, 4, 19, 14, 0);
        final LocalDateTime end = LocalDateTime.of(2026, 4, 19, 16, 0);

        final WeatherData saved = new WeatherData(
                area.areaCode(),
                new aiSafe.weatherdata.domain.ObservationDate(start, end),
                new aiSafe.weatherdata.domain.WeatherSource("IPMA"),
                new aiSafe.weatherdata.domain.WeatherSubArea(38.5, 39.5, -9.5, -8.5, 0, 4500),
                new aiSafe.weatherdata.domain.WindDirection(180.0),
                new aiSafe.weatherdata.domain.WindSpeed(20.0, WindSpeedUnit.KMH),
                new aiSafe.weatherdata.domain.Temperature(15.0, aiSafe.weatherdata.domain.TemperatureUnit.C),
                new aiSafe.weatherdata.domain.Pressure(1013.0, aiSafe.weatherdata.domain.PressureUnit.HPA)
        );
        when(weatherDataRepository.save(any())).thenReturn(saved);

        // Act
        final WeatherData result = controller.registerWeatherData(
                "LP",
                start,
                end,
                38.5, 39.5,
                -9.5, -8.5,
                0, 4500,
                "IPMA",
                180.0,
                20.0,
                WindSpeedUnit.KMH,
                15.0,
                aiSafe.weatherdata.domain.TemperatureUnit.C,
                1013.0,
                aiSafe.weatherdata.domain.PressureUnit.HPA
        );

        // Assert
        assertEquals(saved, result);
        verify(weatherDataRepository).save(any(WeatherData.class));
    }

    @Test
    void registerWeatherData_throws_when_duplicate_exact_subarea() {
        // Arrange
        final AirControlArea area = sampleControlArea();

        when(airControlAreaRepository.findByAreaCode(any(AreaCode.class))).thenReturn(Optional.of(area));
        when(weatherDataRepository.existsByAreaCodeAndObservationDateAndWeatherSource(any(), any(), any(), any()))
                .thenReturn(true);

        final LocalDateTime start = LocalDateTime.of(2026, 4, 19, 14, 0);
        final LocalDateTime end = LocalDateTime.of(2026, 4, 19, 16, 0);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                controller.registerWeatherData(
                        "LP",
                        start,
                        end,
                        38.5, 39.5,
                        -9.5, -8.5,
                        0, 4500,
                        "IPMA",
                        180.0,
                        20.0,
                        WindSpeedUnit.KMH,
                        15.0,
                        aiSafe.weatherdata.domain.TemperatureUnit.C,
                        1013.0,
                        aiSafe.weatherdata.domain.PressureUnit.HPA)
        );
    }

    @Test
    void registerWeatherData_throws_when_overlapping_subarea_exists() {
        // Arrange
        final AirControlArea area = sampleControlArea();

        when(airControlAreaRepository.findByAreaCode(any(AreaCode.class))).thenReturn(Optional.of(area));
        when(weatherDataRepository.existsByAreaCodeAndObservationDateAndWeatherSource(any(), any(), any(), any()))
                .thenReturn(false);
        when(weatherDataRepository.existsByAreaCodeAndObservationDateAndWeatherSourceWithOverlappingSubArea(any(), any(), any(), any()))
                .thenReturn(true);

        final LocalDateTime start = LocalDateTime.of(2026, 4, 19, 14, 0);
        final LocalDateTime end = LocalDateTime.of(2026, 4, 19, 16, 0);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                controller.registerWeatherData(
                        "LP",
                        start,
                        end,
                        38.5, 39.5,
                        -9.5, -8.5,
                        0, 4500,
                        "IPMA",
                        180.0,
                        20.0,
                        WindSpeedUnit.KMH,
                        15.0,
                        aiSafe.weatherdata.domain.TemperatureUnit.C,
                        1013.0,
                        aiSafe.weatherdata.domain.PressureUnit.HPA)
        );
    }
}
