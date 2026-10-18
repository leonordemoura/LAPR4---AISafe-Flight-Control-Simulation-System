package aiSafe.flightRoute.domain.dto;

import aiSafe.flightRoute.domain.FlightRoute;
import eapli.framework.representations.dto.DTO;

/**
 * Data Transfer Object (DTO) for FlightRoute. This class is used to transfer data related to flight routes between different layers of the application, such as from the presentation layer to the application layer. It encapsulates the necessary information about a flight route, such as its name and designator, without exposing the internal structure of the domain model.
 * @author 2DE - Group 3
 * @version 1.0
 */
@DTO
public class FlightRouteDTO {
    /** The name of the flight route, which consists of a prefix and a number. The route name must follow a specific format, with the prefix being exactly 2 uppercase letters and the number being between 1 and 9999. This validation ensures that the route name is consistent and adheres to the expected naming conventions for flight routes in the system. */
    public String routeName;
    /** The IATA code of the origin airport for the flight route. The origin IATA code must be a non-empty string consisting of exactly 3 letters, which is the standard format for IATA codes. This validation ensures that the origin airport is properly defined and adheres to the expected format for airport codes in the system, allowing for accurate representation of the flight route's origin. */
    public String originIataCode;
    /** The IATA code of the destination airport for the flight route. The destination IATA code must be a non-empty string consisting of exactly 3 letters, which is the standard format for IATA codes. Additionally, the destination IATA code cannot be the same as the origin IATA code, ensuring that the flight route represents a valid connection between two different airports. This validation ensures that the destination airport is properly defined and adheres to the expected format for airport codes in the system, while also maintaining the integrity of the flight route designator. */
    public String destinationIataCode;

    /**
     * Constructor for creating a new instance of the FlightRouteDTO class. It takes the route name, origin IATA code, and destination IATA code as parameters and initializes the properties of the FlightRouteDTO instance. This constructor is used to create a DTO object that can be easily transferred between different layers of the application, allowing for a clear separation of concerns and encapsulation of the data related to flight routes.
     * @param routeName The name of the flight route, which consists of a prefix and a number. The route name must follow a specific format, with the prefix being exactly 2 uppercase letters and the number being between 1 and 9999. This validation ensures that the route name is consistent and adheres to the expected naming conventions for flight routes in the system.
     * @param originIataCode The IATA code of the origin airport for the flight route. The origin IATA code must be a non-empty string consisting of exactly 3 letters, which is the standard format for IATA codes. This validation ensures that the origin airport is properly defined and adheres to the expected format for airport codes in the system, allowing for accurate representation of the flight route's origin.
     * @param destinationIataCode The IATA code of the destination airport for the flight route. The destination IATA code must be a non-empty string consisting of exactly 3 letters, which is the standard format for IATA codes. Additionally, the destination IATA code cannot be the same as the origin IATA code, ensuring that the flight route represents a valid connection between two different airports. This validation ensures that the destination airport is properly defined and adheres to the expected format for airport codes in the system, while also maintaining the integrity of the flight route designator.
     */
    public FlightRouteDTO(String routeName, String originIataCode, String destinationIataCode) {
        this.routeName = routeName;
        this.originIataCode = originIataCode;
        this.destinationIataCode = destinationIataCode;
    }

    /**
     * Constructor for creating a new instance of the FlightRouteDTO class from a FlightRoute domain object. It takes a FlightRoute object as a parameter and initializes the properties of the FlightRouteDTO instance based on the values from the FlightRoute object. This constructor allows for easy conversion from the domain model to the DTO, facilitating the transfer of data related to flight routes between different layers of the application while maintaining a clear separation of concerns.
     * @param route The FlightRoute domain object from which the properties of the FlightRouteDTO instance will be initialized. The FlightRoute object contains the necessary information about a flight route, such as its name and designator, which will be used to populate the corresponding properties of the FlightRouteDTO instance. This constructor enables seamless integration between the domain model and the DTO, allowing for efficient data transfer and encapsulation of flight route information in the application.
     */
    public FlightRouteDTO(FlightRoute route) {
        this.routeName = route.routeName().toString();
        this.originIataCode = route.routeDesignator().origin();
        this.destinationIataCode = route.routeDesignator().destination();
    }
}
