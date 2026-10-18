package aiSafe.flightRoute.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * The RouteDesignator class represents the designator of a flight route, which consists of an origin and a destination airport. It is a value object that encapsulates the properties and behaviors related to the designator of a flight route, such as validation of the origin and destination IATA codes. The class is annotated with JPA annotations to enable persistence in a relational database, and it includes validation logic to ensure that the origin and destination are valid when creating instances of this class.
 * @author 2DE - Group 3
 * @version 2.0
 */
@Embeddable
public class RouteDesignator implements ValueObject, Serializable {

    @Column(name = "origin", nullable = false)
    private String origin;

    @Column (name = "destination", nullable = false)
    private String destination;

    /** Protected no-argument constructor required by JPA for entity instantiation. This constructor is used by the ORM framework to create instances of the RouteDesignator class when retrieving data from the database. It should not be used directly in application code, as it does not perform any validation or initialization of the properties of the RouteDesignator instance. */
    protected RouteDesignator() {}

    /**
     * Constructor for creating a new instance of the RouteDesignator class. It takes an origin and a destination as parameters and initializes the properties of the RouteDesignator instance. The constructor includes validation logic to ensure that the provided origin and destination are not empty, that they consist of exactly 3 letters (valid IATA codes), and that they are not the same. If any of the validations fail, an IllegalArgumentException will be thrown, preventing the creation of an invalid RouteDesignator instance.
     * @param origin The origin airport of the flight route, represented by its IATA code. The origin must be a non-empty string consisting of exactly 3 letters, which is the standard format for IATA codes. This validation ensures that the origin is properly defined and adheres to the expected format for airport codes in the system.
     * @param destination The destination airport of the flight route, represented by its IATA code. The destination must be a non-empty string consisting of exactly 3 letters, which is the standard format for IATA codes. Additionally, the destination cannot be the same as the origin, ensuring that the flight route is valid and represents a meaningful connection between two different airports. This validation ensures that the destination is properly defined and adheres to the expected format for airport codes in the system, while also maintaining the integrity of the flight route designator.
     */
    public RouteDesignator(String origin, String destination) {
        Preconditions.nonEmpty(origin, "Origin cannot be empty");
        Preconditions.nonEmpty(destination, "Destination cannot be empty");
        Preconditions.ensure(origin.matches("[A-Za-z]{3}"), "Origin must contain exactly 3 letters");
        Preconditions.ensure(destination.matches("[A-Za-z]{3}"), "Destination must contain exactly 3 letters");
        Preconditions.ensure(!origin.equalsIgnoreCase(destination), "Origin and destination cannot be the same");
        this.origin = origin.toUpperCase();
        this.destination = destination.toUpperCase();
    }

    /**
     * Factory method — creates a validated {@code RouteDesignator}.
     *
     * @param origin      the IATA code of the departure airport
     * @param destination the IATA code of the arrival airport
     * @return a new {@code RouteDesignator} instance
     * @throws IllegalArgumentException if validation fails
     */
    public static RouteDesignator valueOf(String origin, String destination) {
        return new RouteDesignator(origin, destination);
    }

    /**
     * Returns the IATA code of the origin airport.
     *
     * @return 3-letter uppercase origin code
     */
    public String origin() {
        return origin;
    }

    /**
     * Returns the IATA code of the destination airport.
     *
     * @return 3-letter uppercase destination code
     */
    public String destination() {
        return destination;
    }

    /**
     * Checks if this RouteDesignator is equal to another object. Two RouteDesignator instances are considered equal if they have the same origin and destination, regardless of case. The method first checks if the provided object is an instance of RouteDesignator, and if so, it compares the origin and destination properties of both instances for equality. This implementation ensures that two RouteDesignator instances with the same origin and destination are treated as equal, which is important for correctly managing collections of RouteDesignator objects and for ensuring consistent behavior when comparing instances in the application.
     * @param o the object to compare with this RouteDesignator instance. The method will check if the provided object is an instance of RouteDesignator and will compare their origin and destination properties for equality. If the provided object is not an instance of RouteDesignator, the method will return false, indicating that the two objects are not equal.
     * @return true if the provided object is an instance of RouteDesignator and has the same origin and destination as this instance, false otherwise. This implementation ensures that two RouteDesignator instances with the same origin and destination are treated as equal, which is important for correctly managing collections of RouteDesignator objects and for ensuring consistent behavior when comparing instances in the application.
     */
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof RouteDesignator that)) return false;
        return this.origin.equals(that.origin) && this.destination.equals(that.destination);
    }

    /**
     * Returns a hash code value for the RouteDesignator instance. The hash code is computed based on the origin and destination properties of the RouteDesignator, ensuring that two RouteDesignator instances with the same origin and destination will have the same hash code. This implementation is consistent with the equals method, which considers two RouteDesignator instances equal if they have the same origin and destination. By using the Objects.hash method, we can generate a hash code that combines the hash codes of both properties, providing a good distribution of hash codes for different RouteDesignator instances in collections such as HashSet or HashMap.
     * @return a hash code value for this RouteDesignator instance, computed based on the origin and destination properties. This implementation ensures that two RouteDesignator instances with the same origin and destination will have the same hash code, which is important for correctly managing collections of RouteDesignator objects and for ensuring consistent behavior when comparing instances in the application.
     */
    @Override
    public int hashCode() {
        return Objects.hash(origin, destination);
    }

    /**
     * Returns a human-readable representation in the form {@code OPO-LIS}.
     *
     * @return origin and destination separated by a hyphen
     */
    @Override
    public String toString() {
        return String.format("%s-%s", origin, destination);
    }
}