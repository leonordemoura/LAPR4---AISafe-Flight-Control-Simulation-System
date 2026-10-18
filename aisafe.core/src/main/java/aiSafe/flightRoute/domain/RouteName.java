package aiSafe.flightRoute.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * The RouteName class represents the name of a flight route, which consists of a prefix and a number. It is a value object that encapsulates the properties and behaviors related to the name of a flight route, such as validation of the prefix and number. The class is annotated with JPA annotations to enable persistence in a relational database, and it includes validation logic to ensure that the prefix and number are valid when creating instances of this class.
 * @author 2DE - Group 3
 * @version 2.0
 */
@Embeddable
public class RouteName implements ValueObject, Serializable {

    @Column(name = "route_prefix", nullable = false)
    private String value;

    @Column(name = "route_number", nullable = false)
    private int routeNumber;

    /**
     * Protected no-argument constructor required by JPA for entity instantiation. This constructor is used by the ORM framework to create instances of the RouteName class when retrieving data from the database. It should not be used directly in application code, as it does not perform any validation or initialization of the properties of the RouteName instance.
     */
    protected RouteName() {}

    /**
     * Constructor for creating a new instance of the RouteName class. It takes a prefix and a number as parameters and initializes the properties of the RouteName instance. The constructor includes validation logic to ensure that the provided prefix is not null or empty, that it consists of exactly 2 uppercase letters, and that the number is between 1 and 9999. If any of the validations fail, an IllegalArgumentException will be thrown, preventing the creation of an invalid RouteName instance.
     * @param value The prefix of the route name, which must be a non-empty string consisting of exactly 2 uppercase letters. This validation ensures that the route name follows the expected format and adheres to the naming conventions for flight routes in the system.
     * @param routeNumber The number of the route name, which must be an integer between 1 and 9999. This validation ensures that the route number is valid and falls within the acceptable range for flight route numbers in the system, allowing for a consistent and meaningful naming convention for flight routes.
     */
    private RouteName(String value, int routeNumber) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Route id cannot be empty");
        }
        Preconditions.ensure(value.matches("[A-Z]{2}"), "Route prefix must be exactly 2 characters long");
        Preconditions.ensure(routeNumber >= 1 && routeNumber <= 9999, "Route number must have up to 4 digits");
        this.value = value;
        this.routeNumber = routeNumber;
    }

    /**
     * Factory method — creates a validated {@code RouteName}.
     * @param value the prefix of the route name, which must be a non-empty string consisting of exactly 2 uppercase letters. This validation ensures that the route name follows the expected format and adheres to the naming conventions for flight routes in the system.
     * @param number the number of the route name, which must be an integer between 1 and 9999. This validation ensures that the route number is valid and falls within the acceptable range for flight route numbers in the system, allowing for a consistent and meaningful naming convention for flight routes.
     * @return a new {@code RouteName} instance
     * @throws IllegalArgumentException if validation fails
     */
    public static RouteName valueOf(String value, int number) {
        return new RouteName(value, number);
    }

    /**
     * Returns the prefix of the route name.
     * @return the prefix of the route name, which is a non-empty string consisting of exactly 2 uppercase letters. This information is essential for identifying and distinguishing flight routes in the system, as it forms part of the unique identifier for each route based on its naming conventions.
     */
    public String value() {
        return value;
    }

    /**
     * Returns the number of the route name.
     * @return the number of the route name, which is an integer between 1 and 9999. This information is crucial for identifying and distinguishing flight routes in the system, as it forms part of the unique identifier for each route based on its naming conventions, allowing for a consistent and meaningful way to refer to specific flight routes in the application.
     */
    public int routeNumber() {
        return routeNumber;
    }

    /**
     * Overrides the equals method to compare RouteName instances based on their value and routeNumber properties. This method checks if the current instance is the same as the object being compared, and if not, it verifies that the object is an instance of RouteName and compares their value and routeNumber properties for equality. This implementation ensures that two RouteName instances are considered equal if they have the same prefix and number, which is essential for correctly managing and comparing flight routes in the application.
     * @param o the object to compare with this RouteName instance. The method will check if the provided object is an instance of RouteName and will compare their value and routeNumber properties for equality. If the provided object is not an instance of RouteName, the method will return false, indicating that the two objects are not equal.
     * @return true if the provided object is an instance of RouteName and has the same value and routeNumber properties as this instance, false otherwise. This implementation ensures that two RouteName instances with the same prefix and number are treated as equal, which is important for correctly managing collections of RouteName objects and for ensuring consistent behavior when comparing instances in the application.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof RouteName that)) return false;
        return Objects.equals(value, that.value) && routeNumber == that.routeNumber;
    }

    /**
     * Overrides the hashCode method to generate a hash code based on the value and routeNumber properties of the RouteName instance. This method uses the Objects.hash utility to compute a hash code that combines the hash codes of the value and routeNumber properties. This implementation ensures that RouteName instances with the same prefix and number will have the same hash code, which is important for correctly managing collections of RouteName objects and for ensuring consistent behavior when using RouteName instances as keys in hash-based data structures such as HashMap or HashSet.
     * @return a hash code value for the RouteName instance, computed based on the value and routeNumber properties. This implementation ensures that RouteName instances with the same prefix and number will have the same hash code, which is important for correctly managing collections of RouteName objects and for ensuring consistent behavior when using RouteName instances as keys in hash-based data structures such as HashMap or HashSet.
     */
    @Override
    public int hashCode() {
        return Objects.hash(value, routeNumber);
    }

    /**
     * Overrides the toString method to provide a string representation of the RouteName instance. This method formats the value and routeNumber properties into a string that combines the prefix and number in a readable format. This implementation allows for easy visualization of the RouteName instance when it is printed or logged, providing a clear and concise representation of the route name based on its prefix and number.
     * @return a string representation of the RouteName instance, formatted as the prefix followed by the number. This implementation allows for easy visualization of the RouteName instance when it is printed or logged, providing a clear and concise representation of the route name based on its prefix and number, which is useful for debugging and displaying route information in the application.
     */
    @Override
    public String toString() {
        return String.format("%s%d", value, routeNumber);
    }
}