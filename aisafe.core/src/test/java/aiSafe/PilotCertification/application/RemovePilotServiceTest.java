package aiSafe.PilotCertification.application;

import aiSafe.PilotCertification.domain.PilotCertification;
import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.Email;

import eapli.framework.domain.repositories.TransactionalContext;

import org.junit.jupiter.api.*;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RemovePilotServiceTest {

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private TransactionalContext txCtx;

    @Mock
    private aiSafe.PilotCertification.repositories.PilotCertificationRepository pilotRepo;

    @Mock
    private aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository companyRepo;

    @Mock
    private aiSafe.flight.repositories.FlightRepository flightRepo;

    @Mock
    private AirTransportCompany company;

    @Mock
    private AirTransportCompany otherCompany;

    @Mock
    private PilotCertification pilot;

    private RemovePilotService service;

    @BeforeEach
    void setUp() {

        repositoryFactory = mock(RepositoryFactory.class);

        txCtx = mock(TransactionalContext.class);

        pilotRepo = mock(aiSafe.PilotCertification.repositories.PilotCertificationRepository.class);

        companyRepo = mock(aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository.class);

        flightRepo = mock(aiSafe.flight.repositories.FlightRepository.class);

        company = mock(AirTransportCompany.class);

        otherCompany = mock(AirTransportCompany.class);

        pilot = mock(PilotCertification.class);

        when(repositoryFactory.newTransactionalContext())
                .thenReturn(txCtx);

        when(repositoryFactory.pilots(txCtx))
                .thenReturn(pilotRepo);

        when(repositoryFactory.getAirTransportCompanyRepository(txCtx))
                .thenReturn(companyRepo);

        when(repositoryFactory.flights(txCtx))
                .thenReturn(flightRepo);

        doNothing().when(txCtx).beginTransaction();
        doNothing().when(txCtx).commit();
        doNothing().when(txCtx).rollback();

        service = new RemovePilotService(repositoryFactory);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private void defaultSuccessMocks() {

        when(companyRepo.findByCollaboratorEmail(any()))
                .thenReturn(Optional.of(company));

        when(pilotRepo.findByPilotId(any()))
                .thenReturn(Optional.of(pilot));

        when(pilot.company())
                .thenReturn(company);

        when(pilot.isActive())
                .thenReturn(true);

        when(flightRepo.hasActiveFlightsForPilot(any()))
                .thenReturn(false);

        when(pilotRepo.save(any()))
                .thenAnswer(i -> i.getArgument(0));
    }

    // =====================================================
    // TEST 1 - SUCCESS
    // =====================================================

    @Test
    void ensureAuthorizedCollaboratorCanDeactivatePilot() {

        defaultSuccessMocks();

        service.deactivatePilot(
                new PilotID("P001"),
                new Email("collab@aisafe.local")
        );

        verify(pilot).deactivate();

        verify(pilotRepo).save(pilot);

        verify(txCtx).commit();
    }

    // =====================================================
    // TEST 2 - OTHER COMPANY
    // =====================================================

    @Test
    void ensureCollaboratorCannotDeactivatePilotFromAnotherCompany() {

        defaultSuccessMocks();

        when(pilot.company())
                .thenReturn(otherCompany);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.deactivatePilot(
                        new PilotID("P001"),
                        new Email("collab@aisafe.local")
                )
        );

        verify(txCtx).rollback();
    }

    // =====================================================
    // TEST 3 - ALREADY INACTIVE
    // =====================================================

    @Test
    void ensureInactivePilotCannotBeDeactivatedAgain() {

        defaultSuccessMocks();

        when(pilot.isActive())
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.deactivatePilot(
                        new PilotID("P001"),
                        new Email("collab@aisafe.local")
                )
        );

        verify(txCtx).rollback();
    }

    // =====================================================
    // TEST 4 - ACTIVE FLIGHTS
    // =====================================================

    @Test
    void ensurePilotWithAssignedFlightsCannotBeDeactivated() {

        defaultSuccessMocks();

        when(flightRepo.hasActiveFlightsForPilot(
                new PilotID("P001")
        )).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.deactivatePilot(
                        new PilotID("P001"),
                        new Email("collab@aisafe.local")
                )
        );

        verify(txCtx).rollback();
    }

    // =====================================================
    // TEST 5 - NO ACTIVE FLIGHTS
    // =====================================================

    @Test
    void ensurePilotWithoutAssignedFlightsCanBeDeactivated() {

        defaultSuccessMocks();

        service.deactivatePilot(
                new PilotID("P001"),
                new Email("collab@aisafe.local")
        );

        verify(pilot).deactivate();

        verify(pilotRepo).save(pilot);
    }

    // =====================================================
    // TEST 6 - USER ACCOUNT REMAINS
    // =====================================================

    @Test
    void ensurePilotDeactivationDoesNotDeleteUserAccount() {

        defaultSuccessMocks();

        service.deactivatePilot(
                new PilotID("P001"),
                new Email("collab@aisafe.local")
        );

        verify(flightRepo)
                .hasActiveFlightsForPilot(new PilotID("P001"));

        verify(pilot).deactivate();

        verify(pilotRepo).save(pilot);
    }

    // =====================================================
    // TEST 7 - PILOT NOT FOUND
    // =====================================================

    @Test
    void ensureNonExistingPilotThrowsException() {

        when(companyRepo.findByCollaboratorEmail(any()))
                .thenReturn(Optional.of(company));

        when(pilotRepo.findByPilotId(any()))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.deactivatePilot(
                        new PilotID("INVALID"),
                        new Email("collab@aisafe.local")
                )
        );

        verify(txCtx).rollback();
    }

    // =====================================================
    // TEST 8 - COLLABORATOR WITHOUT COMPANY
    // =====================================================

    @Test
    void ensureCollaboratorMustBelongToCompany() {

        when(companyRepo.findByCollaboratorEmail(any()))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.deactivatePilot(
                        new PilotID("P001"),
                        new Email("collab@aisafe.local")
                )
        );

        verify(txCtx).rollback();
    }

    // =====================================================
    // TEST 9 - ROLLBACK ON SAVE FAILURE
    // =====================================================

    @Test
    void ensureRollbackOccursIfSaveFails() {

        defaultSuccessMocks();

        when(pilotRepo.save(any()))
                .thenThrow(new RuntimeException("DB ERROR"));

        assertThrows(
                RuntimeException.class,
                () -> service.deactivatePilot(
                        new PilotID("P001"),
                        new Email("collab@aisafe.local")
                )
        );

        verify(txCtx).rollback();
    }
}