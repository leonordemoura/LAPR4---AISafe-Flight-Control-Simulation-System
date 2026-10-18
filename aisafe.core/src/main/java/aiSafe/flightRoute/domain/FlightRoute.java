package aiSafe.flightRoute.domain;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * The FlightRoute class represents a flight route in the system. It is an aggregate root that encapsulates the properties and behaviors related to a flight route, such as its name, designator, and active status. The class is annotated with JPA annotations to enable persistence in a relational database, and it includes validation logic to ensure that the properties of a flight route are valid when creating or modifying instances of this class.
 *
 * @author 2DE - Group 3
 * @version 1.0
 */
@Entity
@Table(
        name = "flight_route",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"route_prefix", "route_number"})
        }
)
public class FlightRoute implements AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "route_id")
    private Long routeID;

    @Embedded
    private RouteName routeName;

    @Embedded
    private RouteDesignator routeDesignator;

    @Column(name = "deactivationDate")
    private LocalDateTime deactivationDate;

    /**
     * Protected no-argument constructor required by JPA for entity instantiation. This constructor is used by the ORM framework to create instances of the FlightRoute class when retrieving data from the database. It should not be used directly in application code, as it does not perform any validation or initialization of the properties of the FlightRoute instance.
     */
    protected FlightRoute() {
        // for ORM
    }

    /**
     * Constructor for creating a new instance of the FlightRoute class. It takes a RouteName and a RouteDesignator as parameters and initializes the properties of the FlightRoute instance. The constructor includes validation logic to ensure that the provided route name and designator are not null and that the route name is not empty. If any of the validations fail, an IllegalArgumentException will be thrown, preventing the creation of an invalid FlightRoute instance.
     * @param routeName The name of the flight route, which consists of a prefix and a number. The route name must follow a specific format, with the prefix being exactly 2 uppercase letters and the number being between 1 and 9999. This validation ensures that the route name is consistent and adheres to the expected naming conventions for flight routes in the system.
     * @param routeDesignator The designator of the flight route, which includes the origin and destination airports. The designator must not be null, and it should contain valid IATA codes for both the origin and destination airports. This validation ensures that the flight route is properly defined with valid airport information, which is essential for the correct functioning of the application when managing flight routes and related operations.
     */
    public FlightRoute(RouteName routeName, RouteDesignator routeDesignator) {
        Preconditions.noneNull(routeName, routeDesignator);
        Preconditions.nonEmpty(routeName.toString(), "Route name cannot be empty");
        this.routeName = routeName;
        this.routeDesignator = routeDesignator;
        this.deactivationDate = null;
    }

    /**
     * Method to retrieve the name of the flight route. This method returns the RouteName object associated with the flight route, which contains the prefix and number that uniquely identify the route. The route name is an essential property of the flight route, as it is used to distinguish it from other routes in the system and to provide meaningful information about the route when displaying it to users or when performing operations related to flight routes.
     * @return The RouteName object representing the name of the flight route, which includes the prefix and number that uniquely identify the route. This information is crucial for managing and displaying flight routes in the application, as it provides a clear and consistent way to refer to specific routes based on their naming conventions.
     */
    public RouteName routeName() {
        return routeName;
    }

    /**
     * Method to retrieve the designator of the flight route. This method returns the RouteDesignator object associated with the flight route, which contains the origin and destination airports. The route designator is an important property of the flight route, as it defines the specific path that the flight takes between two airports. This information is essential for managing flight routes in the application, as it allows users to understand the origin and destination of each route and to perform operations related to flight routes based on their designators.
     * @return The RouteDesignator object representing the designator of the flight route, which includes the origin and destination airports. This information is crucial for managing and displaying flight routes in the application, as it provides a clear and consistent way to refer to specific routes based on their origin and destination, allowing users to easily identify and work with flight routes in the system.
     */
    public RouteDesignator routeDesignator() {
        return routeDesignator;
    }

    /**
     * Method to retrieve the deactivation date of the flight route. This method returns a LocalDateTime object representing the date and time when the flight route was deactivated. If the flight route is currently active, this method will return null, indicating that there is no deactivation date set for the route. The deactivation date is an important property of the flight route, as it allows users to understand when a route was deactivated and to manage flight routes based on their active status in the system.
     * @return A LocalDateTime object representing the deactivation date of the flight route. If the flight route is currently active, this method will return null, indicating that there is no deactivation date set for the route. This information is crucial for managing flight routes in the application, as it allows users to understand the status of each route and to perform operations based on whether a route is active or inactive in the system.
     */
    public LocalDateTime deactivationDate() {
        return deactivationDate;
    }

    /**
     * Method to check if the flight route is currently active. This method returns a boolean value indicating whether the flight route is active or not. The active status of a flight route is an important property that can be used to determine if the route is currently available for use in the system, such as when scheduling flights or when displaying available routes to users. By checking the active status, the application can ensure that only valid and operational flight routes are considered for various operations, helping to maintain the integrity and accuracy of the flight route data in the system.
     * @return A boolean value indicating whether the flight route is currently active. If the method returns true, it means that the flight route is active and can be used for scheduling flights or other operations in the system. If it returns false, it indicates that the flight route is inactive and should not be considered for use in the application, helping to ensure that only valid and operational routes are utilized in the system.
     */
    public boolean isActive() {
        return isActiveAt(LocalDateTime.now());
    }

    /**
     * Whether the route is active at the given instant (used when listing routes for a reference date).
     */
    public boolean isActiveAt(final LocalDateTime at) {
        return deactivationDate == null || deactivationDate.isAfter(at);
    }

    /**
     * Deactivates this flight route from the given date onwards.
     * The route remains active until that calendar day ends (inclusive for existing plans on that day).
     *
     * @param deactivationDate must be today or in the future (date component)
     */
    public void deactivate(final LocalDateTime deactivationDate) {
        if (deactivationDate == null) {
            throw new IllegalArgumentException("Deactivation date must not be null.");
        }
        if (deactivationDate.toLocalDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deactivation date must be today or in the future.");
        }
        if (!isActive()) {
            throw new IllegalStateException("Flight route is already deactivated.");
        }
        this.deactivationDate = deactivationDate;
    }

    /**
     * Static factory method to create a new instance of the FlightRoute class. This method takes a RouteName and a RouteDesignator as parameters and returns a new FlightRoute instance initialized with the provided values. The use of a static factory method allows for better encapsulation and control over the creation of FlightRoute instances, as it can include additional logic or validations if needed in the future without changing the public API of the class.
     * @param routeName The name of the flight route, which consists of a prefix and a number. The route name must follow a specific format, with the prefix being exactly 2 uppercase letters and the number being between 1 and 9999. This validation ensures that the route name is consistent and adheres to the expected naming conventions for flight routes in the system.
     * @param routeDesignator The designator of the flight route, which includes the origin and destination airports. The designator must not be null, and it should contain valid IATA codes for both the origin and destination airports. This validation ensures that the flight route is properly defined with valid airport information, which is essential for the correct functioning of the application when managing flight routes and related operations.
     * @return A new instance of the FlightRoute class initialized with the provided route name and designator. This method provides a convenient way to create FlightRoute instances while ensuring that the necessary validations are performed through the constructor, maintaining the integrity of the flight route data in the system.
     */
    public static FlightRoute valueOf(RouteName routeName, RouteDesignator routeDesignator) {
        return new FlightRoute(routeName, routeDesignator);
    }

    /**
     * {@inheritDoc}
     * @return The unique identifier of the flight route, which is the routeID property. This method is required by the AggregateRoot interface and is used to retrieve the identity of the aggregate root instance, allowing it to be managed and persisted correctly in the context of the application.
     */
    @Override
    public Long identity() {
        return routeID;
    }

    /**
     * Two {@code FlightRoute} instances are considered the same domain objec when they share the same {@link RouteName}.
     * @param other the object to compare with this instance
     * @return {@code true} if the other object is a {@code FlightRoute} with the same {@link RouteName}, {@code false} otherwise
     */
    @Override
    public boolean sameAs(Object other) {
        if (this == other) return true;
        if (!(other instanceof FlightRoute that)) return false;
        return this.routeName != null && this.routeName.equals(that.routeName);
    }
}
