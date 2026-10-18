package aiSafe.PilotCertification.domain;

import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.aircraftModel.domain.AircraftModelId;
import aiSafe.airtransportcompany.domain.CompanyName;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.domain.IcaoCode;
import aiSafe.usermanagement.domain.Email;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PilotCertificationTest {

    // =========================================================
    // TEST HELPERS
    // =========================================================

    private AirTransportCompany validCompany() {

        CompanyName companyName = CompanyName.valueOf("AirTest");
        IataCode iataCode = IataCode.valueOf("AT");
        IcaoCode icaoCode = IcaoCode.valueOf("ATC");

        return new AirTransportCompany(companyName, iataCode, icaoCode);
    }

    private ModelCertification validCertification() {
        return new ModelCertification(
                new AircraftModelId("A320-001"),
                LocalDate.of(2025, 12, 12)
        );
    }

    private PilotCertification buildValidPilot() {

        return new PilotCertification(
                new PilotID("P-001"),
                new Email("pilot@aisafe.local"),
                validCompany(),
                Set.of(validCertification())
        );
    }

    // =========================================================
    // TEST 1
    // =========================================================

    @Test
    void ensurePilotIsCreatedSuccessfullyWithValidData() {

        final var pilot = buildValidPilot();

        assertNotNull(pilot.identity());
        assertEquals("pilot@aisafe.local", pilot.userEmail().value());
        assertFalse(pilot.certifications().isEmpty());
        assertTrue(pilot.isActive());
    }

    // =========================================================
    // TEST 2
    // =========================================================

    @Test
    void ensurePilotMustHaveAtLeastOneCertification() {

        assertThrows(IllegalArgumentException.class, () ->
                new PilotCertification(
                        new PilotID("P-002"),
                        new Email("pilot@aisafe.local"),
                        validCompany(),
                        Set.of()
                )
        );
    }

    // =========================================================
    // TEST 3
    // =========================================================

    @Test
    void ensureMandatoryFieldsCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () ->
                new PilotCertification(
                        null,
                        null,
                        null,
                        null
                )
        );
    }

    // =========================================================
    // TEST 4
    // =========================================================

    @Test
    void ensurePilotCanBeDeactivated() {

        final var pilot = buildValidPilot();

        pilot.deactivate();

        assertFalse(pilot.isActive());
    }

    // =========================================================
    // TEST 5
    // =========================================================

    @Test
    void ensurePilotCanBeActivatedWhenHasCertifications() {

        final var pilot = buildValidPilot();

        pilot.deactivate();
        pilot.activate();

        assertTrue(pilot.isActive());
    }

    // =========================================================
    // TEST 6
    // =========================================================

    @Test
    void ensureSameAsReturnsTrueForSamePilotId() {

        final var pilot1 = new PilotCertification(
                new PilotID("P-001"),
                new Email("a@aisafe.local"),
                validCompany(),
                Set.of(validCertification())
        );

        final var pilot2 = new PilotCertification(
                new PilotID("P-001"),
                new Email("b@aisafe.local"),
                validCompany(),
                Set.of(validCertification())
        );

        assertTrue(pilot1.sameAs(pilot2));
    }

    // =========================================================
    // TEST 7
    // =========================================================

    @Test
    void ensureSameAsReturnsFalseForDifferentObject() {

        final var pilot = buildValidPilot();

        assertFalse(pilot.sameAs("not a pilot"));
    }

    // =========================================================
    // TEST 8
    // =========================================================

    @Test
    void ensureCertificationsAreImmutableExternally() {

        final var pilot = buildValidPilot();

        assertThrows(UnsupportedOperationException.class, () ->
                pilot.certifications().add(validCertification())
        );
    }

    // =========================================================
    // coverage
    // =========================================================

    @Test
    void ensureActivateWorksWhenCertificationsExist() {

        final var pilot = new PilotCertification(
                new PilotID("P-009"),
                new Email("pilot@aisafe.local"),
                validCompany(),
                Set.of(validCertification())
        );

        pilot.deactivate();
        pilot.activate();

        assertTrue(pilot.isActive());
    }

    @Test
    void ensureCompanyIsCorrectlyAssigned() {

        final var company = validCompany();

        final var pilot = new PilotCertification(
                new PilotID("P-010"),
                new Email("pilot@aisafe.local"),
                company,
                Set.of(validCertification())
        );

        assertEquals(company, pilot.company());
    }


    @Test
    void ensureIdentityReturnsCorrectPilotId() {

        final var id = new PilotID("P-011");

        final var pilot = new PilotCertification(
                id,
                new Email("pilot@aisafe.local"),
                validCompany(),
                Set.of(validCertification())
        );

        assertEquals(id, pilot.identity());
    }

    @Test
    void ensureSameAsReturnsFalseForDifferentPilotId() {

        final var pilot1 = new PilotCertification(
                new PilotID("P-001"),
                new Email("a@aisafe.local"),
                validCompany(),
                Set.of(validCertification())
        );

        final var pilot2 = new PilotCertification(
                new PilotID("P-002"),
                new Email("a@aisafe.local"),
                validCompany(),
                Set.of(validCertification())
        );

        assertFalse(pilot1.sameAs(pilot2));
    }
}