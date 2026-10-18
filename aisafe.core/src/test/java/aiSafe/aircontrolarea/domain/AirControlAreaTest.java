package aiSafe.aircontrolarea.domain;

import aiSafe.costumermanagement.domain.MemberType;
import aiSafe.costumermanagement.domain.Membership;
import aiSafe.costumermanagement.domain.Position;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.FullName;
import aiSafe.usermanagement.domain.PhoneNumber;
import aiSafe.usermanagement.domain.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// AirControlAreaTest
public class AirControlAreaTest {

    @Test
    public void ensureAirControlAreaCannotHaveNullAreaCode() {
        // Arrange
        AreaCode nullAreaCode = null;
        AreaName areaName = new AreaName("Lisbon Control Area");
        MinFuelAtArrival minFuel = new MinFuelAtArrival(100.0);
        MaxAltitude maxAltitude = new MaxAltitude(14000);
        GeoBoundary boundary = new GeoBoundary(List.of(
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 1)
        ));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new AirControlArea(nullAreaCode,areaName, minFuel, maxAltitude, boundary);
        });
    }

    @Test
    public void ensureAirControlAreaCannotHaveNullAreaName() {
        // Arrange
        AreaCode areaCode = new AreaCode("LPPT");
        AreaName nullAreaName = null;
        MinFuelAtArrival minFuel = new MinFuelAtArrival(100.0);
        MaxAltitude maxAltitude = new MaxAltitude(14000);
        GeoBoundary boundary = new GeoBoundary(List.of(
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 1)
        ));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new AirControlArea(areaCode,nullAreaName, minFuel, maxAltitude, boundary);
        });
    }

    @Test
    public void ensureAirControlAreaCannotHaveNullMinFuelAtArrival() {
        // Arrange
        AreaCode areaCode = new AreaCode("LPPT");
        AreaName areaName = new AreaName("Lisbon Control Area");
        MinFuelAtArrival nullMinFuel = null;
        MaxAltitude maxAltitude = new MaxAltitude(14000);
        GeoBoundary boundary = new GeoBoundary(List.of(
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 1)
        ));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new AirControlArea(areaCode, areaName, nullMinFuel, maxAltitude, boundary);
        });
    }

    @Test
    public void ensureAirControlAreaCannotHaveNullGeoBoundary() {
        // Arrange
        AreaCode areaCode = new AreaCode("LPPT");
        AreaName areaName = new AreaName("Lisbon Control Area");
        MinFuelAtArrival minFuel = new MinFuelAtArrival(100.0);
        MaxAltitude maxAltitude = new MaxAltitude(14000);
        GeoBoundary nullBoundary = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new AirControlArea(areaCode, areaName, minFuel, maxAltitude, nullBoundary);
        });
    }

    @Test
    public void ensureAirControlAreaCannotHaveNullMaxAltitude() {
        // Arrange
        AreaCode areaCode = new AreaCode("LPPT");
        AreaName areaName = new AreaName("Lisbon Control Area");
        MinFuelAtArrival minFuel = new MinFuelAtArrival(100.0);
        MaxAltitude nullMaxAltitude = null;
        GeoBoundary boundary = new GeoBoundary(List.of(
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 1)
        ));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new AirControlArea(areaCode, areaName, minFuel, nullMaxAltitude, boundary);
        });
    }

    @Test
    public void ensureAirControlAreaIsCreatedSuccessfully() {
        // Arrange
        AreaCode areaCode = new AreaCode("LPPT");
        AreaName areaName = new AreaName("Lisbon Control Area");
        MinFuelAtArrival minFuel = new MinFuelAtArrival(100.0);
        MaxAltitude maxAltitude = new MaxAltitude(14000);
        GeoBoundary boundary = new GeoBoundary(List.of(
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 1)
        ));

        // Act
        AirControlArea area = new AirControlArea(areaCode, areaName, minFuel, maxAltitude, boundary);

        // Assert
        assertEquals(areaCode, area.areaCode());
        assertEquals(minFuel, area.minFuelAtArrival());
        assertEquals(maxAltitude, area.maxAltitude());
        assertEquals(boundary, area.geoBoundary());
    }

    @Test
    public void ensureMembershipIsCreatedSuccessfullyForAACA() {
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

        final var user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.FLIGHT_CONTROL_OPERATOR
        );

        Position position = Position.valueOf("Chief Pilot");
        MemberType memberType = MemberType.FCO;
        Membership membership = new Membership(user.email(), position, memberType);

        area.addMembership(membership);

        // Act & Assert
        assertNotNull(area.memberships(), "Membership should be created and added to the air control area");
        assertTrue(area.memberships().contains(membership), "Membership should be associated with the air control area");
    }

    @Test
    public void ensureCollaboratorHasCorrectRoleBasedOnCustomerType() {
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

        final var user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.FLIGHT_CONTROL_OPERATOR
        );

        Position position = Position.valueOf("Chief Pilot");
        MemberType memberType = MemberType.FCO;
        Membership membership = new Membership(user.email(), position, memberType);
        MemberType memberType2 = MemberType.PILOT;
        Membership membership2 = new Membership(user.email(), position, memberType2);

        area.addMembership(membership);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> area.addMembership(membership2));
        assertNotNull(area.memberships(), "Membership should be created and added to the air transport company");
        assertTrue(area.memberships().contains(membership), "Membership with correct role should be associated with the air transport company");
        assertFalse(area.memberships().contains(membership2), "Membership with incorrect role should not be associated with the air transport company");
        assertEquals(1, area.memberships().size(), "Only one membership should be associated with the air transport company");
    }

    @Test
    public void ensureThatMembershipCannotBeAddedTwice() {
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

        final var user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.FLIGHT_CONTROL_OPERATOR
        );

        Position position = Position.valueOf("Chief Pilot");
        MemberType memberType = MemberType.FCO;
        Membership membership = new Membership(user.email(), position, memberType);

        area.addMembership(membership);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> area.addMembership(membership), "Adding the same membership twice should throw an exception");
        assertNotNull(area.memberships(), "Membership should be created and added to the air control area");
        assertTrue(area.memberships().contains(membership), "Membership should be associated with the air control area");
        assertEquals(1, area.memberships().size(), "Only one membership should be associated with the air control area");
    }

    @Test
    public void ensureAddMembershipWithNullThrows() {
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

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> area.addMembership(null),
                "Adding a null membership should throw an exception");
    }

    @Test
    public void ensureCustomerReferenceReturnsAreaCode() {
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

        // Act & Assert
        assertEquals(areaCode.value(), area.customerReference(), "Customer reference should return the area code");
    }

    @Test
    public void ensureDisplayNameReturnsAreaName() {
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

        // Act & Assert
        assertEquals(areaName.value(), area.displayName(), "Display name should return the area name");
    }

    @Test
    public void ensureHasCollaboratorReturnsTrueWhenExists() {
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

        final var user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.FLIGHT_CONTROL_OPERATOR
        );

        Membership membership = new Membership(user.email(), Position.valueOf("Controller"), MemberType.FCO);
        area.addMembership(membership);

        // Act & Assert
        assertTrue(area.hasCollaborator(user.email()), "hasCollaborator should return true for an existing collaborator");
    }

    @Test
    public void ensureHasCollaboratorReturnsFalseWhenNotExists() {
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
        Email unknownEmail = new Email("unknown@company.com");

        // Act & Assert
        assertFalse(area.hasCollaborator(unknownEmail), "hasCollaborator should return false for a non-existing collaborator");
    }

    @Test
    public void ensureHasCollaboratorReturnsFalseForNullEmail() {
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

        // Act & Assert
        assertFalse(area.hasCollaborator(null), "hasCollaborator should return false for null email");
    }

    @Test
    void ensureSameAs_rejects_non_area() {
        final AirControlArea area = rectangularAreaSample();
        assertFalse(area.sameAs("not-an-area"));
    }

    @Test
    void ensureSameAs_true_when_value_objects_match_and_identity_is_unknown() {
        final List<Coordinate> corners = List.of(
                new Coordinate(5, 5),
                new Coordinate(5, 7),
                new Coordinate(7, 7),
                new Coordinate(7, 5)
        );
        final AreaCode code = new AreaCode("AAC");
        final AreaName name = new AreaName("AAC Name");
        final MinFuelAtArrival fuel = new MinFuelAtArrival(50);
        final MaxAltitude altitude = new MaxAltitude(9000);
        final AirControlArea a = new AirControlArea(code, name, fuel, altitude, new GeoBoundary(corners));
        final AirControlArea b = new AirControlArea(code, name, fuel, altitude, new GeoBoundary(corners));
        assertTrue(a.sameAs(b));
        assertFalse(a.equals(b)); // persisted id not set
    }

    private static AirControlArea rectangularAreaSample() {
        final GeoBoundary boundary = new GeoBoundary(List.of(
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 1),
                new Coordinate(1, 0)
        ));
        return new AirControlArea(
                new AreaCode("LPPT"),
                new AreaName("Lisbon Control Area"),
                new MinFuelAtArrival(100.0),
                new MaxAltitude(14000),
                boundary
        );
    }
}