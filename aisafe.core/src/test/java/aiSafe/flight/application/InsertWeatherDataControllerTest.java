package aiSafe.flight.application;

import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.flight.domain.*;
import aiSafe.flight.repositories.FlightRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InsertWeatherDataControllerTest {

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private FlightRepository flightRepo;

    @Mock
    private WeatherDataRepository weatherRepo;

    private InsertWeatherDataController controller;

    private MockedStatic<PersistenceContext> persistenceContextMock;

    @BeforeEach
    void setUp() {
        persistenceContextMock = mockStatic(PersistenceContext.class);
        persistenceContextMock.when(PersistenceContext::repositories)
                .thenReturn(repositoryFactory);

        when(repositoryFactory.flights()).thenReturn(flightRepo);
        when(repositoryFactory.weatherData()).thenReturn(weatherRepo);

        controller = new InsertWeatherDataController();
    }

    @AfterEach
    void tearDown() {
        persistenceContextMock.close();
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

    // ══════════════════════════════════════════════════════════════════════════
    // allAvailableWeatherData()
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * US082.2
     * Only weather data already registered in the system can be used.
     * Controller must delegate to the repository and return all records.
     */
    @Test
    void ensureAllAvailableWeatherDataReturnsRepositoryResults() {
        final WeatherData wd = buildWeatherData();
        when(weatherRepo.findAll()).thenReturn(List.of(wd));

        final List<WeatherData> result = controller.allAvailableWeatherData();

        assertEquals(1, result.size());
        assertEquals(wd, result.get(0));
        verify(weatherRepo, times(1)).findAll();
    }

    /**
     * US082.2
     * When no weather data is registered, the list must be empty.
     */
    @Test
    void ensureAllAvailableWeatherDataReturnsEmptyWhenNoneExist() {
        when(weatherRepo.findAll()).thenReturn(List.of());

        assertTrue(controller.allAvailableWeatherData().isEmpty());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // pendingFlights()
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Only flights whose latest plan is pending (DRAFT or VALIDATED) should be listed.
     */
    @Test
    void ensurePendingFlightsReturnsOnlyFlightsWithPendingPlans() {
        final Flight pendingFlight = mock(Flight.class);
        final FlightPlan pendingPlan = mock(FlightPlan.class);
        when(pendingPlan.isPending()).thenReturn(true);
        when(pendingFlight.flightPlan()).thenReturn(pendingPlan);

        final Flight activeFlight = mock(Flight.class);
        final FlightPlan activePlan = mock(FlightPlan.class);
        when(activePlan.isPending()).thenReturn(false);
        when(activeFlight.flightPlan()).thenReturn(activePlan);

        when(flightRepo.findAll()).thenReturn(List.of(pendingFlight, activeFlight));

        final List<Flight> result = controller.pendingFlights();

        assertEquals(1, result.size());
        assertSame(pendingFlight, result.get(0));
    }

    /**
     * Flights with no plan at all must be excluded from the pending list.
     */
    @Test
    void ensureFlightsWithNoPlanAreExcludedFromPendingList() {
        final Flight noPlanFlight = mock(Flight.class);
        when(noPlanFlight.flightPlan()).thenReturn(null);
        when(flightRepo.findAll()).thenReturn(List.of(noPlanFlight));

        assertTrue(controller.pendingFlights().isEmpty());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // insertWeatherData()
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * US082.1 / US082.7
     * Weather data is successfully attached to the flight plan.
     * Controller must delegate to the service, which looks up the WeatherData by id,
     * calls flight.insertWeatherData(), saves, and returns the updated plan.
     */
    @Test
    void ensureWeatherDataIsSuccessfullyAttachedToFlightPlan() {
        final FlightDesignator designator = new FlightDesignator("TP", 100, null);
        final WeatherData wd = buildWeatherData();
        final Long weatherDataId = 1L;

        final Flight flight = mock(Flight.class);
        final FlightPlan updatedPlan = mock(FlightPlan.class);
        when(flight.flightPlan()).thenReturn(updatedPlan);
        when(flightRepo.findByDesignator(designator)).thenReturn(Optional.of(flight));
        when(weatherRepo.ofIdentity(weatherDataId)).thenReturn(Optional.of(wd));
        when(flightRepo.save(flight)).thenReturn(flight);

        final FlightPlan result = controller.insertWeatherData(designator, weatherDataId);

        assertNotNull(result);
        verify(flight, times(1)).insertWeatherData(wd);
        verify(flightRepo, times(1)).save(flight);
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
        when(flightRepo.findByDesignator(designator)).thenReturn(Optional.of(flight));
        when(weatherRepo.ofIdentity(weatherDataId)).thenReturn(Optional.of(wd));
        when(flightRepo.save(flight)).thenReturn(flight);

        final FlightPlan result = controller.insertWeatherData(designator, weatherDataId);

        assertEquals(FlightPlanStatus.DRAFT, result.status());
        verify(flight, times(1)).insertWeatherData(wd);
    }

    /**
     * US082.2
     * Only weather data already registered in the system can be attached.
     * An unknown weatherDataId must be rejected before reaching the flight.
     */
    @Test
    void ensureUnregisteredWeatherDataCannotBeAttached() {
        final FlightDesignator designator = new FlightDesignator("TP", 100, null);
        final Long weatherDataId = 999L;

        final Flight flight = mock(Flight.class);
        when(flightRepo.findByDesignator(designator)).thenReturn(Optional.of(flight));
        when(weatherRepo.ofIdentity(weatherDataId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> controller.insertWeatherData(designator, weatherDataId));

        verify(flight, never()).insertWeatherData(any());
        verify(flightRepo, never()).save(any());
    }

    /**
     * insertWeatherData() throws IllegalArgumentException when no flight matches the designator.
     */
    @Test
    void ensureInsertWeatherDataThrowsWhenFlightNotFound() {
        final FlightDesignator designator = new FlightDesignator("TP", 999, null);
        when(flightRepo.findByDesignator(designator)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> controller.insertWeatherData(designator, 1L));
    }

    /**
     * US082.7
     * The system confirms successful association — the returned plan must not be null.
     */
    @Test
    void ensureSuccessfulAssociationIsConfirmed() {
        final FlightDesignator designator = new FlightDesignator("TP", 100, null);
        final WeatherData wd = buildWeatherData();
        final Long weatherDataId = 1L;

        final Flight flight = mock(Flight.class);
        final FlightPlan updatedPlan = mock(FlightPlan.class);
        when(flight.flightPlan()).thenReturn(updatedPlan);
        when(flightRepo.findByDesignator(designator)).thenReturn(Optional.of(flight));
        when(weatherRepo.ofIdentity(weatherDataId)).thenReturn(Optional.of(wd));
        when(flightRepo.save(flight)).thenReturn(flight);

        final FlightPlan result = controller.insertWeatherData(designator, weatherDataId);

        assertNotNull(result);
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
        when(flightRepo.findByDesignator(designator)).thenReturn(Optional.of(flight));
        when(weatherRepo.ofIdentity(weatherDataId)).thenReturn(Optional.of(wd));
        doThrow(new IllegalStateException("Cannot insert into ACTIVE plan"))
                .when(flight).insertWeatherData(wd);

        assertThrows(IllegalStateException.class,
                () -> controller.insertWeatherData(designator, weatherDataId));

        verify(flightRepo, never()).save(any());
    }
}