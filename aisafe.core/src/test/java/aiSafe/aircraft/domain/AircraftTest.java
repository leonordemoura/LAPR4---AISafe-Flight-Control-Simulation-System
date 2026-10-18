package aiSafe.aircraft.domain;

import aiSafe.aircraftModel.domain.*;
import aiSafe.aircraftmanagement.domain.*;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.CompanyName;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.domain.IcaoCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AircraftTest {

    // =========================================================
    // AUX METHODS (builders)
    // =========================================================

    private Maker validMaker() {
        return new Maker(
                new MakerID(123L),
                new MakerName("Airbus"),
                new Country("France", 351)
        );
    }

    private FuelType validFuelType() {
        return new FuelType(
                new FuelTypeName("JET A"),
                new FuelTypeDensity(0.8),
                new FuelTypeSpecificEnergy(43.0)
        );
    }

    private EngineModel validEngineModel() {
        return new EngineModel(
                new EngineModelName("CFM56"),
                new Power(27000, Unit.HORSEPOWER),
                new TSFC(0.5),
                validFuelType(),
                MotorizationType.TURBOFAN,
                validMaker()
        );
    }

    private PhysicalSpecs validSpecs() {
        return new PhysicalSpecs(
                new Altitude(12000, AltitudeUnit.METERS),
                new Speed(850, SpeedUnit.KMH),
                new Area(122.6, AreaUnit.m2),
                new Weight(41000, WeightUnit.KG),
                new Weight(75000, WeightUnit.KG),
                new Weight(60000, WeightUnit.KG),
                new Weight(20000, WeightUnit.KG),
                new LiftCoefficient(0.5),
                new DragCoefficient(0.02)
        );
    }

    private AircraftModel validAircraftModel() {
        return new AircraftModel(
                new AircraftModelId("MODEL-001"),
                new AircraftModelName("A320"),
                validMaker(),
                List.of(validEngineModel()),
                validSpecs(),
                AircraftType.PASSENGER,
                new MaximumSeats(180)
        );
    }

    private AirTransportCompany validCompany() {
        return new AirTransportCompany(
                CompanyName.valueOf("TAP Air Portugal"),
                IataCode.valueOf("TP"),
                IcaoCode.valueOf("TAP")
        );
    }

    private Country validCountry() {
        return new Country("Portugal", 620);
    }

    private CabinConfiguration validCabinConfiguration() {
        return new CabinConfiguration(130, 20, 0);
    }

    private List<CurrentEngine> validEngines() {
        return List.of(CurrentEngine.valueOf("CFM56-SN-001"));
    }

    private Aircraft buildValidAircraft() {
        return new Aircraft(
                AircraftRegistration.valueOf("CS-TUA"),
                validAircraftModel(),
                validEngines(),
                validCabinConfiguration(),
                new RegistrationYear(2015),
                validCountry(),
                AircraftStatus.ACTIVE,
                validCompany()
        );
    }

    // =========================================================
    // TEST 1 - SUCCESS
    // =========================================================

    @Test
    void ensureAircraftIsCreatedSuccessfullyWithValidData() {

        final var aircraft = buildValidAircraft();

        assertNotNull(aircraft);
    }

    // =========================================================
    // TEST 2 - NULL FIELDS
    // =========================================================

    @Test
    void ensureMandatoryFieldsCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Aircraft(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        });
    }

    // =========================================================
    // TEST 3 - NO ENGINES
    // =========================================================

    @Test
    void ensureAircraftMustHaveAtLeastOneEngine() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Aircraft(
                    AircraftRegistration.valueOf("CS-TUB"),
                    validAircraftModel(),
                    List.of(), // empty
                    validCabinConfiguration(),
                    new RegistrationYear(2015),
                    validCountry(),
                    AircraftStatus.ACTIVE,
                    validCompany()
            );
        });
    }

    // =========================================================
    // TEST 4 - SEATS EXCEED MODEL CAPACITY
    // =========================================================

    @Test
    void ensureTotalSeatsMustNotExceedAircraftModelCapacity() {

        final var overloadedCabin = new CabinConfiguration(150, 80, 30); // 260 > 180

        assertThrows(IllegalArgumentException.class, () -> {
            new Aircraft(
                    AircraftRegistration.valueOf("CS-TUC"),
                    validAircraftModel(),
                    validEngines(),
                    overloadedCabin,
                    new RegistrationYear(2015),
                    validCountry(),
                    AircraftStatus.ACTIVE,
                    validCompany()
            );
        });
    }

    // =========================================================
    // TEST 5 - DECOMMISSION
    // =========================================================

    @Test
    void ensureAircraftCanBeDecommissioned() {

        final var aircraft = buildValidAircraft();

        aircraft.decommission();

        assertEquals(AircraftStatus.DECOMMISSIONED, aircraft.status());
    }

    // =========================================================
    // TEST 6 - DECOMMISSION ALREADY DECOMMISSIONED
    // =========================================================

    @Test
    void ensureDecommissioningAlreadyDecommissionedAircraftThrowsException() {

        final var aircraft = buildValidAircraft();
        aircraft.decommission();

        assertThrows(IllegalStateException.class, aircraft::decommission);
    }

    // =========================================================
    // TEST 7 - sameAs TRUE (same registration)
    // =========================================================

    @Test
    void ensureSameAsReturnsTrueForSameRegistration() {

        final var aircraft1 = buildValidAircraft();

        final var aircraft2 = new Aircraft(
                AircraftRegistration.valueOf("CS-TUA"), // same registration
                validAircraftModel(),
                validEngines(),
                validCabinConfiguration(),
                new RegistrationYear(2018),
                validCountry(),
                AircraftStatus.ACTIVE,
                validCompany()
        );

        assertTrue(aircraft1.sameAs(aircraft2));
    }

    // =========================================================
    // TEST 8 - sameAs FALSE
    // =========================================================

    @Test
    void ensureSameAsReturnsFalseForDifferentRegistration() {

        final var aircraft1 = buildValidAircraft();

        final var aircraft2 = new Aircraft(
                AircraftRegistration.valueOf("CS-TUB"),
                validAircraftModel(),
                validEngines(),
                validCabinConfiguration(),
                new RegistrationYear(2015),
                validCountry(),
                AircraftStatus.ACTIVE,
                validCompany()
        );

        assertFalse(aircraft1.sameAs(aircraft2));
    }

    // =========================================================
    // TEST 9 - sameAs DIFFERENT OBJECT TYPE
    // =========================================================

    @Test
    void ensureSameAsReturnsFalseForDifferentObjectType() {

        final var aircraft = buildValidAircraft();

        assertFalse(aircraft.sameAs("not an aircraft"));
    }

    // =========================================================
    // TEST 10 - GETTERS
    // =========================================================

    @Test
    void ensureGettersReturnCorrectValues() {

        final var aircraft = buildValidAircraft();

        assertEquals(AircraftRegistration.valueOf("CS-TUA"), aircraft.registration());
        assertNotNull(aircraft.aircraftModel());
        assertNotNull(aircraft.cabinConfiguration());
        assertNotNull(aircraft.country());
        assertNotNull(aircraft.company());
        assertEquals(AircraftStatus.ACTIVE, aircraft.status());
    }

    @Test
    void ensureEngineModelsGetterReturnsCorrectList() {

        final var aircraft = buildValidAircraft();

        assertEquals(1, aircraft.engineModels().size());
    }

    @Test
    void ensureEngineModelsListIsUnmodifiable() {

        final var aircraft = buildValidAircraft();

        assertThrows(UnsupportedOperationException.class, () ->
                aircraft.engineModels().add(CurrentEngine.valueOf("CFM56-SN-999"))
        );
    }

    @Test
    void ensureRegistrationYearGetterReturnsCorrectValue() {

        final var aircraft = buildValidAircraft();

        assertEquals(2015, aircraft.registrationYear().value());
    }

    @Test
    void ensureAgeReturnsPositiveValue() {

        final var aircraft = buildValidAircraft();

        assertTrue(aircraft.age() > 0);
    }
}