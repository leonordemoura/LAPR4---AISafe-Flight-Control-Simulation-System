package aiSafe.flightRoute.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlightRouteTest {

    @Test
    void ensureTwoRoutesWithSameNameAreEqual(){
        // Arrange
        RouteName name = RouteName.valueOf("TP", 123);
        RouteDesignator designator = RouteDesignator.valueOf("OPO", "LIS");

        // Act
        final var route1 = new FlightRoute(name, designator);
        final var route2 = new FlightRoute(name, designator);

        // Assert
        assertTrue(route1.sameAs(route2), "Two FlightRoutes with the same name should be considered the same");
    }

    @Test
    void ensureFlightRouteIsCreatedSuccessfully() {
        // Arrange
        RouteName name = RouteName.valueOf("TP", 123);
        RouteDesignator designator = RouteDesignator.valueOf("OPO", "LIS");

        // Act
        final var route = new FlightRoute(name, designator);

        // Assert
        assertNotNull(route, "FlightRoute should be created successfully with valid attributes");
    }

    @Test
    void ensureRouteNameCannotBeNull(){
        // Arrange
        RouteDesignator designator = RouteDesignator.valueOf("OPO", "LIS");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new FlightRoute(null, designator));
    }

    @Test
    void ensureRouteNameIsRight() {
        // Arrange
        RouteName routeName = RouteName.valueOf("TP", 1234);

        // Act
        String expected = "TP1234";
        String actual = routeName.toString();

        // Assert
        assertEquals(expected, actual, "The route name should be correctly formatted as prefix followed by number.");

    }

    @Test
    void ensureRouteDesignatorIsRight() {
        // Arrange
        RouteDesignator routeDesignator = RouteDesignator.valueOf("OPO", "LIS");

        // Act
        String expected = "OPO-LIS";
        String actual = routeDesignator.toString();

        // Assert
        assertEquals(expected, actual, "The route designator should be correctly formatted as origin followed by destination.");
    }

    @Test
    void constructor_creates_active_flight_route_with_given_name_and_designator() {
        // Act
        RouteName validName       = RouteName.valueOf("TP", 123);
        RouteDesignator validDesignator = RouteDesignator.valueOf("LIS", "OPO");
        final FlightRoute route = new FlightRoute(validName, validDesignator);

        // Assert
        assertEquals(validName,       route.routeName());
        assertEquals(validDesignator, route.routeDesignator());
        assertTrue(route.isActive(), "A newly created route must be active by default");
    }

    @Test
    void valueOf_factory_method_produces_equivalent_instance_to_constructor() {
        // Act
        RouteName validName       = RouteName.valueOf("TP", 123);
        RouteDesignator validDesignator = RouteDesignator.valueOf("LIS", "OPO");

        // Arrange
        final FlightRoute viaConstructor = new FlightRoute(validName, validDesignator);
        final FlightRoute viaFactory     = FlightRoute.valueOf(validName, validDesignator);

        // Assert
        assertTrue(viaConstructor.sameAs(viaFactory), "The valueOf factory method should produce an instance that is the same as one created directly with the constructor when given the same arguments");
    }

    @Test
    void constructor_throws_when_route_name_is_null() {
        // Act
        RouteDesignator validDesignator = RouteDesignator.valueOf("LIS", "OPO");

        // Assert
        assertThrows(Exception.class, () -> new FlightRoute(null, validDesignator));
    }

    @Test
    void constructor_throws_when_route_designator_is_null() {
        // Act
        RouteName validName       = RouteName.valueOf("TP", 123);

        // Assert
        assertThrows(Exception.class, () -> new FlightRoute(validName, null));
    }

    @Test
    void constructor_throws_when_both_arguments_are_null() {
        // Assert
        assertThrows(Exception.class, () -> new FlightRoute(null, null));
    }

    @Test
    void isActive_returns_true_immediately_after_creation() {
        // Act
        RouteName validName       = RouteName.valueOf("TP", 123);
        RouteDesignator validDesignator = RouteDesignator.valueOf("LIS", "OPO");

        // Arrange
        final FlightRoute route = new FlightRoute(validName, validDesignator);

        // Assert
        assertTrue(route.isActive(), "A newly created flight route should be active by default");
    }

    @Test
    void routeName_returns_the_name_provided_at_construction() {
        // Act
        RouteName validName       = RouteName.valueOf("TP", 123);
        RouteDesignator validDesignator = RouteDesignator.valueOf("LIS", "OPO");

        // Arrange
        final FlightRoute route = new FlightRoute(validName, validDesignator);

        // Assert
        assertEquals(validName, route.routeName(), "The routeName accessor should return the RouteName provided at construction");
    }

    @Test
    void routeName_prefix_matches_the_prefix_used_at_construction() {
        // Act
        RouteName validName       = RouteName.valueOf("TP", 123);
        RouteDesignator validDesignator = RouteDesignator.valueOf("LIS", "OPO");

        // Arrange
        final FlightRoute route = new FlightRoute(validName, validDesignator);

        // Assert
        assertEquals("TP", route.routeName().value(), "The routeName prefix should match the prefix used at construction");
    }

    @Test
    void routeName_number_matches_the_number_used_at_construction() {
        // Act
        RouteName validName       = RouteName.valueOf("TP", 123);
        RouteDesignator validDesignator = RouteDesignator.valueOf("LIS", "OPO");

        // Arrange
        final FlightRoute route = new FlightRoute(validName, validDesignator);

        // Assert
        assertEquals(123, route.routeName().routeNumber(), "The routeName number should match the number used at construction");
    }

    @Test
    void routeDesignator_returns_the_designator_provided_at_construction() {
        // Act
        RouteName validName       = RouteName.valueOf("TP", 123);
        RouteDesignator validDesignator = RouteDesignator.valueOf("LIS", "OPO");

        // Arrange
        final FlightRoute route = new FlightRoute(validName, validDesignator);

        // Assert
        assertEquals(validDesignator, route.routeDesignator(), "The routeDesignator accessor should return the RouteDesignator provided at construction");
    }

    @Test
    void routeDesignator_origin_matches_the_origin_used_at_construction() {
        // Act
        RouteName validName       = RouteName.valueOf("TP", 123);
        RouteDesignator validDesignator = RouteDesignator.valueOf("LIS", "OPO");

        // Arrange
        final FlightRoute route = new FlightRoute(validName, validDesignator);

        // Assert
        assertEquals("LIS", route.routeDesignator().origin(), "The routeDesignator origin should match the origin used at construction");
    }

    @Test
    void routeDesignator_destination_matches_the_destination_used_at_construction() {
        // Act
        RouteName validName       = RouteName.valueOf("TP", 123);
        RouteDesignator validDesignator = RouteDesignator.valueOf("LIS", "OPO");

        // Arrange
        final FlightRoute route = new FlightRoute(validName, validDesignator);

        // Assert
        assertEquals("OPO", route.routeDesignator().destination(), "The routeDesignator destination should match the destination used at construction");
    }
    @Test
    void sameAs_returns_true_for_routes_with_the_same_route_name() {
        // Act
        RouteName validName       = RouteName.valueOf("TP", 123);
        RouteDesignator validDesignator = RouteDesignator.valueOf("LIS", "OPO");

        // Arrange
        final FlightRoute a = new FlightRoute(validName, validDesignator);
        final FlightRoute b = new FlightRoute(validName, RouteDesignator.valueOf("LIS", "FAO"));

        // Assert
        assertTrue(a.sameAs(b), "Two FlightRoutes with the same RouteName should be considered the same domain object, regardless of their RouteDesignator");
    }

    @Test
    void sameAs_returns_false_for_routes_with_different_route_names() {
        // Act
        RouteDesignator validDesignator = RouteDesignator.valueOf("LIS", "OPO");

        // Arrange
        final FlightRoute a = new FlightRoute(RouteName.valueOf("TP", 1),   validDesignator);
        final FlightRoute b = new FlightRoute(RouteName.valueOf("TP", 2),   validDesignator);

        // Assert
        assertFalse(a.sameAs(b), "Two FlightRoutes with different RouteNames should not be considered the same domain object, even if they have the same RouteDesignator");
    }

    @Test
    void sameAs_returns_false_when_prefix_differs() {
        // Act
        RouteName validName       = RouteName.valueOf("TP", 123);
        RouteDesignator validDesignator = RouteDesignator.valueOf("LIS", "OPO");

        // Arrange
        final FlightRoute a = new FlightRoute(RouteName.valueOf("TP", 123), validDesignator);
        final FlightRoute b = new FlightRoute(RouteName.valueOf("AB", 123), validDesignator);

        // Assert
        assertFalse(a.sameAs(b), "Two FlightRoutes with different RouteName prefixes should not be considered the same domain object, even if their numbers and RouteDesignators are the same");
    }

    @Test
    void sameAs_returns_true_for_the_same_instance() {
        // Act
        RouteName validName       = RouteName.valueOf("TP", 123);
        RouteDesignator validDesignator = RouteDesignator.valueOf("LIS", "OPO");

        // Arrange
        final FlightRoute route = new FlightRoute(validName, validDesignator);

        // Assert
        assertTrue(route.sameAs(route), "The same FlightRoute instance should be considered the same domain object when compared to itself");
    }

    @Test
    void sameAs_returns_false_for_null() {
        // Act
        RouteName validName       = RouteName.valueOf("TP", 123);
        RouteDesignator validDesignator = RouteDesignator.valueOf("LIS", "OPO");

        // Arrange
        final FlightRoute route = new FlightRoute(validName, validDesignator);

        // Assert
        assertFalse(route.sameAs(null), "The sameAs method should return false when compared to null, as a FlightRoute cannot be the same as null");
    }

    @Test
    void sameAs_returns_false_for_object_of_different_type() {
        // Act
        RouteName validName       = RouteName.valueOf("TP", 123);
        RouteDesignator validDesignator = RouteDesignator.valueOf("LIS", "OPO");

        // Arrange
        final FlightRoute route = new FlightRoute(validName, validDesignator);

        // Assert
        assertFalse(route.sameAs("TP123"), "The sameAs method should return false when compared to an object of a different type, as a FlightRoute cannot be the same as a String or any other type");
    }

    @Test
    void identity_returns_null_before_persistence() {
        // Act
        RouteName validName       = RouteName.valueOf("TP", 123);
        RouteDesignator validDesignator = RouteDesignator.valueOf("LIS", "OPO");

        // Arrange
        final FlightRoute route = new FlightRoute(validName, validDesignator);

        // Assert
        assertNull(route.identity(), "The identity method should return null for a FlightRoute instance that has not been persisted yet, as the routeID is typically assigned by the persistence mechanism when the entity is saved to the database");
    }

    @Test
    void two_routes_with_same_name_but_different_designators_are_considered_the_same_domain_object() {
        // Act
        RouteName validName       = RouteName.valueOf("TP", 123);

        // sameAs is based solely on RouteName (business key)
        // Arrange
        final FlightRoute lisOpo = FlightRoute.valueOf(validName, RouteDesignator.valueOf("LIS", "OPO"));
        final FlightRoute lisFao = FlightRoute.valueOf(validName, RouteDesignator.valueOf("LIS", "FAO"));

        // Assert
        assertTrue(lisOpo.sameAs(lisFao), "Two FlightRoutes with the same RouteName should be considered the same domain object, even if their RouteDesignators differ, as the identity of a FlightRoute is determined by its RouteName according to the sameAs method implementation");
    }

    @Test
    void two_routes_with_different_names_are_not_the_same_domain_object() {
        // Act
        RouteDesignator validDesignator = RouteDesignator.valueOf("LIS", "OPO");

        // sameAs is based solely on RouteName (business key)
        // Arrange
        final FlightRoute tp1 = FlightRoute.valueOf(RouteName.valueOf("TP", 1), validDesignator);
        final FlightRoute tp2 = FlightRoute.valueOf(RouteName.valueOf("TP", 2), validDesignator);

        // Assert
        assertFalse(tp1.sameAs(tp2), "Two FlightRoutes with different RouteNames should not be considered the same domain object, even if their RouteDesignators are the same, as the identity of a FlightRoute is determined by its RouteName according to the sameAs method implementation");
    }
}