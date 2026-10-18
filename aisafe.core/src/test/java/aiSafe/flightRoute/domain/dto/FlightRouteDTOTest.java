package aiSafe.flightRoute.domain.dto;

import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlightRouteDTOTest {


    @Test
    void ensureFlightRouteDTOIsCreated(){
        // Arrange
        String routeName = "TP654";
        String originAirport = "OPO";
        String destinationAirport = "FAO";

        // Act
        final var flightRoutDTO = new FlightRouteDTO(routeName, originAirport, destinationAirport);

        // Assert
        assertNotNull(flightRoutDTO, "The DTO should be created");
    }

    @Test
    void ensureFlightRouteDTOHasCorrectValues() {
        // Arrange
        String routePrefix = "TP";
        int routeNumber = 654;
        String originAirport = "OPO";
        String destinationAirport = "FAO";

        // Act
        FlightRoute route = new FlightRoute(RouteName.valueOf(routePrefix, routeNumber), RouteDesignator.valueOf(originAirport, destinationAirport));
        final var flightRoutDTO = new FlightRouteDTO(route);

        // Assert
        assertNotNull(flightRoutDTO, "The DTO should be created");
    }

}