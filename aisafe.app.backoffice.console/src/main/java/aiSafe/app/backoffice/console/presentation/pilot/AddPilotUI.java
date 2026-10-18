package aiSafe.app.backoffice.console.presentation.pilot;

import aiSafe.PilotCertification.application.AddPilotController;
import aiSafe.PilotCertification.domain.ModelCertification;

import aiSafe.aircraftModel.domain.AircraftModelId;

import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import java.util.ArrayList;
import java.util.List;

public class AddPilotUI extends AbstractUI {

    private final AddPilotController controller =
            new AddPilotController();

    @Override
    protected boolean doShow() {

        try {

            System.out.println("\n=== ADD PILOT ===\n");

            // =====================================================
            // BASIC DATA
            // =====================================================

            final String pilotId =
                    Console.readNonEmptyLine(
                            "Pilot ID:",
                            "Pilot ID is mandatory"
                    );

            final String email =
                    Console.readNonEmptyLine(
                            "Pilot User Email:",
                            "Email is mandatory"
                    );

            // =====================================================
            // CERTIFICATIONS
            // =====================================================

            final List<ModelCertification> certifications =
                    new ArrayList<>();

            System.out.println(
                    "\nInsert Pilot Certifications"
            );

            boolean addMore = true;

            while (addMore) {

                // =============================================
                // AIRCRAFT MODEL
                // =============================================

                final String aircraftModelId =
                        Console.readNonEmptyLine(
                                "Aircraft Model ID:",
                                "Aircraft Model ID is mandatory"
                        );

                // =============================================
                // CERTIFICATION DATE
                // =============================================

                LocalDate certificationDate = null;

                while (certificationDate == null) {

                    try {

                        final String date =
                                Console.readNonEmptyLine(
                                        "Certification Date (YYYY-MM-DD):",
                                        "Date is mandatory"
                                );

                        certificationDate =
                                LocalDate.parse(date);

                    } catch (DateTimeParseException e) {

                        System.out.println(
                                "Invalid date format."
                        );
                    }
                }

                // =============================================
                // CREATE CERTIFICATION
                // =============================================

                certifications.add(

                        new ModelCertification(
                                new AircraftModelId(
                                        aircraftModelId
                                ),
                                certificationDate
                        )
                );

                // =============================================
                // CONTINUE?
                // =============================================

                final String answer =
                        Console.readLine(
                                "Add another certification? (y/n): "
                        );

                addMore =
                        answer.equalsIgnoreCase("y");
            }

            // =====================================================
            // SAFETY CHECK
            // =====================================================

            if (certifications.isEmpty()) {

                System.out.println(
                        "\nPilot must have at least one certification."
                );

                return false;
            }

            // =====================================================
            // CREATE PILOT
            // =====================================================

            controller.addPilot(
                    pilotId,
                    email,
                    certifications
            );

            System.out.println(
                    "\nPilot successfully created!"
            );

            return true;

        } catch (Exception e) {

            System.out.println(
                    "\nERROR: "
                            + e.getMessage()
            );

            return false;
        }
    }

    @Override
    public String headline() {
        return "Add Pilot";
    }
}