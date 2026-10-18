package aiSafe.usermanagement.domain;

import eapli.framework.infrastructure.authz.domain.model.Role;

import java.util.Objects;

/**
 * Application roles mirrored as framework {@link Role} instances (see bootstrap / auth tables).
 */
public final class AisafeRoles {

    public static final Role ADMIN = Role.valueOf("ADMIN");
    public static final Role BACKOFFICE_OPERATOR = Role.valueOf("BACKOFFICE_OPERATOR");
    public static final Role WEATHER_PERSON = Role.valueOf("WEATHER_PERSON");
    public static final Role AIR_TRANSPORT_COMPANY_COLLABORATOR = Role.valueOf("AIR_TRANSPORT_COMPANY_COLLABORATOR");
    public static final Role PILOT = Role.valueOf("PILOT");
    public static final Role FLIGHT_CONTROL_OPERATOR = Role.valueOf("FLIGHT_CONTROL_OPERATOR");

    private AisafeRoles() {
    }


    public static String authorityKey(final Role role) {
        return role.toString();
    }
}
