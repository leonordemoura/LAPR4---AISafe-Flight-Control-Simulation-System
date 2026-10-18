package aiSafe.flight.application;

import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.flight.domain.*;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.weatherdata.domain.*;
import aiSafe.weatherdata.repositories.WeatherDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InsertWeatherDataServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private WeatherDataRepository weatherDataRepository;

    private InsertWeatherDataService service;

    @BeforeEach
    void setUp() {
        service = new InsertWeatherDataService(flightRepository, weatherDataRepository);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private WeatherData buildWeatherData() {
        return new WeatherData(
                new AreaCode("LPPT"),
                new ObservationDate(
                        LocalDateTime.of(2026, 6, 5, 0, 0),
                        LocalDateTime.of(2026, 6, 5, 23, 59)
                ),
                new WeatherSource("METAR"),
                new WeatherSubArea(38.0, 39.0, -9.5, -8.5, 0, 3000),
                new WindDirection(270),
                new WindSpeed(14, WindSpeedUnit.KT),
                new Temperature(18, TemperatureUnit.C),
                new Pressure(1013, PressureUnit.HPA)
        );
    }

    private FlightPlan buildPendingPlan(final LocalDateTime departure) {
        final FlightPlan plan = mock(FlightPlan.class);
        final DepartureDateTime departureDateTime = mock(DepartureDateTime.class);
        when(departureDateTime.value()).thenReturn(departure);
        when(plan.departureDateTime()).thenReturn(departureDateTime);
        when(plan.isPending()).thenReturn(true);
        return plan;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // listAvailableWeatherData()
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * US082.2
     * When the flight has a pending plan, the service must search weather data
     * for the plan's departure date and the given area, delegating to the repository.
     */
    @Test
    void ensureListAvailableWeatherDataReturnsRepositoryResultsForPendingPlan() {
        final FlightDesignator designator = new FlightDesignator("TP", 100, null);
        final AreaCode areaCode = new AreaCode("LPPT");
        final LocalDateTime departure = LocalDateTime.of(2026, 6, 5, 10, 0);

        final FlightPlan plan = buildPendingPlan(departure);
        final Flight flight = mock(Flight.class);
        when(flight.flightPlan()).thenReturn(plan);
        when(flightRepository.findByDesignator(designator)).thenReturn(Optional.of(flight));

        final WeatherData wd = buildWeatherData();
        when(weatherDataRepository.findByAreaCodeAndDate(areaCode, departure.toLocalDate()))
                .thenReturn(List.of(wd));

        final List<WeatherData> result = service.listAvailableWeatherData(designator, areaCode);

        assertEquals(1, result.size());
        assertEquals(wd, result.get(0));
        verify(weatherDataRepository, times(1))
                .findByAreaCodeAndDate(areaCode, departure.toLocalDate());
    }

    /**
     * When no weather data matches the area/date, an empty list is returned.
     */
    @Test
    void ensureListAvailableWeatherDataReturnsEmptyWhenNoneMatch() {
        final FlightDesignator designator = new FlightDesignator("TP", 100, null);
        final AreaCode areaCode = new AreaCode("LPPT");
        final LocalDateTime departure = LocalDateTime.of(2026, 6, 5, 10, 0);

        final FlightPlan plan = buildPendingPlan(departure);
        final Flight flight = mock(Flight.class);
        when(flight.flightPlan()).thenReturn(plan);
        when(flightRepository.findByDesignator(designator)).thenReturn(Optional.of(flight));

        when(weatherDataRepository.findByAreaCodeAndDate(any(), any())).thenReturn(List.of());

        assertTrue(service.listAvailableWeatherData(designator, areaCode).isEmpty());
    }

    /**
     * listAvailableWeatherData() throws IllegalArgumentException when no flight matches the designator.
     */
    @Test
    void ensureListAvailableWeatherDataThrowsWhenFlightNotFound() {
        final FlightDesignator designator = new FlightDesignator("TP", 999, null);
        final AreaCode areaCode = new AreaCode("LPPT");
        when(flightRepository.findByDesignator(designator)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.listAvailableWeatherData(designator, areaCode));

        verify(weatherDataRepository, never()).findByAreaCodeAndDate(any(), any());
    }

    /**
     * listAvailableWeatherData() throws IllegalStateException when the flight has no plan at all.
     */
    @Test
    void ensureListAvailableWeatherDataThrowsWhenFlightHasNoPlan() {
        final FlightDesignator designator = new FlightDesignator("TP", 100, null);
        final AreaCode areaCode = new AreaCode("LPPT");

        final Flight flight = mock(Flight.class);
        when(flight.flightPlan()).thenReturn(null);
        when(flightRepository.findByDesignator(designator)).thenReturn(Optional.of(flight));

        assertThrows(IllegalStateException.class,
                () -> service.listAvailableWeatherData(designator, areaCode));

        verify(weatherDataRepository, never()).findByAreaCodeAndDate(any(), any());
    }

    /**
     * listAvailableWeatherData() throws IllegalStateException when the plan is not pending
     * (e.g. it's ACTIVE — already part of an ongoing simulation).
     */
    @Test
    void ensureListAvailableWeatherDataThrowsWhenPlanIsNotPending() {
        final FlightDesignator designator = new FlightDesignator("TP", 100, null);
        final AreaCode areaCode = new AreaCode("LPPT");

        final FlightPlan plan = mock(FlightPlan.class);
        when(plan.isPending()).thenReturn(false);

        final Flight flight = mock(Flight.class);
        when(flight.flightPlan()).thenReturn(plan);
        when(flightRepository.findByDesignator(designator)).thenReturn(Optional.of(flight));

        assertThrows(IllegalStateException.class,
                () -> service.listAvailableWeatherData(designator, areaCode));

        verify(weatherDataRepository, never()).findByAreaCodeAndDate(any(), any());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // insertWeatherData()
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * US082.1 / US082.7
     * Weather data is successfully attached to the flight plan.
     * Service must look up the WeatherData by id, call flight.insertWeatherData(),
     * save the flight, and return the updated plan.
     */
    @Test
    void ensureWeatherDataIsSuccessfullyAttachedToFlightPlan() {
        final FlightDesignator designator = new FlightDesignator("TP", 100, null);
        final WeatherData wd = buildWeatherData();
        final Long weatherDataId = 1L;

        final Flight flight = mock(Flight.class);
        final FlightPlan updatedPlan = mock(FlightPlan.class);
        when(flight.flightPlan()).thenReturn(updatedPlan);
        when(flightRepository.findByDesignator(designator)).thenReturn(Optional.of(flight));
        when(weatherDataRepository.ofIdentity(weatherDataId)).thenReturn(Optional.of(wd));
        when(flightRepository.save(flight)).thenReturn(flight);

        final FlightPlan result = service.insertWeatherData(designator, weatherDataId);

        assertNotNull(result);
        verify(flight, times(1)).insertWeatherData(wd);
        verify(flightRepository, times(1)).save(flight);
    }

    /**
     * US082.5
     * After inserting weather data into a previously VALIDATED plan,
     * the returned plan must be in DRAFT (test deemed void).
     */
    @Test
    void ensureAttachingWeatherDataInvalidatesPreviousValidation() {
        final FlightDesignator designator = new FlightDesignator("TP", 100, null);
        final WeatherData wd = buildWeatherData();
        final Long weatherDataId = 1L;

        final Flight flight = mock(Flight.class);
        final FlightPlan updatedPlan = mock(FlightPlan.class);
        when(updatedPlan.status()).thenReturn(FlightPlanStatus.DRAFT);
        when(flight.flightPlan()).thenReturn(updatedPlan);
        when(flightRepository.findByDesignator(designator)).thenReturn(Optional.of(flight));
        when(weatherDataRepository.ofIdentity(weatherDataId)).thenReturn(Optional.of(wd));
        when(flightRepository.save(flight)).thenReturn(flight);

        final FlightPlan result = service.insertWeatherData(designator, weatherDataId);

        assertEquals(FlightPlanStatus.DRAFT, result.status());
        verify(flight, times(1)).insertWeatherData(wd);
    }

    /**
     * insertWeatherData() throws IllegalArgumentException when no flight matches the designator.
     */
    @Test
    void ensureInsertWeatherDataThrowsWhenFlightNotFound() {
        final FlightDesignator designator = new FlightDesignator("TP", 999, null);
        when(flightRepository.findByDesignator(designator)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.insertWeatherData(designator, 1L));

        verify(weatherDataRepository, never()).ofIdentity(any());
        verify(flightRepository, never()).save(any());
    }

    /**
     * US082.2
     * Only weather data already registered in the system can be attached.
     * An unknown weatherDataId must be rejected before reaching the flight.
     */
    @Test
    void ensureInsertWeatherDataThrowsWhenWeatherDataNotFound() {
        final FlightDesignator designator = new FlightDesignator("TP", 100, null);
        final Long weatherDataId = 999L;

        final Flight flight = mock(Flight.class);
        when(flightRepository.findByDesignator(designator)).thenReturn(Optional.of(flight));
        when(weatherDataRepository.ofIdentity(weatherDataId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.insertWeatherData(designator, weatherDataId));

        verify(flight, never()).insertWeatherData(any());
        verify(flightRepository, never()).save(any());
    }

    /**
     * The flight must NOT be saved if the domain rejects the insertion.
     */
    @Test
    void ensureFlightIsNotSavedWhenDomainInsertionFails() {
        final FlightDesignator designator = new FlightDesignator("TP", 100, null);
        final WeatherData wd = buildWeatherData();
        final Long weatherDataId = 1L;

        final Flight flight = mock(Flight.class);
        when(flightRepository.findByDesignator(designator)).thenReturn(Optional.of(flight));
        when(weatherDataRepository.ofIdentity(weatherDataId)).thenReturn(Optional.of(wd));
        doThrow(new IllegalStateException("Cannot insert into ACTIVE plan"))
                .when(flight).insertWeatherData(wd);

        assertThrows(IllegalStateException.class,
                () -> service.insertWeatherData(designator, weatherDataId));

        verify(flightRepository, never()).save(any());
    }
}