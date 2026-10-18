package aiSafe.costumermanagement.application;

import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircontrolarea.domain.AreaName;
import aiSafe.aircontrolarea.domain.Coordinate;
import aiSafe.aircontrolarea.domain.GeoBoundary;
import aiSafe.aircontrolarea.domain.MaxAltitude;
import aiSafe.aircontrolarea.domain.MinFuelAtArrival;
import aiSafe.aircontrolarea.repositories.AirControlAreaRepository;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.CompanyName;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.domain.IcaoCode;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.application.AuthorizationGuardService;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.FullName;
import aiSafe.usermanagement.domain.PhoneNumber;
import aiSafe.usermanagement.domain.User;
import aiSafe.usermanagement.repositories.UserRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Application-layer transactional tests for {@link AddCostumerCollaboratorController} (AAA + Mockito).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddCostumerCollaboratorControllerApplicationTest {

    @Mock
    private AuthorizationGuardService authorizationGuardService;

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private TransactionalContext transactionalContext;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AirTransportCompanyRepository airTransportCompanyRepository;

    @Mock
    private AirControlAreaRepository airControlAreaRepository;

    private MembershipFactory membershipFactory;

    private AddCostumerCollaboratorController controller;

    @BeforeEach
    void setUp() {
        membershipFactory = new MembershipFactory();
        when(repositoryFactory.newTransactionalContext()).thenReturn(transactionalContext);
        doNothing().when(transactionalContext).beginTransaction();
        doNothing().when(transactionalContext).commit();
        doNothing().when(transactionalContext).rollback();
        when(repositoryFactory.userManagementUsers(transactionalContext)).thenReturn(userRepository);

        doNothing().when(authorizationGuardService).ensureAuthenticatedBackofficeUser();

        controller = new AddCostumerCollaboratorController(
                authorizationGuardService,
                repositoryFactory,
                membershipFactory
        );
    }

    private static AirTransportCompany emptyCompany() {
        return new AirTransportCompany(
                CompanyName.valueOf("Acme Air"),
                IataCode.valueOf("AC"),
                IcaoCode.valueOf("ACM")
        );
    }

    private static AirControlArea emptyArea() {
        final GeoBoundary boundary = new GeoBoundary(List.of(
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 1),
                new Coordinate(1, 0)
        ));
        return new AirControlArea(
                new AreaCode("AR"),
                new AreaName("Area R"),
                new MinFuelAtArrival(10),
                new MaxAltitude(5000),
                boundary
        );
    }

    @Test
    void registerCostumerCollaborator_company_path_commits_transaction() {
        // Arrange
        final User pilot = new User(
                new FullName("Ana", "Aviator"),
                new Email("ana.aviator@company.com"),
                new PhoneNumber(351, 910000001),
                AisafeRoles.PILOT
        );
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(pilot));
        when(repositoryFactory.getAirTransportCompanyRepository(transactionalContext))
                .thenReturn(airTransportCompanyRepository);

        final AirTransportCompany company = emptyCompany();
        when(airTransportCompanyRepository.findByIataCode(IataCode.valueOf("AC")))
                .thenReturn(Optional.of(company));
        when(airTransportCompanyRepository.save(any(AirTransportCompany.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        final boolean ok = controller.registerCostumerCollaborator(1, "AC", "ana.aviator@company.com", "Captain");

        // Assert
        assertTrue(ok);
        verify(transactionalContext).commit();
        verify(transactionalContext, never()).rollback();
        verify(airTransportCompanyRepository).save(company);
    }

    @Test
    void registerCostumerCollaborator_area_path_commits_transaction() {
        // Arrange
        final User fco = new User(
                new FullName("Bruno", "Control"),
                new Email("bruno.control@aisafe.local"),
                new PhoneNumber(351, 910000002),
                AisafeRoles.FLIGHT_CONTROL_OPERATOR
        );
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(fco));
        when(repositoryFactory.airControlAreas(transactionalContext)).thenReturn(airControlAreaRepository);

        final AirControlArea area = emptyArea();
        when(airControlAreaRepository.findByAreaCode(new AreaCode("AR"))).thenReturn(Optional.of(area));
        when(airControlAreaRepository.save(any(AirControlArea.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        final boolean ok = controller.registerCostumerCollaborator(2, "AR", "bruno.control@aisafe.local", "Lead FCO");

        // Assert
        assertTrue(ok);
        verify(transactionalContext).commit();
        verify(airControlAreaRepository).save(area);
    }

    @Test
    void registerCostumerCollaborator_rolls_back_when_user_unknown() {
        // Arrange
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.registerCostumerCollaborator(1, "AC", "ghost@company.com", "X"));

        verify(transactionalContext).rollback();
        verify(transactionalContext, never()).commit();
    }

}