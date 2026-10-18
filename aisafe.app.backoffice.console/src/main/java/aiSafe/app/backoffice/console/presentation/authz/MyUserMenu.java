package aiSafe.app.backoffice.console.presentation.authz;

import aiSafe.app.backoffice.console.authz.LoginUI;
import aiSafe.infrastructure.authz.AuthenticationCredentialHandler;
import eapli.framework.actions.Actions;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.presentation.console.ShowMessageAction;

public class MyUserMenu extends Menu {
    private static final String MENU_TITLE = "My account >";
    private static final int EXIT_OPTION = 0;
    private static final int LOGIN_OPTION = 1;
    private static final int LOGOUT_OPTION = 2;
    private static final int SESSION_INFO_OPTION = 3;
    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    public MyUserMenu() {
        this(null);
    }

    public MyUserMenu(final Role onlyWithThis) {
        super(MENU_TITLE);
        build(onlyWithThis);
    }

    private void build(final Role onlyWithThis) {
        addItem(MenuItem.of(LOGIN_OPTION, "Change user",
                new LoginUI(new AuthenticationCredentialHandler(), onlyWithThis, DEFAULT_MAX_ATTEMPTS)::show));
        addItem(MenuItem.of(LOGOUT_OPTION, "Logout", new LogoutUI()::show));
        addItem(MenuItem.of(SESSION_INFO_OPTION, "Session info", new ShowMessageAction(sessionInfoMessage())));
        addItem(MenuItem.of(EXIT_OPTION, "Return ", Actions.SUCCESS));
    }

    private String sessionInfoMessage() {
        return authz.session()
                .map(s -> "Authenticated as @" + s.authenticatedUser().identity())
                .orElse("No active session.");
    }
}
