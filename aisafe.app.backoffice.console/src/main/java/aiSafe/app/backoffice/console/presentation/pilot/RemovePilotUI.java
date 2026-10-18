package aiSafe.app.backoffice.console.presentation.pilot;

import aiSafe.PilotCertification.application.RemovePilotController;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

public class RemovePilotUI extends AbstractUI {

    private final RemovePilotController controller =
            new RemovePilotController();

    @Override
    protected boolean doShow() {

        try {

            System.out.println(
                    "\n=== DEACTIVATE PILOT ===\n"
            );

            // =====================================================
            // PILOT ID
            // =====================================================

            final String pilotId =
                    Console.readNonEmptyLine(
                            "Pilot ID:",
                            "Pilot ID is mandatory"
                    );

            // =====================================================
            // CONFIRMATION
            // =====================================================

            final String confirmation =
                    Console.readLine(
                            "Confirm deactivation? (y/n): "
                    );

            if (!confirmation.equalsIgnoreCase("y")) {

                System.out.println(
                        "\nOperation cancelled."
                );

                return false;
            }

            // =====================================================
            // EXECUTE USE CASE
            // =====================================================

            controller.deactivatePilot(pilotId);

            // =====================================================
            // SUCCESS
            // =====================================================

            System.out.println(
                    "\nPilot successfully deactivated!"
            );

            return true;

        } catch (Exception e) {

            // =====================================================
            // ERROR FEEDBACK
            // =====================================================

            System.out.println(
                    "\nERROR: "
                            + e.getMessage()
            );

            return false;
        }
    }

    @Override
    public String headline() {
        return "Deactivate Pilot";
    }
}