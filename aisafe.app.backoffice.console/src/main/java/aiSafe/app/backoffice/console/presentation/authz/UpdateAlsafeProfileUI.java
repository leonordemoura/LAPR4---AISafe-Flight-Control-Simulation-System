package aiSafe.app.backoffice.console.presentation.authz;

import aiSafe.usermanagement.application.UpdateAlsafeProfileController;
import aiSafe.usermanagement.domain.SecurityClearance;
import aiSafe.usermanagement.domain.SkillsAssessment;
import aiSafe.usermanagement.domain.User;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import java.time.ZoneId;
import java.util.Date;

import java.time.LocalDate;

public class UpdateAlsafeProfileUI extends AbstractUI {

    private final UpdateAlsafeProfileController theController =
            new UpdateAlsafeProfileController();

    @Override
    protected boolean doShow() {

        try {

            // =========================================
            // USERS LIST
            // =========================================

            System.out.println("\nAVAILABLE USERS:");

            for (User user : theController.listUsers()) {
                System.out.println(
                        user.identity()
                                + " - "
                                + user.fullName().firstName()
                                + " "
                                + user.fullName().lastName()
                                + " ("
                                + user.email().value()
                                + ")"
                );
            }

            final Long userId = Console.readLong("\nChoose User ID:");

            // validate user exists (optional but recommended)
            boolean exists = false;
            User selectedUser = null;

            for (User u : theController.listUsers()) {
                if (u.identity().equals(userId)) {
                    exists = true;
                    selectedUser = u;
                    break;
                }
            }

            if (!exists) {
                System.out.println("User not found.");
                return false;
            }

            // =========================================
            // SECURITY CLEARANCE
            // =========================================

            final Date clearanceExpiryDate =
                    Console.readDate("Clearance expiry date (yyyy/mm/dd):");

            final LocalDate clearanceExpiry =
                    clearanceExpiryDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

            final SecurityClearance clearance =
                    new SecurityClearance(clearanceExpiry);

            // =========================================
            // SKILLS ASSESSMENT
            // =========================================

            final Date assessmentDateRaw =
                    Console.readDate("Skills assessment date (yyyy/mm/dd):");

            final LocalDate assessmentDate =
                    assessmentDateRaw.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

            final SkillsAssessment assessment =
                    new SkillsAssessment(assessmentDate);

            // =========================================
            // EXECUTE
            // =========================================

            theController.attachProfileToUser(
                    selectedUser.identity(),
                    clearance,
                    assessment
            );

            System.out.println("\nALSafe profile successfully updated!");
            return true;

        } catch (Exception e) {
            System.out.println("\nERROR: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Update ALSAFE Profile";
    }
}