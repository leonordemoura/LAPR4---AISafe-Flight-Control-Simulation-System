package aiSafe.infrastructure.remote;

import aiSafe.usermanagement.domain.AisafeRoles;

public final class CompositeRemoteCommandHandler implements RemoteCommandHandler {

    private final WeatherRemoteCommandHandler weatherHandler = new WeatherRemoteCommandHandler();
    private final PilotRemoteCommandHandler pilotHandler = new PilotRemoteCommandHandler();
    private final CollaboratorRemoteCommandHandler collaboratorHandler = new CollaboratorRemoteCommandHandler();

    @Override
    public String handle(final String commandLine, final RemoteConnectionContext context) {
        final String[] parts = commandLine.split(";", -1);
        final String command = parts.length == 0 ? "" : parts[0].trim().toUpperCase();

        if ("LOGIN".equals(command)) {
            final String roleHint = parts.length >= 4 ? parts[3].trim().toUpperCase() : "";
            if ("PILOT".equals(roleHint)) {
                return pilotHandler.handle(commandLine, context);
            }
            if ("COLLABORATOR".equals(roleHint)) {
                return collaboratorHandler.handle(commandLine, context);
            }
            return weatherHandler.handle(commandLine, context);
        }

        if ("LOGOUT".equals(command)) {
            if (AisafeRoles.PILOT.toString().equals(context.role())) {
                return pilotHandler.handle(commandLine, context);
            }
            if (AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR.toString().equals(context.role())) {
                return collaboratorHandler.handle(commandLine, context);
            }
            return weatherHandler.handle(commandLine, context);
        }

        if (isPilotCommand(command)) {
            return pilotHandler.handle(commandLine, context);
        }
        if (isCollaboratorCommand(command)) {
            return collaboratorHandler.handle(commandLine, context);
        }
        return weatherHandler.handle(commandLine, context);
    }

    @Override
    public String serviceId() {
        return "US44/US78/US86";
    }

    private static boolean isPilotCommand(final String command) {
        return "REGISTER_FLIGHT_PLAN".equals(command) || "IMPORT_FLIGHT_PLAN_FILE".equals(command);
    }

    private static boolean isCollaboratorCommand(final String command) {
        return "ADD_AIRCRAFT".equals(command) || "DECOMMISSION_AIRCRAFT".equals(command)
                || "LIST_FLEET".equals(command) || "REGISTER_FLIGHT_ROUTE".equals(command)
                || "DEACTIVATE_FLIGHT_ROUTE".equals(command) || "ADD_PILOT".equals(command)
                || "LIST_PILOT_ROSTER".equals(command) || "REMOVE_PILOT".equals(command);
    }
}
