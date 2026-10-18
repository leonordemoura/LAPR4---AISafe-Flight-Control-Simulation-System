package aiSafe.infrastructure.remote;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.io.IOException;
import java.net.Socket;

public final class RemoteConnectionHandler implements Runnable {

    private final Socket socket;
    private final RemoteCommandHandler commandHandler;
    private final RemoteAccessUdpLogger udpLogger;

    public RemoteConnectionHandler(final Socket socket,
                                   final RemoteCommandHandler commandHandler,
                                   final RemoteAccessUdpLogger udpLogger) {
        this.socket = socket;
        this.commandHandler = commandHandler;
        this.udpLogger = udpLogger;
    }

    @Override
    public void run() {
        final RemoteConnectionContext context = new RemoteConnectionContext(
                socket.getInetAddress().getHostAddress(),
                socket.getPort(),
                udpLogger
        );

        try (socket) {
            var running = true;
            while (running) {
                final String request;
                try {
                    request = RemoteMessageCodec.readMessage(socket.getInputStream());
                } catch (final IOException ex) {
                    break;
                }

                final String response = dispatch(request, context);
                RemoteMessageCodec.writeMessage(socket.getOutputStream(), response);

                if (request.startsWith("LOGOUT") && response.startsWith("OK;LOGOUT")) {
                    running = false;
                }
            }
        } catch (final IOException ex) {
            System.err.println("[US044] Connection error: " + ex.getMessage());
        } finally {
            if (context.isAuthenticated()) {
                AuthzRegistry.authorizationService().clearSession();
                context.udpLogger().log(
                        context.username(),
                        context.clientIp(),
                        context.clientPort(),
                        context.serviceId() == null ? commandHandler.serviceId() : context.serviceId(),
                        "DISCONNECT"
                );
            }
        }
    }

    private String dispatch(final String request, final RemoteConnectionContext context) {
        if (request == null || request.isBlank()) {
            return RemoteResponse.error("BAD_REQUEST", "Empty request");
        }
        final String command = request.split(";", 2)[0].trim().toUpperCase();
        if (!"LOGIN".equals(command) && !context.isAuthenticated()) {
            return RemoteResponse.error("SESSION_REQUIRED", "Login required");
        }
        return commandHandler.handle(request.trim(), context);
    }
}
