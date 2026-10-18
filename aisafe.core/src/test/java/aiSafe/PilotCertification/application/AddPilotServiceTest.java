package aiSafe.PilotCertification.application;

import aiSafe.PilotCertification.domain.*;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.aircraftModel.domain.AircraftModel;
import aiSafe.aircraftModel.domain.AircraftModelId;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.User;

import eapli.framework.domain.repositories.TransactionalContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddPilotServiceTest {

    @Mock private RepositoryFactory factory;
    @Mock private TransactionalContext txCtx;

    @Mock private aiSafe.usermanagement.repositories.UserRepository userRepo;
    @Mock private aiSafe.PilotCertification.repositories.PilotCertificationRepository pilotRepo;
    @Mock private aiSafe.aircraftModel.repositories.AircraftModelRepository aircraftRepo;
    @Mock private aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository companyRepo;

    @Mock private User user;
    @Mock private AirTransportCompany company;
    @Mock private AircraftModel aircraftModel;

    private AddPilotService service;

    // =====================================================
    // SETUP
    // =====================================================

    @BeforeEach
    void setUp() {

        when(factory.newTransactionalContext()).thenReturn(txCtx);
        when(factory.userManagementUsers(txCtx)).thenReturn(userRepo);
        when(factory.pilots(txCtx)).thenReturn(pilotRepo);
        when(factory.aircraftModels(txCtx)).thenReturn(aircraftRepo);
        when(factory.getAirTransportCompanyRepository(txCtx)).thenReturn(companyRepo);

        doNothing().when(txCtx).beginTransaction();
        doNothing().when(txCtx).commit();
        doNothing().when(txCtx).rollback();

        service = new AddPilotService(factory);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private Email collaboratorEmail() {
        return new Email("collab@aisafe.local");
    }

    private List<ModelCertification> validCerts() {
        return List.of(
                new ModelCertification(
                        new AircraftModelId("A320"),
                        LocalDate.of(2025, 1, 1)
                )
        );
    }

    private void defaultMocks() {

        when(companyRepo.findByCollaboratorEmail(any()))
                .thenReturn(Optional.of(company));

        when(pilotRepo.ofIdentity(any())).thenReturn(Optional.empty());
        when(pilotRepo.findByUserEmail(any())).thenReturn(Optional.empty());

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(user.assignedRole()).thenReturn(AisafeRoles.PILOT);

        when(aircraftRepo.ofIdentity(any()))
                .thenReturn(Optional.of(aircraftModel));

        when(pilotRepo.save(any()))
                .thenAnswer(i -> i.getArgument(0));
    }

    // =====================================================
    // TEST 1 - SUCCESS
    // =====================================================

    @Test
    void ensurePilotIsCreatedSuccessfully() {

        defaultMocks();

        var result = service.execute(
                "P-001",
                "pilot@aisafe.local",
                validCerts(),
                collaboratorEmail()
        );

        assertNotNull(result);
        verify(pilotRepo).save(any(PilotCertification.class));
        verify(txCtx).commit();
    }

    // =====================================================
    // TEST 2 - COMPANY NOT FOUND
    // =====================================================

    @Test
    void ensureCollaboratorMustBelongToCompany() {

        when(companyRepo.findByCollaboratorEmail(any()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.execute(
                        "P-001",
                        "pilot@aisafe.local",
                        validCerts(),
                        collaboratorEmail()
                )
        );

        verify(txCtx).rollback();
    }

    // =====================================================
    // TEST 3 - USER NOT FOUND
    // =====================================================

    @Test
    void ensureUserMustExist() {

        when(companyRepo.findByCollaboratorEmail(any()))
                .thenReturn(Optional.of(company));

        when(userRepo.findByEmail(any()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.execute(
                        "P-001",
                        "pilot@aisafe.local",
                        validCerts(),
                        collaboratorEmail()
                )
        );

        verify(txCtx).rollback();
    }

    // =====================================================
    // TEST 4 - WRONG ROLE
    // =====================================================

    @Test
    void ensureUserMustBePilotRole() {

        when(companyRepo.findByCollaboratorEmail(any()))
                .thenReturn(Optional.of(company));

        when(userRepo.findByEmail(any()))
                .thenReturn(Optional.of(user));

        when(user.assignedRole()).thenReturn(AisafeRoles.ADMIN);

        assertThrows(IllegalArgumentException.class, () ->
                service.execute(
                        "P-001",
                        "pilot@aisafe.local",
                        validCerts(),
                        collaboratorEmail()
                )
        );
    }

    // =====================================================
    // TEST 5 - DUPLICATE PILOT ID
    // =====================================================

    @Test
    void ensurePilotIdMustBeUnique() {

        when(companyRepo.findByCollaboratorEmail(any()))
                .thenReturn(Optional.of(company));

        when(pilotRepo.ofIdentity(any()))
                .thenReturn(Optional.of(mock(PilotCertification.class)));

        assertThrows(IllegalArgumentException.class, () ->
                service.execute(
                        "P-001",
                        "pilot@aisafe.local",
                        validCerts(),
                        collaboratorEmail()
                )
        );
    }

    // =====================================================
    // TEST 6 - DUPLICATE EMAIL
    // =====================================================

    @Test
    void ensurePilotEmailMustBeUnique() {

        when(companyRepo.findByCollaboratorEmail(any()))
                .thenReturn(Optional.of(company));

        when(pilotRepo.ofIdentity(any())).thenReturn(Optional.empty());
        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(user.assignedRole()).thenReturn(AisafeRoles.PILOT);

        when(pilotRepo.findByUserEmail(any()))
                .thenReturn(Optional.of(mock(PilotCertification.class)));

        assertThrows(IllegalArgumentException.class, () ->
                service.execute(
                        "P-001",
                        "pilot@aisafe.local",
                        validCerts(),
                        collaboratorEmail()
                )
        );
    }

    // =====================================================
    // TEST 7 - INVALID CERTIFICATIONS
    // =====================================================

    @Test
    void ensureAircraftMustExist() {

        when(companyRepo.findByCollaboratorEmail(any()))
                .thenReturn(Optional.of(company));

        when(pilotRepo.ofIdentity(any())).thenReturn(Optional.empty());

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(user.assignedRole()).thenReturn(AisafeRoles.PILOT);

        when(aircraftRepo.ofIdentity(any()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.execute(
                        "P-001",
                        "pilot@aisafe.local",
                        validCerts(),
                        collaboratorEmail()
                )
        );
    }

    // =====================================================
    // TEST 8 - NULL CERTIFICATIONS
    // =====================================================

    @Test
    void ensurePilotMustHaveCertifications() {

        when(companyRepo.findByCollaboratorEmail(any()))
                .thenReturn(Optional.of(company));

        assertThrows(IllegalArgumentException.class, () ->
                service.execute(
                        "P-001",
                        "pilot@aisafe.local",
                        null,
                        collaboratorEmail()
                )
        );
    }

    // =====================================================
    // TEST 9 - ROLLBACK ON SAVE FAILURE
    // =====================================================

    @Test
    void ensureRollbackOccursIfSaveFails() {

        defaultMocks();

        when(pilotRepo.save(any()))
                .thenThrow(new RuntimeException("DB ERROR"));

        assertThrows(RuntimeException.class, () ->
                service.execute(
                        "P-001",
                        "pilot@aisafe.local",
                        validCerts(),
                        collaboratorEmail()
                )
        );

        verify(txCtx).rollback();
    }
}