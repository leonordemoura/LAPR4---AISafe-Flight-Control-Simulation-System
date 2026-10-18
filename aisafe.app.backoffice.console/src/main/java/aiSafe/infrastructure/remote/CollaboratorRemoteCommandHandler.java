package aiSafe.infrastructure.remote;

import aiSafe.PilotCertification.application.AddPilotController;
import aiSafe.PilotCertification.application.ListPilotRosterController;
import aiSafe.PilotCertification.application.RemovePilotController;
import aiSafe.PilotCertification.domain.ModelCertification;
import aiSafe.aircraft.application.AddAircraftController;
import aiSafe.aircraft.application.DecommissionAircraftController;
import aiSafe.aircraft.application.ListFleetController;
import aiSafe.aircraft.domain.AircraftStatus;
import aiSafe.aircraftModel.domain.AircraftModelId;
import aiSafe.flightRoute.application.DeactivateFlightRouteController;
import aiSafe.flightRoute.application.RegisterFlightRouteController;
import aiSafe.flightRoute.domain.dto.FlightRouteDTO;
import aiSafe.usermanagement.domain.AisafeRoles;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CollaboratorRemoteCommandHandler implements RemoteCommandHandler {

    private static final String SERVICE_ID = "US78";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final AddAircraftController addAircraftController = new AddAircraftController();
    private final DecommissionAircraftController decommissionAircraftController = new DecommissionAircraftController();
    private final ListFleetController listFleetController = new ListFleetController();
    private final RegisterFlightRouteController registerRouteController = new RegisterFlightRouteController();
    private final DeactivateFlightRouteController deactivateRouteController = new DeactivateFlightRouteController();
    private final AddPilotController addPilotController = new AddPilotController();
    private final ListPilotRosterController listPilotRosterController = new ListPilotRosterController();
    private final RemovePilotController removePilotController = new RemovePilotController();

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
            case "ADD_AIRCRAFT" -> requireAuth(context, () -> handleAddAircraft(parts));
            case "DECOMMISSION_AIRCRAFT" -> requireAuth(context, () -> handleDecommissionAircraft(parts));
            case "LIST_FLEET" -> requireAuth(context, () -> handleListFleet(parts));
            case "REGISTER_FLIGHT_ROUTE" -> requireAuth(context, () -> handleRegisterFlightRoute(parts));
            case "DEACTIVATE_FLIGHT_ROUTE" -> requireAuth(context, () -> handleDeactivateFlightRoute(parts));
            case "ADD_PILOT" -> requireAuth(context, () -> handleAddPilot(parts));
            case "LIST_PILOT_ROSTER" -> requireAuth(context, () -> handleListPilotRoster(parts));
            case "REMOVE_PILOT" -> requireAuth(context, () -> handleRemovePilot(parts));
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
        final var session = auth.authenticate(username, password, AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        if (session.isEmpty()) {
            context.udpLogger().log(username, context.clientIp(), context.clientPort(), SERVICE_ID, "LOGIN_FAILED");
            return RemoteResponse.error("AUTH_FAILED", "Invalid credentials or role");
        }

        context.setAuthenticated(username, AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR.toString(), SERVICE_ID);
        context.udpLogger().log(username, context.clientIp(), context.clientPort(), SERVICE_ID, "LOGIN_SUCCESS");
        return RemoteResponse.ok("SESSION", username);
    }

    private String handleLogout(final RemoteConnectionContext context) {
        if (!context.isAuthenticated() || !AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR.toString().equals(context.role())) {
            return RemoteResponse.error("SESSION_REQUIRED", "Not authenticated");
        }
        final String username = context.username();
        AuthzRegistry.authorizationService().clearSession();
        context.clearAuthentication();
        context.udpLogger().log(username, context.clientIp(), context.clientPort(), SERVICE_ID, "LOGOUT");
        return RemoteResponse.ok("LOGOUT", username);
    }

    private String requireAuth(final RemoteConnectionContext context, final CommandAction action) {
        if (!context.isAuthenticated() || !AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR.toString().equals(context.role())) {
            return RemoteResponse.error("SESSION_REQUIRED", "Login required");
        }
        try {
            return action.run();
        } catch (final IllegalArgumentException | IllegalStateException ex) {
            return RemoteResponse.error("VALIDATION_ERROR", ex.getMessage());
        } catch (final Exception ex) {
            return RemoteResponse.error("INTERNAL_ERROR", ex.getMessage());
        }
    }

    private String handleAddAircraft(final String[] parts) {
        if (parts.length < 12) {
            return RemoteResponse.error("BAD_REQUEST", "Usage: ADD_AIRCRAFT;registration;modelId;engines(comma separated);firstClass;business;economy;year;countryName;countryCode;status;iataCode");
        }
        final List<String> engines = Arrays.asList(parts[3].trim().split(","));
        final var aircraft = addAircraftController.addAircraft(
                parts[1].trim(),
                parts[2].trim(),
                engines,
                Integer.parseInt(parts[4].trim()),
                Integer.parseInt(parts[5].trim()),
                Integer.parseInt(parts[6].trim()),
                Integer.parseInt(parts[7].trim()),
                parts[8].trim(),
                parts[9].trim().isEmpty() ? null : Integer.parseInt(parts[9].trim()),
                AircraftStatus.valueOf(parts[10].trim().toUpperCase()),
                parts[11].trim()
        );
        return RemoteResponse.ok("AIRCRAFT_ADDED", aircraft.registration().toString());
    }

    private String handleDecommissionAircraft(final String[] parts) {
        if (parts.length < 2) {
            return RemoteResponse.error("BAD_REQUEST", "Usage: DECOMMISSION_AIRCRAFT;registration");
        }
        decommissionAircraftController.decommissionAircraft(parts[1].trim());
        return RemoteResponse.ok("AIRCRAFT_DECOMMISSIONED", parts[1].trim());
    }

    private String handleListFleet(final String[] parts) {
        if (parts.length < 2) {
            return RemoteResponse.error("BAD_REQUEST", "Usage: LIST_FLEET;iataCode");
        }
        final var fleet = listFleetController.getFleet(parts[1].trim());
        final String payload = fleet.stream()
                .map(a -> a.registration().toString() + "," + a.aircraftModel().identity().toString() + "," + a.status())
                .collect(Collectors.joining("|"));
        return RemoteResponse.ok("FLEET", payload);
    }

    private String handleRegisterFlightRoute(final String[] parts) {
        if (parts.length < 4) {
            return RemoteResponse.error("BAD_REQUEST", "Usage: REGISTER_FLIGHT_ROUTE;routeName;originIata;destIata");
        }
        final var dto = new FlightRouteDTO(parts[1].trim(), parts[2].trim(), parts[3].trim());
        registerRouteController.registerFlightRoute(dto);
        return RemoteResponse.ok("ROUTE_REGISTERED", parts[1].trim());
    }

    private String handleDeactivateFlightRoute(final String[] parts) {
        if (parts.length < 3) {
            return RemoteResponse.error("BAD_REQUEST", "Usage: DEACTIVATE_FLIGHT_ROUTE;routeName;yyyy-MM-ddTHH:mm:ss");
        }
        final var dto = new FlightRouteDTO(parts[1].trim(), "", "");
        final LocalDateTime deactivationDate = LocalDateTime.parse(parts[2].trim(), DATE_TIME);
        deactivateRouteController.deactivateFlightRoute(dto, deactivationDate);
        return RemoteResponse.ok("ROUTE_DEACTIVATED", parts[1].trim());
    }

    private String handleAddPilot(final String[] parts) {
        if (parts.length < 4) {
            return RemoteResponse.error("BAD_REQUEST", "Usage: ADD_PILOT;pilotId;email;cert1Model,cert1Date;cert2Model,cert2Date...");
        }
        final List<ModelCertification> certs = new ArrayList<>();
        for (int i = 3; i < parts.length; i++) {
            final String[] certParts = parts[i].split(",");
            if (certParts.length == 2) {
                certs.add(new ModelCertification(new AircraftModelId(certParts[0].trim()), LocalDate.parse(certParts[1].trim(), DATE)));
            }
        }
        final var pilot = addPilotController.addPilot(parts[1].trim(), parts[2].trim(), certs);
        return RemoteResponse.ok("PILOT_ADDED", pilot.identity().toString());
    }

    private String handleListPilotRoster(final String[] parts) {
        final var roster = listPilotRosterController.listRoster();
        final String payload = roster.stream()
                .map(p -> p.pilotId() + "," + p.email() + "," + p.active() + "," + String.join(",", p.certifiedAircraftModels()))
                .collect(Collectors.joining("|"));
        return RemoteResponse.ok("PILOT_ROSTER", payload);
    }

    private String handleRemovePilot(final String[] parts) {
        if (parts.length < 2) {
            return RemoteResponse.error("BAD_REQUEST", "Usage: REMOVE_PILOT;pilotId");
        }
        removePilotController.deactivatePilot(parts[1].trim());
        return RemoteResponse.ok("PILOT_DEACTIVATED", parts[1].trim());
    }

    @FunctionalInterface
    private interface CommandAction {
        String run() throws Exception;
    }
}
