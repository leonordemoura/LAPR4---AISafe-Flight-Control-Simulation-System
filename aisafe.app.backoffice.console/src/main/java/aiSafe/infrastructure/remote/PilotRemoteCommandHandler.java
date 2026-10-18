package aiSafe.infrastructure.remote;

import aiSafe.flight.application.RegisterFlightPlanController;
import aiSafe.flight.application.RegisterFlightPlanDTO;
import aiSafe.flight.application.importplan.ImportFlightPlanFromFileController;
import aiSafe.usermanagement.domain.AisafeRoles;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PilotRemoteCommandHandler implements RemoteCommandHandler {

    private static final String SERVICE_ID = "US86";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final RegisterFlightPlanController registerController = new RegisterFlightPlanController();
    private final ImportFlightPlanFromFileController importController = new ImportFlightPlanFromFileController();

    @Override
    public String handle(final String commandLine, final RemoteConnectionContext context) {
        final String[] parts = commandLine.split(";", -1);
        if (parts.length == 0 || parts[0].isBlank()) {
            return RemoteResponse.error("BAD_REQUEST", "Empty command");
        }

        final String command = parts[0].trim().toUpperCase();
        return switch (command) {
            case "LOGIN" -> handleLogin(parts, context);
            case "LOGOUT" -> handleLogout(context);
            case "REGISTER_FLIGHT_PLAN" -> requireAuth(context, () -> handleRegisterFlightPlan(parts));
            case "IMPORT_FLIGHT_PLAN_FILE" -> requireAuth(context, () -> handleImportFlightPlan(parts));
            default -> RemoteResponse.error("BAD_REQUEST", "Unknown command: " + command);
        };
    }

    @Override
    public String serviceId() {
        return SERVICE_ID;
    }

    private String handleLogin(final String[] parts, final RemoteConnectionContext context) {
        if (parts.length < 3) {
            return RemoteResponse.error("BAD_REQUEST", "Usage: LOGIN;username;password");
        }
        final String username = parts[1].trim();
        final String password = parts[2];

        final var auth = AuthzRegistry.authenticationService();
        final var session = auth.authenticate(username, password, AisafeRoles.PILOT);
        if (session.isEmpty()) {
            context.udpLogger().log(username, context.clientIp(), context.clientPort(), SERVICE_ID, "LOGIN_FAILED");
            return RemoteResponse.error("AUTH_FAILED", "Invalid credentials or role");
        }

        context.setAuthenticated(username, AisafeRoles.PILOT.toString(), SERVICE_ID);
        context.udpLogger().log(username, context.clientIp(), context.clientPort(), SERVICE_ID, "LOGIN_SUCCESS");
        return RemoteResponse.ok("SESSION", username);
    }

    private String handleLogout(final RemoteConnectionContext context) {
        if (!context.isAuthenticated() || !AisafeRoles.PILOT.toString().equals(context.role())) {
            return RemoteResponse.error("SESSION_REQUIRED", "Not authenticated");
        }
        final String username = context.username();
        AuthzRegistry.authorizationService().clearSession();
        context.clearAuthentication();
        context.udpLogger().log(username, context.clientIp(), context.clientPort(), SERVICE_ID, "LOGOUT");
        return RemoteResponse.ok("LOGOUT", username);
    }

    private String requireAuth(final RemoteConnectionContext context, final CommandAction action) {
        if (!context.isAuthenticated() || !AisafeRoles.PILOT.toString().equals(context.role())) {
            return RemoteResponse.error("SESSION_REQUIRED", "Login required");
        }
        try {
            return action.run();
        } catch (final IllegalArgumentException ex) {
            return RemoteResponse.error("VALIDATION_ERROR", ex.getMessage());
        } catch (final Exception ex) {
            return RemoteResponse.error("INTERNAL_ERROR", ex.getMessage());
        }
    }

    private String handleRegisterFlightPlan(final String[] parts) {
        if (parts.length < 10) {
            return RemoteResponse.error("BAD_REQUEST",
                    "Usage: REGISTER_FLIGHT_PLAN;airlineCode;flightNumber;operationalSuffix;pilotId;departureDateTime;arrivalDateTime;fuelQuantity;fuelUnit;dslContent");
        }

        final LocalDateTime departure = LocalDateTime.parse(parts[5].trim(), DATE_TIME);
        final LocalDateTime arrival = LocalDateTime.parse(parts[6].trim(), DATE_TIME);
        final RegisterFlightPlanDTO dto = new RegisterFlightPlanDTO(
                parts[1].trim(),
                Integer.parseInt(parts[2].trim()),
                parts[3].trim(),
                parts[4].trim(),
                departure,
                arrival,
                Double.parseDouble(parts[7].trim()),
                parts[8].trim(),
                parts[9],
                java.util.List.of(),
                null
        );

        final var flight = registerController.registerFlightPlan(dto);
        return RemoteResponse.ok("FLIGHT_PLAN_REGISTERED", String.valueOf(flight.designator()));
    }

    private String handleImportFlightPlan(final String[] parts) {
        if (parts.length < 5) {
            return RemoteResponse.error("BAD_REQUEST",
                    "Usage: IMPORT_FLIGHT_PLAN_FILE;airlineCode;flightNumber;operationalSuffix;filePath");
        }

        final var designator = importController.generateFlightDesignator(
                parts[1].trim(),
                Integer.parseInt(parts[2].trim()),
                parts[3].trim()
        );
        final var result = importController.importFlightPlanFromFile(designator, Path.of(parts[4].trim()));
        if (!result.isSuccess()) {
            final String message = result.formattedErrors().isEmpty()
                    ? "Import failed"
                    : String.join("|", result.formattedErrors());
            return RemoteResponse.error("VALIDATION_ERROR", message);
        }
        return RemoteResponse.ok("FLIGHT_PLAN_IMPORTED", designator.toString());
    }

    @FunctionalInterface
    private interface CommandAction {
        String run() throws Exception;
    }
}

