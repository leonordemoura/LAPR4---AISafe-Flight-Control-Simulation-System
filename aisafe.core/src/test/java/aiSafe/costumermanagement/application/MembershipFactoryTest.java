package aiSafe.costumermanagement.application;

import aiSafe.aircontrolarea.domain.*;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.costumermanagement.domain.Costumer;
import aiSafe.costumermanagement.domain.Membership;
import aiSafe.costumermanagement.domain.MemberType;
import aiSafe.costumermanagement.domain.Position;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.FullName;
import aiSafe.usermanagement.domain.PhoneNumber;
import aiSafe.usermanagement.domain.User;
import aiSafe.airtransportcompany.domain.CompanyName;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.domain.IcaoCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MembershipFactoryTest {

    private MembershipFactory factory;
    private Position position;

    @BeforeEach
    void setUp() {
        factory = new MembershipFactory();
        position = Position.valueOf("Chief Pilot");
    }


    @Test
    void ensureCreateAtcMembershipWithPilotRoleReturnsPilotMembership() {
        // Arrange
        User user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.PILOT
        );

        // Act
        Membership membership = factory.createAtcMembership(user.email(), user.assignedRole(), position);

        // Assert
        assertNotNull(membership, "Membership should not be null");
        assertEquals(MemberType.PILOT, membership.memberType(), "Member type should be PILOT");
        assertEquals(user.email(), membership.memberEmail(), "Member email should match");
    }

    @Test
    void ensureCreateAtcMembershipWithAtccRoleReturnsAtccMembership() {
        // Arrange
        User user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR
        );

        // Act
        Membership membership = factory.createAtcMembership(user.email(), user.assignedRole(), position);

        // Assert
        assertNotNull(membership, "Membership should not be null");
        assertEquals(MemberType.ATCC, membership.memberType(), "Member type should be ATCC");
        assertEquals(user.email(), membership.memberEmail(), "Member email should match");
    }

    @Test
    void ensureCreateAtcMembershipWithInvalidRoleThrows() {
        // Arrange
        User user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.FLIGHT_CONTROL_OPERATOR
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> factory.createAtcMembership(user.email(), user.assignedRole(), position),
                "Creating ATC membership with FCO role should throw IllegalArgumentException");
    }


    @Test
    void ensureCreateAreaMembershipWithPilotRoleThrows() {
        // Arrange
        User user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.PILOT
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> factory.createAreaMembership(user.email(), user.assignedRole(), position),
                "Creating area membership with FCO role should throw IllegalArgumentException");
    }

    @Test
    void ensureCreateAreaMembershipWithValidRoleReturnsFcoMembership() {
        // Arrange
        User user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.FLIGHT_CONTROL_OPERATOR
        );

        // Act
        Membership membership = factory.createAreaMembership(user.email(), user.assignedRole(), position);

        // Assert
        assertNotNull(membership, "Membership should not be null");
        assertEquals(MemberType.FCO, membership.memberType(), "Member type should be FCO");
        assertEquals(user.email(), membership.memberEmail(), "Member email should match");
    }

    @Test
    void ensureCreateMembershipForAirTransportCompanyDoesNotThrow() {
        // Arrange
        AirTransportCompany company = new AirTransportCompany(
                CompanyName.valueOf("AirTest"),
                IataCode.valueOf("AT"),
                IcaoCode.valueOf("ATC")
        );
        User user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.PILOT
        );

        // Act & Assert
        assertDoesNotThrow(() -> factory.createMembership(company, user, position),
                "Creating membership for AirTransportCompany should not throw");
    }

    @Test
    void ensureCreateMembershipForAirControlAreaDoesNotThrow() {
        // Arrange
        AreaCode areaCode = new AreaCode("LPPT");
        AreaName areaName = new AreaName("Lisbon Control Area");
        MinFuelAtArrival minFuel = new MinFuelAtArrival(100.0);
        GeoBoundary boundary = new GeoBoundary(List.of(
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 1)
        ));

        AirControlArea area = new AirControlArea(areaCode, areaName, minFuel, new MaxAltitude(14000), boundary);

        User user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.FLIGHT_CONTROL_OPERATOR
        );

        // Act & Assert
        assertDoesNotThrow(() -> factory.createMembership(area, user, position),
                "Creating membership for AirControlArea should not throw");
    }

    @Test
    void ensureCreateMembershipForUnknownCustomerTypeThrows() {
        final Costumer other = new Costumer() {
            @Override
            public String customerReference() {
                return "other";
            }

            @Override
            public String displayName() {
                return "Other";
            }

            @Override
            public boolean hasCollaborator(final Email email) {
                return false;
            }

            @Override
            public void addMembership(final Membership membership) {
                throw new UnsupportedOperationException();
            }
        };
        final User user = new User(
                new FullName("Test", "User"),
                new Email("test@company.com"),
                new PhoneNumber(351, 919999999),
                AisafeRoles.PILOT
        );

        assertThrows(IllegalArgumentException.class,
                () -> factory.createMembership(other, user, position),
                "Unknown customer implementation should reject membership creation.");
    }
}