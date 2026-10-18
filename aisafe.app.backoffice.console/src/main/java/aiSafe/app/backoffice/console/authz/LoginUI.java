package aiSafe.app.backoffice.console.authz;

import aiSafe.infrastructure.authz.CredentialHandler;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.io.util.Console;

public class LoginUI {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    private final CredentialHandler credentialHandler;
    private final Role onlyWithThis;
    private final int maxAttempts;

    public LoginUI(final CredentialHandler credentialHandler) {
        this(credentialHandler, null, DEFAULT_MAX_ATTEMPTS);
    }

    public LoginUI(final CredentialHandler credentialHandler, final Role onlyWithThis, final int maxAttempts) {
        this.credentialHandler = credentialHandler;
        this.onlyWithThis = onlyWithThis;
        this.maxAttempts = maxAttempts;
    }

    public boolean show() {
        var attempt = 1;
        while (attempt <= maxAttempts) {
            final String userName = Console.readNonEmptyLine("Email:", "Please provide a username");
            final String password = Console.readLine("Password:");

            if (credentialHandler.authenticated(userName, password, onlyWithThis)) {
                return true;
            }
            System.out.printf("Wrong username or password. You have %d attempts left.%n%n", maxAttempts - attempt);
            attempt++;
        }
        System.out.println("Unable to authenticate user.");
        return false;
    }
}
