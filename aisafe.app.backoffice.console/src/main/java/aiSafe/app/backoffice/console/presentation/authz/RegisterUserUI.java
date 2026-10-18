package aiSafe.app.backoffice.console.presentation.authz;

import aiSafe.usermanagement.application.RegisterUserController;
import eapli.framework.actions.Actions;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.MenuRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

import java.util.Objects;

public class RegisterUserUI extends AbstractUI {
    private final RegisterUserController theController = new RegisterUserController();

    @Override
    protected boolean doShow() {
        final String firstName = Console.readNonEmptyLine("First name:", "First name is mandatory.");
        final String lastName = Console.readNonEmptyLine("Last name:", "Last name is mandatory.");
        final String email = Console.readNonEmptyLine("E-Mail:", "E-Mail is mandatory.");
        final String password = Console.readNonEmptyLine("Password:", "Password is mandatory.");
        final Role assignedRole = readRole();
        final Integer phoneCode = Console.readInteger("Phone code (e.g. 351):");
        final Integer phoneNumber = Console.readInteger("Phone number:");

        try {
            theController.registerNewUser(firstName, lastName, email, assignedRole, phoneCode, phoneNumber, password);
            System.out.println("User successfully registered.");
            return true;
        } catch (final IllegalArgumentException e) {
            System.out.println("Unable to register user: " + e.getMessage());
        }
        return false;
    }

    private Role readRole() {
        final var roleHolder = new RoleHolder();
        while (roleHolder.value == null) {
            final Menu rolesMenu = new Menu("Select role >");
            int counter = 0;
            for (final var roleType : theController.getBackofficeRoleTypes()) {
                rolesMenu.addItem(MenuItem.of(counter++, roleType.toString(), () -> {
                    roleHolder.set(roleType);
                    return true;
                }));
            }
            rolesMenu.addItem(MenuItem.of(counter, "Cancel", Actions.FAIL));

            final MenuRenderer renderer = new VerticalMenuRenderer(rolesMenu, MenuItemRenderer.DEFAULT);
            final boolean ok = renderer.render();
            if (!ok && roleHolder.value == null) {
                throw new IllegalArgumentException("Role selection is mandatory");
            }
        }
        return roleHolder.value;
    }

    private static final class RoleHolder {
        private Role value;

        private void set(final Role role) {
            this.value = Objects.requireNonNull(role);
        }
    }

    @Override
    public String headline() {
        return "Register Backoffice User";
    }
}

