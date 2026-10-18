package aiSafe.airport.domain;

import aiSafe.aircontrolarea.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AirportTest {

    private AirControlArea validArea() {

        return new AirControlArea(
                new AreaCode("PT01"),
                new AreaName("Portugal North"),
                new MinFuelAtArrival(30),
                new MaxAltitude(14000),
                new GeoBoundary(List.of(
                        new Coordinate(0, 0),
                        new Coordinate(1, 0),
                        new Coordinate(1, 1),
                        new Coordinate(0, 1)
                ))
        );
    }

    private Airport buildValidAirport() {
        return new Airport(
                new IataCode_Airport("OPO"),
                new IcaoCode_Airport("LPPR"),
                new AirportName("Porto Airport"),
                new Town("Porto"),
                new Country_Airport("Portugal", "PT"),
                new Elevation(70.0),
                new Coordinate_Airport(41.242, -8.678),
                validArea()
        );
    }

    // =========================================================
    // TEST 1
    // =========================================================

    @Test
    void ensureAirportIsCreatedSuccessfullyWithValidData() {

        final var airport = buildValidAirport();

        assertNotNull(airport);
    }

    // =========================================================
    // TEST 2
    // =========================================================

    @Test
    void ensureInvalidIataCodeIsRejected() {

        final var area = validArea();

        assertThrows(IllegalArgumentException.class, () -> {
            new Airport(
                    new IataCode_Airport("OP"),
                    new IcaoCode_Airport("LPPT"),
                    new AirportName("Lisboa Airport"),
                    new Town("Lisboa"),
                    new Country_Airport("Portugal", "PT"),
                    new Elevation(20.0),
                    new Coordinate_Airport(38.774, -9.134),
                    area
            );
        });
    }

    // =========================================================
    // TEST 3
    // =========================================================

    @Test
    void ensureInvalidIcaoCodeIsRejected() {

        final var area = validArea();

        assertThrows(IllegalArgumentException.class, () -> {
            new Airport(
                    new IataCode_Airport("LIS"),
                    new IcaoCode_Airport("LPP"),
                    new AirportName("Lisboa Airport 2"),
                    new Town("Lisboa"),
                    new Country_Airport("Portugal", "PT"),
                    new Elevation(25.0),
                    new Coordinate_Airport(38.781, -9.135),
                    area
            );
        });
    }

    // =========================================================
    // TEST 4
    // =========================================================

    @Test
    void ensureInvalidCoordinatesAreRejected() {

        final var area = validArea();

        assertThrows(IllegalArgumentException.class, () -> {
            new Airport(
                    new IataCode_Airport("BRG"),
                    new IcaoCode_Airport("LPBR"),
                    new AirportName("Braga Airport"),
                    new Town("Braga"),
                    new Country_Airport("Portugal", "PT"),
                    new Elevation(90.0),
                    new Coordinate_Airport(100.0, -8.600),
                    area
            );
        });
    }

    // =========================================================
    // TEST 5
    // =========================================================

    @Test
    void ensureInvalidElevationIsRejected() {

        final var area = validArea();

        assertThrows(IllegalArgumentException.class, () -> {
            new Airport(
                    new IataCode_Airport("LON"),
                    new IcaoCode_Airport("EGLL"),
                    new AirportName("London Heathrow"),
                    new Town("London"),
                    new Country_Airport("United Kingdom", "UK"),
                    null,
                    new Coordinate_Airport(51.470, -0.454),
                    area
            );
        });
    }

    // =========================================================
    // TEST 6
    // =========================================================

    @Test
    void ensureAirportMustHaveAirControlArea() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Airport(
                    new IataCode_Airport("MAD"),
                    new IcaoCode_Airport("LEMD"),
                    new AirportName("Madrid Airport"),
                    new Town("Madrid"),
                    new Country_Airport("Spain", "ES"),
                    new Elevation(610.0),
                    new Coordinate_Airport(40.472, -3.561),
                    null
            );
        });
    }

    // =========================================================
    // TEST 7
    // =========================================================

    @Test
    void ensureAirportNameCannotBeEmpty() {

        final var area = validArea();

        assertThrows(IllegalArgumentException.class, () -> {
            new Airport(
                    new IataCode_Airport("MAD"),
                    new IcaoCode_Airport("LEBL"),
                    new AirportName(""),
                    new Town("Barcelona"),
                    new Country_Airport("Spain", "ES"),
                    new Elevation(12.0),
                    new Coordinate_Airport(41.297, 2.083),
                    area
            );
        });
    }

    // =========================================================
    // TEST 8
    // =========================================================

    @Test
    void ensureTownCannotBeEmpty() {

        final var area = validArea();

        assertThrows(IllegalArgumentException.class, () -> {
            new Airport(
                    new IataCode_Airport("PAR"),
                    new IcaoCode_Airport("LFPG"),
                    new AirportName("Paris Airport"),
                    new Town(""),
                    new Country_Airport("France", "FR"),
                    new Elevation(119.0),
                    new Coordinate_Airport(49.009, 2.547),
                    area
            );
        });
    }

    // =========================================================
    // TEST 9
    // =========================================================

    @Test
    void ensureCountryMustBeValid() {

        final var area = validArea();

        assertThrows(IllegalArgumentException.class, () -> {
            new Airport(
                    new IataCode_Airport("BER"),
                    new IcaoCode_Airport("EDDB"),
                    new AirportName("Berlin Airport"),
                    new Town("Berlin"),
                    new Country_Airport("", ""),
                    new Elevation(48.0),
                    new Coordinate_Airport(52.366, 13.503),
                    area
            );
        });
    }

    // =========================================================
    // EXTRA TESTS
    // =========================================================

    @Test
    void ensureMandatoryFieldsCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Airport(
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

    @Test
    void ensureGettersReturnCorrectValues() {

        final var airport = buildValidAirport();

        assertEquals("OPO", airport.iataCode().toString());
        assertEquals("LPPR", airport.icaoCode().toString());
        assertEquals("Porto Airport", airport.airportName().toString());
        assertEquals("Porto", airport.town().toString());
        assertNotNull(airport.country());
        assertNotNull(airport.coordinate());
        assertNotNull(airport.elevation());
        assertNotNull(airport.airControlArea());
    }

    @Test
    void ensureSameAsReturnsTrueForSameIata() {

        final var area = validArea();

        final var airport1 = new Airport(
                new IataCode_Airport("OPO"),
                new IcaoCode_Airport("LPPR"),
                new AirportName("Airport 1"),
                new Town("Porto"),
                new Country_Airport("Portugal", "PT"),
                new Elevation(70.0),
                new Coordinate_Airport(41.1, -8.6),
                area
        );

        final var airport2 = new Airport(
                new IataCode_Airport("OPO"),
                new IcaoCode_Airport("XXXX"),
                new AirportName("Airport 2"),
                new Town("Lisbon"),
                new Country_Airport("Portugal", "PT"),
                new Elevation(50.0),
                new Coordinate_Airport(40.0, -8.0),
                area
        );

        assertTrue(airport1.sameAs(airport2));
    }

    @Test
    void ensureSameAsReturnsTrueForSameIcao() {

        final var area = validArea();

        final var airport1 = new Airport(
                new IataCode_Airport("AAA"),
                new IcaoCode_Airport("LPPR"),
                new AirportName("Airport 1"),
                new Town("Porto"),
                new Country_Airport("Portugal", "PT"),
                new Elevation(70.0),
                new Coordinate_Airport(41.1, -8.6),
                area
        );

        final var airport2 = new Airport(
                new IataCode_Airport("BBB"),
                new IcaoCode_Airport("LPPR"),
                new AirportName("Airport 2"),
                new Town("Lisbon"),
                new Country_Airport("Portugal", "PT"),
                new Elevation(50.0),
                new Coordinate_Airport(40.0, -8.0),
                area
        );

        assertTrue(airport1.sameAs(airport2));
    }

    @Test
    void ensureSameAsReturnsFalseForDifferentAirports() {

        final var area = validArea();

        final var airport1 = new Airport(
                new IataCode_Airport("OPO"),
                new IcaoCode_Airport("LPPR"),
                new AirportName("Airport 1"),
                new Town("Porto"),
                new Country_Airport("Portugal", "PT"),
                new Elevation(70.0),
                new Coordinate_Airport(41.1, -8.6),
                area
        );

        final var airport2 = new Airport(
                new IataCode_Airport("LIS"),
                new IcaoCode_Airport("LPPT"),
                new AirportName("Airport 2"),
                new Town("Lisbon"),
                new Country_Airport("Portugal", "PT"),
                new Elevation(50.0),
                new Coordinate_Airport(40.0, -8.0),
                area
        );

        assertFalse(airport1.sameAs(airport2));
    }

    @Test
    void ensureSameAsReturnsFalseForDifferentObjectType() {

        final var airport = buildValidAirport();

        assertFalse(airport.sameAs("not an airport"));
    }

    @Test
    void ensureIdentityIsNullBeforePersistence() {

        final var airport = buildValidAirport();

        assertNull(airport.identity());
    }

    @Test
    void ensureSameAsReturnsFalseForNull() {

        final var airport = buildValidAirport();

        assertFalse(airport.sameAs(null));
    }

    @Test
    void ensureAllGettersReturnNonNullValues() {

        final var airport = buildValidAirport();

        assertNotNull(airport.iataCode());
        assertNotNull(airport.icaoCode());
        assertNotNull(airport.airportName());
        assertNotNull(airport.town());
        assertNotNull(airport.country());
        assertNotNull(airport.elevation());
        assertNotNull(airport.coordinate());
        assertNotNull(airport.airControlArea());
    }

    @Test
    void ensureIdentityReturnsNullWhenNotPersisted() {

        final var airport = buildValidAirport();

        assertNull(airport.identity());
    }

    @Test
    void ensureSameAsReturnsFalseWhenBothCodesAreDifferent() {

        final var area = validArea();

        final var a1 = new Airport(
                new IataCode_Airport("OPO"),
                new IcaoCode_Airport("LPPR"),
                new AirportName("A1"),
                new Town("Porto"),
                new Country_Airport("Portugal", "PT"),
                new Elevation(10.0),
                new Coordinate_Airport(1,1),
                area
        );

        final var a2 = new Airport(
                new IataCode_Airport("LIS"),
                new IcaoCode_Airport("LPPT"),
                new AirportName("A2"),
                new Town("Lisbon"),
                new Country_Airport("Portugal", "PT"),
                new Elevation(10.0),
                new Coordinate_Airport(1,1),
                area
        );

        assertFalse(a1.sameAs(a2));
    }
}