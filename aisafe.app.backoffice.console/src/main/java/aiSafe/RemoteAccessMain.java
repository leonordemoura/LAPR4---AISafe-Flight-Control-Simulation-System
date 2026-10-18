package aiSafe;

import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.remote.RemoteAccessTcpServer;
import aiSafe.usermanagement.domain.AisafePasswordPolicy;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;

import java.io.IOException;

/**
 * Headless entry point for DEI deployment: starts only the remote TCP server (US044/US078/US086).
 */
public final class RemoteAccessMain {

    private RemoteAccessMain() {
    }

    public static void main(final String[] args) throws IOException {
        AuthzRegistry.configure(
                PersistenceContext.repositories().users(),
                new AisafePasswordPolicy(),
                new PlainTextEncoder()
        );

        try (RemoteAccessTcpServer server = new RemoteAccessTcpServer()) {
            server.start();
            System.out.println("Remote access server running. Press Enter to stop.");
            System.in.read();
        }
    }
}
