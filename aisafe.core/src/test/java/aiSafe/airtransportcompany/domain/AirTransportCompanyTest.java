package aiSafe.airtransportcompany.domain;

import aiSafe.costumermanagement.domain.MemberType;
import aiSafe.costumermanagement.domain.Membership;
import aiSafe.costumermanagement.domain.Position;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.FullName;
import aiSafe.usermanagement.domain.PhoneNumber;
import aiSafe.usermanagement.domain.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AirTransportCompanyTest {

    @Test
    public void ensureAirTransportCompanyIsCreated() {
        // Arrange
        CompanyName companyName = CompanyName.valueOf("AirTest");
        IataCode iataCode = IataCode.valueOf("AT");
        IcaoCode icaoCode = IcaoCode.valueOf("ATC");

        final AirTransportCompany airTransportCompany = new AirTransportCompany(companyName, iataCode, icaoCode);

        // Act & Assert
        assertNotNull(airTransportCompany, "Air Transport Company should be created successfully");
    }

    @Test
    public void ensureAirTransportCompanyIsNotCreatedWithDuplicateIATACode() {
        // Arrange
        CompanyName companyName1 = CompanyName.valueOf("AirTest1");
        IataCode iataCode = IataCode.valueOf("AT");
        IcaoCode icaoCode1 = IcaoCode.valueOf("AT1");
        final AirTransportCompany airTransportCompany1 = new AirTransportCompany(companyName1, iataCode, icaoCode1);
        CompanyName companyName2 = CompanyName.valueOf("AirTest2");
        IcaoCode icaoCode2 = IcaoCode.valueOf("AT2");
        final AirTransportCompany airTransportCompany2 = new AirTransportCompany(companyName2, iataCode, icaoCode2);

        // Act & Assert
        assertNotNull(airTransportCompany1, "First Air Transport Company should be created successfully");
        assertTrue(airTransportCompany1.sameAs(airTransportCompany2), "Second Air Transport Company should be considered the same as the first due to duplicate IATA code");
    }

    @Test
    public void ensureAirTransportCompanyIsNotCreatedWithInvalidIATACode() {
        // Arrange
        CompanyName companyName = CompanyName.valueOf("AirTest");
        String invalidIataCode = "A";
        IcaoCode icaoCode = IcaoCode.valueOf("AT1");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new AirTransportCompany(companyName, IataCode.valueOf(invalidIataCode), icaoCode);
        }, "Creating an Air Transport Company with an invalid IATA code should throw an exception");
    }

    @Test
    public void ensureAirTransportCompanyIsNotCreatedWithDuplicateICAOCode() {
        // Arrange
        CompanyName companyName1 = CompanyName.valueOf("AirTest1");
        IataCode iataCode1 = IataCode.valueOf("AT");
        IcaoCode icaoCode1 = IcaoCode.valueOf("AT1");
        final AirTransportCompany airTransportCompany1 = new AirTransportCompany(companyName1, iataCode1, icaoCode1);
        CompanyName companyName2 = CompanyName.valueOf("AirTest2");
        IataCode iataCode2 = IataCode.valueOf("AT");
        final AirTransportCompany airTransportCompany2 = new AirTransportCompany(companyName2, iataCode2, icaoCode1);

        // Act & Assert
        assertNotNull(airTransportCompany1, "First Air Transport Company should be created successfully");
        assertTrue(airTransportCompany1.sameAs(airTransportCompany2), "Second Air Transport Company should be considered the same as the first due to duplicate ICAO code");
    }

    @Test
    public void ensureAirTransportCompanyIsNotCreatedWithInvalidICAOCode() {
        // Arrange
        CompanyName companyName = CompanyName.valueOf("AirTest");
        IataCode iataCode = IataCode.valueOf("AT");
        String invalidIcaoCode = "A";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new AirTransportCompany(companyName, iataCode, IcaoCode.valueOf(invalidIcaoCode));
        }, "Creating an Air Transport Company with an invalid ICAO code should throw an exception");
    }

    @Test
    public void ensureAirTransportCompanyIsNotCreatedWithBlankCompanyName() {
        // Arrange
        String blankCompanyName = "";
        IataCode iataCode = IataCode.valueOf("AT");
        IcaoCode icaoCode = IcaoCode.valueOf("AT1");
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new AirTransportCompany(CompanyName.valueOf(blankCompanyName), iataCode, icaoCode);
        }, "Creating an Air Transport Company with a blank company name should throw an exception");
    }

    @Test
    public void ensureAirTransportCompanyIsNotCreatedWithDuplicateCompanyName() {
        // Arrange
        CompanyName companyName = CompanyName.valueOf("AirTest");
        IataCode iataCode1 = IataCode.valueOf("AT");
        IcaoCode icaoCode1 = IcaoCode.valueOf("AT1");
        final AirTransportCompany airTransportCompany1 = new AirTransportCompany(companyName, iataCode1, icaoCode1);
        IataCode iataCode2 = IataCode.valueOf("AD");
        IcaoCode icaoCode2 = IcaoCode.valueOf("AT2");
        final AirTransportCompany airTransportCompany2 = new AirTransportCompany(companyName, iataCode2, icaoCode2);

        // Act & Assert
        assertNotNull(airTransportCompany1, "First Air Transport Company should be created successfully");
        assertTrue(airTransportCompany1.sameAs(airTransportCompany2), "Second Air Transport Company should be considered the same as the first due to duplicate company name!");
    }

    @Test
    public void ensureMembershipIsCreatedSuccessfullyForATC() {
        // Arrange
        CompanyName companyName = CompanyName.valueOf("AirTest");
        IataCode iataCode = IataCode.valueOf("AT");
        IcaoCode icaoCode = IcaoCode.valueOf("ATC");

        final AirTransportCompany airTransportCompany = new AirTransportCompany(companyName, iataCode, icaoCode);

        final var user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.PILOT
        );

        Position position = Position.valueOf("Chief Pilot");
        MemberType memberType = MemberType.PILOT;
        Membership membership = new Membership(user.email(), position, memberType);

        airTransportCompany.addMembership(membership);

        // Act & Assert
        assertNotNull(airTransportCompany.memberships(), "Membership should be created and added to the air transport company");
        assertTrue(airTransportCompany.memberships().contains(membership), "Membership should be associated with the air transport company");
    }

    @Test
    public void ensureCollaboratorHasCorrectRoleBasedOnCustomerType() {
        // Arrange
        CompanyName companyName = CompanyName.valueOf("AirTest");
        IataCode iataCode = IataCode.valueOf("AT");
        IcaoCode icaoCode = IcaoCode.valueOf("ATC");

        final AirTransportCompany airTransportCompany = new AirTransportCompany(companyName, iataCode, icaoCode);

        final var user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.PILOT
        );

        Position position = Position.valueOf("Chief Pilot");
        MemberType memberType = MemberType.FCO;
        Membership membership = new Membership(user.email(), position, memberType);
        MemberType memberType2 = MemberType.PILOT;
        Membership membership2 = new Membership(user.email(), position, memberType2);

        airTransportCompany.addMembership(membership2);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> airTransportCompany.addMembership(membership));
        assertNotNull(airTransportCompany.memberships(), "Membership should be created and added to the air transport company");
        assertTrue(airTransportCompany.memberships().contains(membership2), "Membership with correct role should be associated with the air transport company");
        assertFalse(airTransportCompany.memberships().contains(membership), "Membership with incorrect role should not be associated with the air transport company");
        assertEquals(1, airTransportCompany.memberships().size(), "Only one membership should be associated with the air transport company");
    }

    @Test
    public void ensureThatMembershipCannotBeAddedTwice() {
        // Arrange
        CompanyName companyName = CompanyName.valueOf("AirTest");
        IataCode iataCode = IataCode.valueOf("AT");
        IcaoCode icaoCode = IcaoCode.valueOf("ATC");

        final AirTransportCompany airTransportCompany = new AirTransportCompany(companyName, iataCode, icaoCode);

        final var user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.PILOT
        );

        Position position = Position.valueOf("Chief Pilot");
        MemberType memberType = MemberType.PILOT;
        Membership membership = new Membership(user.email(), position, memberType);

        airTransportCompany.addMembership(membership);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> airTransportCompany.addMembership(membership), "Adding the same membership twice should throw an exception");
        assertNotNull(airTransportCompany.memberships(), "Membership should be created and added to the air transport company");
        assertTrue(airTransportCompany.memberships().contains(membership), "Membership should be associated with the air transport company");
        assertEquals(1, airTransportCompany.memberships().size(), "Only one membership should be associated with the air transport company");
    }

    @Test
    public void ensureAtccMembershipIsAddedSuccessfully() {
        // Arrange
        CompanyName companyName = CompanyName.valueOf("AirTest");
        IataCode iataCode = IataCode.valueOf("AT");
        IcaoCode icaoCode = IcaoCode.valueOf("ATC");

        final AirTransportCompany airTransportCompany = new AirTransportCompany(companyName, iataCode, icaoCode);

        final var user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR
        );

        Position position = Position.valueOf("Controller");
        Membership membership = new Membership(user.email(), position, MemberType.ATCC);

        airTransportCompany.addMembership(membership);

        // Act & Assert
        assertTrue(airTransportCompany.memberships().contains(membership), "ATCC membership should be added successfully");
    }

    @Test
    public void ensureAddMembershipWithNullThrows() {
        // Arrange
        CompanyName companyName = CompanyName.valueOf("AirTest");
        IataCode iataCode = IataCode.valueOf("AT");
        IcaoCode icaoCode = IcaoCode.valueOf("ATC");

        final AirTransportCompany airTransportCompany = new AirTransportCompany(companyName, iataCode, icaoCode);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> airTransportCompany.addMembership(null),
                "Adding a null membership should throw an exception");
    }

    @Test
    public void ensureCustomerReferenceReturnsIataCode() {
        // Arrange
        CompanyName companyName = CompanyName.valueOf("AirTest");
        IataCode iataCode = IataCode.valueOf("AT");
        IcaoCode icaoCode = IcaoCode.valueOf("ATC");

        final AirTransportCompany airTransportCompany = new AirTransportCompany(companyName, iataCode, icaoCode);

        // Act & Assert
        assertEquals("AT", airTransportCompany.customerReference(), "Customer reference should return the IATA code");
    }

    @Test
    public void ensureDisplayNameReturnsCompanyName() {
        // Arrange
        CompanyName companyName = CompanyName.valueOf("AirTest");
        IataCode iataCode = IataCode.valueOf("AT");
        IcaoCode icaoCode = IcaoCode.valueOf("ATC");

        final AirTransportCompany airTransportCompany = new AirTransportCompany(companyName, iataCode, icaoCode);

        // Act & Assert
        assertEquals("AirTest", airTransportCompany.displayName(), "Display name should return the company name");
    }

    @Test
    public void ensureHasCollaboratorReturnsTrueWhenExists() {
        // Arrange
        CompanyName companyName = CompanyName.valueOf("AirTest");
        IataCode iataCode = IataCode.valueOf("AT");
        IcaoCode icaoCode = IcaoCode.valueOf("ATC");

        final AirTransportCompany airTransportCompany = new AirTransportCompany(companyName, iataCode, icaoCode);

        final var user = new User(
                new FullName("John", "Doe"),
                new Email("john.doe@company.com"),
                new PhoneNumber(351, 912345678),
                AisafeRoles.PILOT
        );

        Position position = Position.valueOf("Chief Pilot");
        Membership membership = new Membership(user.email(), position, MemberType.PILOT);
        airTransportCompany.addMembership(membership);

        // Act & Assert
        assertTrue(airTransportCompany.hasCollaborator(user.email()), "hasCollaborator should return true for an existing collaborator");
    }

    @Test
    public void ensureHasCollaboratorReturnsFalseWhenNotExists() {
        // Arrange
        CompanyName companyName = CompanyName.valueOf("AirTest");
        IataCode iataCode = IataCode.valueOf("AT");
        IcaoCode icaoCode = IcaoCode.valueOf("ATC");

        final AirTransportCompany airTransportCompany = new AirTransportCompany(companyName, iataCode, icaoCode);
        Email unknownEmail = new Email("unknown@company.com");

        // Act & Assert
        assertFalse(airTransportCompany.hasCollaborator(unknownEmail), "hasCollaborator should return false for a non-existing collaborator");
    }

    @Test
    public void ensureHasCollaboratorReturnsFalseForNullEmail() {
        // Arrange
        CompanyName companyName = CompanyName.valueOf("AirTest");
        IataCode iataCode = IataCode.valueOf("AT");
        IcaoCode icaoCode = IcaoCode.valueOf("ATC");

        final AirTransportCompany airTransportCompany = new AirTransportCompany(companyName, iataCode, icaoCode);

        // Act & Assert
        assertFalse(airTransportCompany.hasCollaborator(null), "hasCollaborator should return false for null email");
    }

    @Test
    public void ensureSameAsReturnsFalseWhenNoFieldMatches() {
        // Arrange
        final AirTransportCompany a = new AirTransportCompany(CompanyName.valueOf("Alpha"), IataCode.valueOf("AA"), IcaoCode.valueOf("ALP"));
        final AirTransportCompany b = new AirTransportCompany(CompanyName.valueOf("Beta"), IataCode.valueOf("BB"), IcaoCode.valueOf("BET"));

        // Act & Assert
        assertFalse(a.sameAs(b), "sameAs should return false when no fields match");
    }

    @Test
    public void ensureSameAsReturnsFalseForNullOrWrongType() {
        // Arrange
        final AirTransportCompany a = new AirTransportCompany(CompanyName.valueOf("Alpha"), IataCode.valueOf("AA"), IcaoCode.valueOf("ALP"));

        // Act & Assert
        assertFalse(a.sameAs(null));
        assertFalse(a.sameAs("not-a-company"), "sameAs should return false when comparing with null or wrong type");
    }

    @Test
    public void ensureIdentityIsNullBeforePersistence() {
        // Arrange
        final AirTransportCompany company = new AirTransportCompany(CompanyName.valueOf("Alpha"), IataCode.valueOf("AA"), IcaoCode.valueOf("ALP"));

        // Act & Assert
        assertNull(company.identity());
    }
}