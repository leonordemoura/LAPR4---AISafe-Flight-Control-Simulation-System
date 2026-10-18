package aiSafe.aircraft.application;

import aiSafe.aircraft.domain.Aircraft;
import aiSafe.aircraft.repositories.AircraftRepository;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ListFleetControllerTest {

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private AircraftRepository aircraftRepo;

    @Mock
    private AirTransportCompanyRepository companyRepo;

    @Mock
    private AirTransportCompany company;

    @Mock
    private Aircraft aircraft1;

    @Mock
    private Aircraft aircraft2;

    private ListFleetController controller;

    private MockedStatic<PersistenceContext> persistenceContextMock;

    private static final String COMPANY_IATA = "TP";

    // =========================================================
    // SETUP / TEARDOWN
    // =========================================================

    @BeforeEach
    void setUp() {
        persistenceContextMock = mockStatic(PersistenceContext.class);

        persistenceContextMock.when(PersistenceContext::repositories)
                .thenReturn(repositoryFactory);

        when(repositoryFactory.aircrafts()).thenReturn(aircraftRepo);
        when(repositoryFactory.getAirTransportCompanyRepository()).thenReturn(companyRepo);

        controller = new ListFleetController();
    }

    @AfterEach
    void tearDown() {
        persistenceContextMock.close();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void stubCompanyFound() {
        when(companyRepo.findByIataCode(any(IataCode.class))).thenReturn(Optional.of(company));
    }

    private void stubCompanyNotFound() {
        when(companyRepo.findByIataCode(any(IataCode.class))).thenReturn(Optional.empty());
    }

    // =========================================================
    // TESTS — availableCompanies
    // =========================================================

    @Test
    void ensureAvailableCompaniesReturnsRepositoryValues() {
        final List<AirTransportCompany> companies = List.of(company);
        when(companyRepo.findAll()).thenReturn(companies);

        final List<AirTransportCompany> result = controller.availableCompanies();

        assertEquals(companies, result);
        verify(companyRepo).findAll();
    }

    @Test
    void ensureAvailableCompaniesReturnsEmptyListWhenNoneExist() {
        when(companyRepo.findAll()).thenReturn(List.of());

        final List<AirTransportCompany> result = controller.availableCompanies();

        assertTrue(result.isEmpty());
    }

    // =========================================================
    // TESTS — resolveCompany (shared guard clause)
    // =========================================================

    @Test
    void ensureGetFleetThrowsWhenCompanyNotFound() {
        stubCompanyNotFound();

        assertThrows(IllegalStateException.class, () ->
                controller.getFleet(COMPANY_IATA));
    }

    // =========================================================
    // TESTS — getFleet
    // =========================================================

    @Test
    void ensureGetFleetReturnsAircraftForCompany() {
        stubCompanyFound();
        final List<Aircraft> fleet = List.of(aircraft1, aircraft2);
        when(aircraftRepo.findByCompany(company)).thenReturn(fleet);

        final List<Aircraft> result = controller.getFleet(COMPANY_IATA);

        assertEquals(fleet, result);
        verify(aircraftRepo).findByCompany(company);
    }

    @Test
    void ensureGetFleetReturnsEmptyListWhenNoAircraft() {
        stubCompanyFound();
        when(aircraftRepo.findByCompany(company)).thenReturn(List.of());

        assertTrue(controller.getFleet(COMPANY_IATA).isEmpty());
    }

    // =========================================================
    // TESTS — getFleetByModel
    // =========================================================

    @Test
    void ensureGetFleetByModelReturnsFilteredAircraft() {
        stubCompanyFound();
        final List<Aircraft> fleet = List.of(aircraft1);
        when(aircraftRepo.findByCompanyAndModel(company, "A320")).thenReturn(fleet);

        final List<Aircraft> result = controller.getFleetByModel(COMPANY_IATA, "A320");

        assertEquals(fleet, result);
        verify(aircraftRepo).findByCompanyAndModel(company, "A320");
    }

    @Test
    void ensureGetFleetByModelThrowsWhenCompanyNotFound() {
        stubCompanyNotFound();

        assertThrows(IllegalStateException.class, () ->
                controller.getFleetByModel(COMPANY_IATA, "A320"));
    }

    // =========================================================
    // TESTS — getFleetByMaker
    // =========================================================

    @Test
    void ensureGetFleetByMakerReturnsFilteredAircraft() {
        stubCompanyFound();
        final List<Aircraft> fleet = List.of(aircraft1, aircraft2);
        when(aircraftRepo.findByCompanyAndMaker(company, "Airbus")).thenReturn(fleet);

        final List<Aircraft> result = controller.getFleetByMaker(COMPANY_IATA, "Airbus");

        assertEquals(fleet, result);
        verify(aircraftRepo).findByCompanyAndMaker(company, "Airbus");
    }

    @Test
    void ensureGetFleetByMakerThrowsWhenCompanyNotFound() {
        stubCompanyNotFound();

        assertThrows(IllegalStateException.class, () ->
                controller.getFleetByMaker(COMPANY_IATA, "Airbus"));
    }

    // =========================================================
    // TESTS — getFleetByMinCapacity
    // =========================================================

    @Test
    void ensureGetFleetByMinCapacityReturnsFilteredAircraft() {
        stubCompanyFound();
        final List<Aircraft> fleet = List.of(aircraft1);
        when(aircraftRepo.findByCompanyAndMinCapacity(company, 150)).thenReturn(fleet);

        final List<Aircraft> result = controller.getFleetByMinCapacity(COMPANY_IATA, 150);

        assertEquals(fleet, result);
        verify(aircraftRepo).findByCompanyAndMinCapacity(company, 150);
    }

    @Test
    void ensureGetFleetByMinCapacityThrowsWhenCompanyNotFound() {
        stubCompanyNotFound();

        assertThrows(IllegalStateException.class, () ->
                controller.getFleetByMinCapacity(COMPANY_IATA, 150));
    }

    // =========================================================
    // TESTS — getFleetByMaxCapacity
    // =========================================================

    @Test
    void ensureGetFleetByMaxCapacityReturnsFilteredAircraft() {
        stubCompanyFound();
        final List<Aircraft> fleet = List.of(aircraft2);
        when(aircraftRepo.findByCompanyAndMaxCapacity(company, 300)).thenReturn(fleet);

        final List<Aircraft> result = controller.getFleetByMaxCapacity(COMPANY_IATA, 300);

        assertEquals(fleet, result);
        verify(aircraftRepo).findByCompanyAndMaxCapacity(company, 300);
    }

    @Test
    void ensureGetFleetByMaxCapacityThrowsWhenCompanyNotFound() {
        stubCompanyNotFound();

        assertThrows(IllegalStateException.class, () ->
                controller.getFleetByMaxCapacity(COMPANY_IATA, 300));
    }

    // =========================================================
    // TESTS — getFleetByMaxAge
    // =========================================================

    @Test
    void ensureGetFleetByMaxAgeReturnsFilteredAircraft() {
        stubCompanyFound();
        final List<Aircraft> fleet = List.of(aircraft1);
        when(aircraftRepo.findByCompanyAndMaxAge(company, 10)).thenReturn(fleet);

        final List<Aircraft> result = controller.getFleetByMaxAge(COMPANY_IATA, 10);

        assertEquals(fleet, result);
        verify(aircraftRepo).findByCompanyAndMaxAge(company, 10);
    }

    @Test
    void ensureGetFleetByMaxAgeThrowsWhenCompanyNotFound() {
        stubCompanyNotFound();

        assertThrows(IllegalStateException.class, () ->
                controller.getFleetByMaxAge(COMPANY_IATA, 10));
    }

    // =========================================================
    // TESTS — constructor
    // =========================================================

    @Test
    void ensureDefaultConstructorLoadsSuccessfully() {
        assertDoesNotThrow(() -> new ListFleetController());
    }
}