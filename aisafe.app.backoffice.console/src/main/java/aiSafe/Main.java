package aiSafe;

import aiSafe.app.backoffice.console.authz.LoginUI;
import aiSafe.app.backoffice.console.presentation.MainMenu;
import aiSafe.app.common.console.BaseApp;
import aiSafe.infrastructure.authz.AuthenticationCredentialHandler;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.remote.RemoteAccessTcpServer;
import aiSafe.usermanagement.domain.AisafePasswordPolicy;
import aiSafe.Application;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.pubsub.EventDispatcher;

import java.io.IOException;

public final class Main extends BaseApp {
    private Main() {
        // Utility constructor.
    }

    public static void main(final String[] args) {
        AuthzRegistry.configure(
                PersistenceContext.repositories().users(),
                new AisafePasswordPolicy(),
                new PlainTextEncoder()
        );

        final RemoteAccessTcpServer remoteServer = maybeStartRemoteServer();
        try {
            new Main().run(args);
        } finally {
            if (remoteServer != null) {
                remoteServer.close();
            }
        }
    }

    private static RemoteAccessTcpServer maybeStartRemoteServer() {
        final String enabled = Application.settings().getProperty("remote.tcp.enabled");
        if (!"true".equalsIgnoreCase(enabled)) {
            return null;
        }
        try {
            final RemoteAccessTcpServer server = new RemoteAccessTcpServer();
            server.start();
            return server;
        } catch (final IOException ex) {
            System.err.println("Failed to start remote TCP server: " + ex.getMessage());
            return null;
        }
    }

    @Override
    protected void doMain(final String[] args) {
        if (new LoginUI(new AuthenticationCredentialHandler()).show()) {
            final var menu = new MainMenu();
            menu.mainLoop();
        }
    }

    @Override
    protected String appTitle() {
        return "AISafe Back Office";
    }

    @Override
    protected String appGoodbye() {
        return "AISafe Back Office";
    }

    @Override
    protected void doSetupEventHandlers(final EventDispatcher dispatcher) {
        // No domain events wired yet.
    }
}
