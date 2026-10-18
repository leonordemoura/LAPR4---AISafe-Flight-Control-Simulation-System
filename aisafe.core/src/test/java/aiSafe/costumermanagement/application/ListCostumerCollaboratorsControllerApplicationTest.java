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
import aiSafe.costumermanagement.domain.MemberType;
import aiSafe.costumermanagement.domain.Membership;
import aiSafe.costumermanagement.domain.Position;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.application.AuthorizationGuardService;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.FullName;
import aiSafe.usermanagement.domain.PhoneNumber;
import aiSafe.usermanagement.domain.User;
import aiSafe.usermanagement.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Application-layer orchestration tests for {@link ListCostumerCollaboratorsController} (AAA + Mockito).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ListCostumerCollaboratorsControllerApplicationTest {

    @Mock
    private AuthorizationGuardService authorizationGuardService;

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private AirTransportCompanyRepository airTransportCompanyRepository;

    @Mock
    private AirControlAreaRepository airControlAreaRepository;

    @Mock
    private UserRepository userRepository;

    private ListCostumerCollaboratorsController controller;

    @BeforeEach
    void setUp() {
        when(repositoryFactory.getAirTransportCompanyRepository()).thenReturn(airTransportCompanyRepository);
        when(repositoryFactory.airControlAreas()).thenReturn(airControlAreaRepository);
        when(repositoryFactory.userManagementUsers()).thenReturn(userRepository);

        controller = new ListCostumerCollaboratorsController(authorizationGuardService, repositoryFactory);

        doNothing().when(authorizationGuardService).ensureAuthenticatedBackofficeUser();
    }

    private static AirTransportCompany companyWithCollaborator() {
        final AirTransportCompany company = new AirTransportCompany(
                CompanyName.valueOf("Tap Air"),
                IataCode.valueOf("TP"),
                IcaoCode.valueOf("TAP")
        );
        final User user = new User(
                new FullName("João", "Silva"),
                new Email("joao.silva@company.com"),
                new PhoneNumber(351, 912000000),
                AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR
        );
        company.addMembership(new Membership(user.email(), Position.valueOf("Ops Manager"), MemberType.ATCC));
        return company;
    }

    private static AirControlArea areaWithFco() {
        final GeoBoundary boundary = new GeoBoundary(List.of(
                new Coordinate(38.0, -10.0),
                new Coordinate(38.0, -8.0),
                new Coordinate(40.0, -10.0),
                new Coordinate(40.0, -8.0)
        ));
        final AirControlArea area = new AirControlArea(
                new AreaCode("LP"),
                new AreaName("Lisbon"),
                new MinFuelAtArrival(50),
                new MaxAltitude(5000),
                boundary
        );
        final User fco = new User(
                new FullName("Maria", "Costa"),
                new Email("maria.costa@aisafe.local"),
                new PhoneNumber(351, 913000000),
                AisafeRoles.FLIGHT_CONTROL_OPERATOR
        );
        area.addMembership(new Membership(fco.email(), Position.valueOf("FCO Lead"), MemberType.FCO));
        return area;
    }

    @Test
    void getAirTransportCompanies_delegates_to_repository() {
        // Arrange
        final List<AirTransportCompany> companies = new ArrayList<>();
        companies.add(companyWithCollaborator());
        when(airTransportCompanyRepository.findAll()).thenReturn(companies);

        // Act
        final List<AirTransportCompany> result = controller.getAirTransportCompanies();

        // Assert
        assertEquals(1, result.size());
        verify(authorizationGuardService).ensureAuthenticatedBackofficeUser();
    }

    @Test
    void listActiveCollaboratorsByCompany_throws_when_company_missing() {
        // Arrange
        when(airTransportCompanyRepository.findByIataCode(IataCode.valueOf("TP"))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.listActiveCollaboratorsByCompany("TP"));
    }

    @Test
    void listActiveCollaboratorsByCompany_maps_active_memberships() {
        // Arrange
        final AirTransportCompany company = companyWithCollaborator();
        when(airTransportCompanyRepository.findByIataCode(IataCode.valueOf("TP"))).thenReturn(Optional.of(company));
        final User user = new User(
                new FullName("João", "Silva"),
                new Email("joao.silva@company.com"),
                new PhoneNumber(351, 912000000),
                AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR
        );
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));

        // Act
        final List<ListCostumerCollaboratorsController.ActiveCollaborator> result =
                controller.listActiveCollaboratorsByCompany("TP");

        // Assert
        assertEquals(1, result.size());
        assertEquals("João Silva", result.getFirst().user().fullName().toString());
        assertEquals("joao.silva@company.com", result.getFirst().user().email().value());
        assertEquals("Ops Manager", result.getFirst().membership().position().position());
        assertEquals(MemberType.ATCC, result.getFirst().membership().memberType());
    }

    @Test
    void getAirControlAreas_returns_empty_list_when_none() {
        // Arrange
        when(airControlAreaRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        final List<AirControlArea> result = controller.getAirControlAreas();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void listActiveCollaboratorsByArea_returns_collaborators() {
        // Arrange
        final AirControlArea area = areaWithFco();
        when(airControlAreaRepository.findByAreaCode(new AreaCode("LP"))).thenReturn(Optional.of(area));
        final User fco = new User(
                new FullName("Maria", "Costa"),
                new Email("maria.costa@aisafe.local"),
                new PhoneNumber(351, 913000000),
                AisafeRoles.FLIGHT_CONTROL_OPERATOR
        );
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(fco));

        // Act
        final List<ListCostumerCollaboratorsController.ActiveCollaborator> result =
                controller.listActiveCollaboratorsByArea("LP");

        // Assert
        assertEquals(1, result.size());
        assertEquals(MemberType.FCO, result.getFirst().membership().memberType());

        verify(authorizationGuardService).ensureAuthenticatedBackofficeUser();
    }

    @Test
    void listActiveCollaboratorsByArea_throws_when_area_unknown() {
        // Arrange
        when(airControlAreaRepository.findByAreaCode(new AreaCode("XX"))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.listActiveCollaboratorsByArea("XX"));
    }
}
