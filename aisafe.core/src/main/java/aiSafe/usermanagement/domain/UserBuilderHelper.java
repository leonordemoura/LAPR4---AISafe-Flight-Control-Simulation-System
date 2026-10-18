package aiSafe.usermanagement.domain;

import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;

public final class UserBuilderHelper {
    private UserBuilderHelper() {
        // Utility class.
    }

    public static SystemUserBuilder builder() {
        return new SystemUserBuilder(new AisafePasswordPolicy(), new PlainTextEncoder());
    }
}
