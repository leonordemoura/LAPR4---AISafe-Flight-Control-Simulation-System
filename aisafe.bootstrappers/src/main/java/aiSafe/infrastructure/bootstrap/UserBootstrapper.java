package aiSafe.infrastructure.bootstrap;

import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.FullName;
import aiSafe.usermanagement.domain.PhoneNumber;
import aiSafe.usermanagement.domain.User;
import aiSafe.usermanagement.domain.UserBuilderHelper;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.infrastructure.authz.domain.model.Role;

import java.util.Set;

/**
 * US031/US030 bootstrap:
 * - Creates framework auth users (login/roles)
 * - Creates domain {@link User} (VOs/invariants + optional linkage to framework PK)
 *
 * Uses email as the single identity (username == email).
 */
public class UserBootstrapper {

    public void execute() {
        registerIfMissing(
                "System",
                "Administrator",
                "admin@aisafe.local",
                "Admin123",
                351,
                999999999,
                AisafeRoles.ADMIN,
                Set.of(AisafeRoles.ADMIN, AisafeRoles.BACKOFFICE_OPERATOR)
        );

        registerIfMissing(
                "BackOffice",
                "Operator",
                "backoffice@aisafe.local",
                "Backoffice123",
                351,
                900000001,
                AisafeRoles.BACKOFFICE_OPERATOR,
                Set.of(AisafeRoles.BACKOFFICE_OPERATOR)
        );

        registerIfMissing(
                "Weather",
                "Person",
                "weather@aisafe.local",
                "Weather123",
                351,
                900000002,
                AisafeRoles.WEATHER_PERSON,
                Set.of(AisafeRoles.WEATHER_PERSON)
        );

        registerIfMissing(
                "AirTransport",
                "Collaborator",
                "collaborator@aisafe.local",
                "Collaborator123",
                351,
                900000003,
                AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                Set.of(AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)
        );

        registerIfMissing("Pilot",
                "Collaborator",
                "pilot@aisafe.local",
                "Pilot123",
                351,
                900000000,
                AisafeRoles.PILOT,
                Set.of(AisafeRoles.PILOT)
        );

        registerIfMissing(
                "SecondPilot",
                "Collaborator",
                "pilot2@aisafe.local",
                "Pilot123",
                351,
                912345680,
                AisafeRoles.PILOT,
                Set.of(AisafeRoles.PILOT)
        );

        registerIfMissing(
                "FlightControl",
                "Operator",
                "fco@aisafe.local",
                "Fco1234",
                351,
                912345681,
                AisafeRoles.FLIGHT_CONTROL_OPERATOR,
                Set.of(AisafeRoles.FLIGHT_CONTROL_OPERATOR)
        );
    }

    private void registerIfMissing(
            final String firstName,
            final String lastName,
            final String email,
            final String password,
            final Integer phoneCode,
            final Integer phoneNumber,
            final Role domainAssignedRole,
            final Set<Role> authzRoles
    ) {
        try {
            final var factory = PersistenceContext.repositories();
            final var domainRepo = factory.userManagementUsers();
            final var authzRepo = factory.users();

            final var emailVO = new Email(email);
            final var phoneVO = new PhoneNumber(phoneCode, phoneNumber);

            if (domainRepo.existsByEmail(emailVO)) {
                System.out.println("User already exists (domain): " + email);
                return;
            }

            final var userBuilder = UserBuilderHelper.builder();
            userBuilder.withUsername(emailVO.value())
                    .withPassword(password)
                    .withName(firstName, lastName)
                    .withEmail(emailVO.value())
                    .withRoles(authzRoles);
            try {
                authzRepo.save(userBuilder.build());
            } catch (ConcurrencyException | IntegrityViolationException ex) {
                // assume already exists on authz side; continue to ensure domain record exists
            }

            final var domainUser =
                    new User(new FullName(firstName, lastName), emailVO, phoneVO, domainAssignedRole);
            domainRepo.save(domainUser);

            System.out.println("User created: " + email);
        } catch (final Exception ex) {
            System.out.println("Failed to create user " + email + ": " + ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }
}
