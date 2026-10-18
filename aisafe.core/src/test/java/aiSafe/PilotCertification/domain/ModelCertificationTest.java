package aiSafe.PilotCertification.domain;

import aiSafe.aircraftModel.domain.AircraftModelId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ModelCertificationTest {

    // =========================================================
    // TEST HELPERS
    // =========================================================

    private AircraftModelId validAircraftModel() {
        return new AircraftModelId("A320-001");
    }

    private ModelCertification buildValidCertification() {
        return new ModelCertification(
                validAircraftModel(),
                LocalDate.now().minusDays(1)
        );
    }

    // =========================================================
    // TEST 1
    // =========================================================

    @Test
    void ensureModelCertificationIsCreatedSuccessfully() {

        final var cert = buildValidCertification();

        assertNotNull(cert);
        assertEquals("A320-001", cert.aircraftModelId().toString());
        assertNotNull(cert.certificationDate());
    }

    // =========================================================
    // TEST 2
    // =========================================================

    @Test
    void ensureCertificationDateCannotBeInTheFuture() {

        assertThrows(IllegalArgumentException.class, () ->
                new ModelCertification(
                        validAircraftModel(),
                        LocalDate.now().plusDays(1)
                )
        );
    }

    // =========================================================
    // TEST 3
    // =========================================================

    @Test
    void ensureNullAircraftModelIsRejected() {

        assertThrows(IllegalArgumentException.class, () ->
                new ModelCertification(
                        null,
                        LocalDate.now()
                )
        );
    }

    // =========================================================
    // TEST 4
    // =========================================================

    @Test
    void ensureNullCertificationDateIsRejected() {

        assertThrows(IllegalArgumentException.class, () ->
                new ModelCertification(
                        validAircraftModel(),
                        null
                )
        );
    }

    // =========================================================
    // TEST 5
    // =========================================================

    @Test
    void ensureGettersReturnCorrectValues() {

        final var cert = buildValidCertification();

        assertEquals(validAircraftModel(), cert.aircraftModelId());
        assertEquals(LocalDate.now().minusDays(1), cert.certificationDate());
    }

    // =========================================================
    // TEST 6
    // =========================================================

    @Test
    void ensureCertificationDateEqualTodayIsAllowed() {

        final var cert = new ModelCertification(
                validAircraftModel(),
                LocalDate.now()
        );

        assertNotNull(cert);
    }
}