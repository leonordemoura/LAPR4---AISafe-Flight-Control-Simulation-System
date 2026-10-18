package aiSafe.app.backoffice.console.presentation.authz;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.presentation.console.AbstractUI;

public class LogoutUI extends AbstractUI {
    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    @Override
    public String headline() {
        return "Logout successful.\nPlease login again.";
    }

    @Override
    protected boolean doShow() {
        authz.clearSession();
        return true;
    }
}
