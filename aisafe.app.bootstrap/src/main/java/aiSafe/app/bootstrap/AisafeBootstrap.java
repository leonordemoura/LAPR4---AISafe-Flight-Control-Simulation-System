package aiSafe.app.bootstrap;

import aiSafe.infrastructure.bootstrap.MasterDataBootstrapper;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.usermanagement.domain.AisafePasswordPolicy;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;

public final class AisafeBootstrap {

    private AisafeBootstrap() {
        // Utility class.
    }

    public static void main(final String[] args) {
        AuthzRegistry.configure(
                PersistenceContext.repositories().users(),
                new AisafePasswordPolicy(),
                new PlainTextEncoder()
        );
        System.out.println("Starting AISafe bootstrap...");
        new MasterDataBootstrapper().execute();
        System.out.println("AISafe bootstrap completed.");
    }
}
