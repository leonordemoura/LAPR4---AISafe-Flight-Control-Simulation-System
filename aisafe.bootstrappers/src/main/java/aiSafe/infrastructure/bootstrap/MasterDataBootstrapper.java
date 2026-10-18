package aiSafe.infrastructure.bootstrap;

import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.UserBuilderHelper;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.io.util.Console;

import java.util.Set;

public class MasterDataBootstrapper {

    public void execute() {
        if (!isBootstrapExecutionAuthorized()) {
            System.out.println("Bootstrap denied. Administrator authentication required.");
            return;
        }
        registerUsers();
        registerAirControlAreas();
        registerMaker();
        registerFuelTypes();
        registerEngineModel();
        registerAirTransportCompany();
        registerCostumerCollaborators();
        registerAirports();
        registerAircraftModels();
        registerAircrafts();
        registerFlightRoutes();
        registerFlights();
        System.out.println("Master data bootstrap finished.");
    }

    private boolean isBootstrapExecutionAuthorized() {
        final var userRepository = PersistenceContext.repositories().users();
        final boolean hasExistingUsers = userRepository.findAll().iterator().hasNext();
        if (!hasExistingUsers) {
            return true;
        }

        System.out.println("Existing users detected. Authenticate as ADMIN to execute bootstrap operations.");
        final String username = Console.readNonEmptyLine("Admin username:", "Username is mandatory.");
        final String password = Console.readLine("Admin password:");

        return AuthzRegistry.authenticationService()
                .authenticate(username, password, AisafeRoles.ADMIN)
                .isPresent();
    }

    private void registerAdminUser() {
        final var userBuilder = UserBuilderHelper.builder();
        userBuilder.withUsername("admin")
                .withPassword("Admin123")
                .withName("System", "Administrator")
                .withEmail("admin@aisafe.local")
                .withRoles(Set.of(AisafeRoles.ADMIN, AisafeRoles.BACKOFFICE_OPERATOR));

        final var userRepository = PersistenceContext.repositories().users();
        try {
            userRepository.save(userBuilder.build());
            System.out.println("Admin user created: admin / Admin123");
        } catch (ConcurrencyException | IntegrityViolationException ex) {
            System.out.println("Admin user already exists. Keeping current credentials.");
        }
    }

    private void registerUsers() {
        try {
            new UserBootstrapper().execute();
            System.out.println("Users bootstrap executed.");
        } catch (final Exception ex) {
            System.out.println("Users bootstrap skipped due to error: " + ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    private void registerAirControlAreas() {
        try {
            new AirControlAreaBootstrapper().execute();
            System.out.println("Air control areas bootstrap executed.");
        } catch (final Exception ex) {
            System.out.println("Air control areas bootstrap skipped due to error.");
        }
    }

    private void registerMaker() {
        try {
            new MakerBootstrapper().execute();
            System.out.println("Maker Bootstrap executed.");
        } catch (final Exception ex) {
            System.out.println("Maker bootstrap skipped due to error.");
        }
    }

    private void registerEngineModel() {
        try {
            new EngineModelBootstrapper().execute();
            System.out.println("Engine Model Bootstrap executed.");
        } catch (final Exception ex) {
            System.out.println("Engine Model bootstrap skipped due to error.");
        }
    }

    private void registerAirTransportCompany() {
        try {
            new AirTransportCompanyBootstrapper().execute();
            System.out.println("Air Transport Company Bootstrap executed.");
        } catch (final Exception ex) {
            System.out.println("Air Transport Company bootstrap skipped due to error.");
        }
    }

    private void registerAirports() {
        try {
            new AirportBootstrapper().execute();
            System.out.println("Airport bootstrap executed.");
        } catch (final Exception ex) {
            System.out.println("Airport bootstrap skipped due to error.");
        }
    }

    private void registerCostumerCollaborators() {
        try {
            new CostumerCollaboratorBootstrapper().execute();
            System.out.println("Costumer collaborators bootstrap executed.");
        } catch (final Exception ex) {
            System.out.println("Costumer collaborators bootstrap skipped due to error.");
        }
    }

    private void registerAircraftModels() {
        try {
            new AircraftModelBootstrapper().execute();
            System.out.println("AircraftModel bootstrap executed.");
        } catch (final Exception ex) {
            System.out.println("AircraftModel bootstrap skipped due to error.");
        }
    }

    private void registerFuelTypes() {
        try {
            new FuelTypeBootstrapper().execute();
            System.out.println("Fuel types bootstrap executed.");
        } catch (final Exception ex) {
            System.out.println("Fuel types bootstrap skipped due to error.");
        }
    }

    private void registerAircrafts() {
        try {
            new AircraftBootstrapper().execute();
            System.out.println("Aircraft bootstrap executed.");
        } catch (final Exception ex) {
            System.out.println("Aircraft bootstrap skipped due to error: " + ex.getMessage());
        }
    }

    private void registerFlights() {
        try {
            new FlightBootstrap().execute();
            System.out.println("Flight bootstrap executed.");
        } catch (final Exception ex) {
            System.out.println("Flight bootstrap skipped due to error: " + ex.getMessage());
        }
    }

    private void registerFlightRoutes() {
        try {
            new FlightRouteBootstrap().execute();
            System.out.println("FlightRoute bootstrap executed.");
        } catch (final Exception ex) {
            System.out.println("FlightRoute bootstrap skipped due to error: " + ex.getMessage());
        }
    }
}