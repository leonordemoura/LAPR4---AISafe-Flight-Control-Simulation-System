package aiSafe.app.backoffice.console.presentation.aircraftModel;

import aiSafe.aircraftModel.application.RegisterAircraftModelController;
import aiSafe.aircraftModel.domain.*;
import aiSafe.aircraftmanagement.domain.EngineModel;
import aiSafe.aircraftmanagement.domain.Maker;
import aiSafe.aircraftmanagement.domain.MotorizationType;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.ArrayList;
import java.util.List;

public class RegisterAircraftModelUI extends AbstractUI {

    private final RegisterAircraftModelController controller =
            new RegisterAircraftModelController();

    @Override
    protected boolean doShow() {

        try {

            // =========================================
            // AIRCRAFT MODEL ID
            // =========================================

            final String aircraftModelId = Console.readNonEmptyLine(
                    "Aircraft Model ID:",
                    "ID is mandatory"
            );

            // =========================================
            // MAXIMUM SEATS
            // =========================================

            int maxSeatsVal;

            do {
                maxSeatsVal = Console.readInteger("Maximum Seats:");
            } while (maxSeatsVal <= 0);

            final MaximumSeats maximumSeats =
                    new MaximumSeats(maxSeatsVal);

            // =========================================
            // NAME
            // =========================================

            final String name = Console.readNonEmptyLine(
                    "Aircraft Model Name:",
                    "Name is mandatory"
            );

            // =========================================
            // MAKERS
            // =========================================

            System.out.println("\nAVAILABLE MAKERS:");

            for (Maker m : controller.allMakers()) {
                System.out.println(
                        m.makerID().toString()
                                + " - "
                                + m.name()
                );
            }

            final Long makerId = Console.readLong("\nChoose Maker ID:");

            // =========================================
            // ENGINE SELECTION
            // =========================================

            final List<Long> engineIds = new ArrayList<>();

            MotorizationType selectedType = null;

            System.out.println("\nAVAILABLE ENGINES:");

            for (EngineModel e : controller.allEngineModels()) {

                System.out.println(
                        e.identity()
                                + " - "
                                + e.modelName()
                                + " ["
                                + e.engineType()
                                + "]"
                );
            }

            System.out.println(
                    "\nSelect Engine IDs (0 to finish)"
            );

            while (true) {

                Long id = Console.readLong("Engine ID:");

                if (id == 0) {

                    if (engineIds.isEmpty()) {
                        System.out.println("At least one engine required.");
                        continue;
                    }

                    break;
                }

                EngineModel selectedEngine = null;

                for (EngineModel e : controller.allEngineModels()) {

                    if (e.identity().equals(id)) {
                        selectedEngine = e;
                        break;
                    }
                }

                if (selectedEngine == null) {
                    System.out.println("Engine not found.");
                    continue;
                }

                // FIRST ENGINE DEFINES TYPE

                if (selectedType == null) {

                    selectedType = selectedEngine.engineType();

                    System.out.println(
                            "Motorization type locked to: "
                                    + selectedType
                    );

                } else {

                    if (selectedEngine.engineType() != selectedType) {

                        System.out.println(
                                "All engines must be "
                                        + selectedType
                        );

                        continue;
                    }
                }

                engineIds.add(id);

                System.out.println("Engine added.");
            }

            // =========================================
            // AIRCRAFT TYPE
            // =========================================

            AircraftType type = null;

            while (type == null) {

                System.out.println("\nAircraft Types:");
                System.out.println("1 - PASSENGER");
                System.out.println("2 - CARGO");
                System.out.println("3 - MIXED");

                switch (Console.readInteger("Select type:")) {

                    case 1 -> type = AircraftType.PASSENGER;
                    case 2 -> type = AircraftType.CARGO;
                    case 3 -> type = AircraftType.MIXED;

                    default -> System.out.println("Invalid option.");
                }
            }

            // =========================================
            // ALTITUDE
            // =========================================

            double altitudeVal;

            do {
                altitudeVal = Console.readDouble("Service Ceiling:");
            } while (altitudeVal <= 0);

            final AltitudeUnit altitudeUnit =
                    chooseAltitudeUnit();

            // =========================================
            // SPEED
            // =========================================

            double speedVal;

            do {
                speedVal = Console.readDouble("Cruise Speed:");
            } while (speedVal <= 0);

            final SpeedUnit speedUnit =
                    chooseSpeedUnit();

            // =========================================
            // AREA
            // =========================================

            double wingAreaVal;

            do {
                wingAreaVal = Console.readDouble("Wing Area:");
            } while (wingAreaVal <= 0);

            final AreaUnit areaUnit =
                    chooseAreaUnit();

            // =========================================
            // WEIGHTS
            // =========================================

            final WeightUnit weightUnit =
                    chooseWeightUnit();

            double emptyWeightVal;

            do {
                emptyWeightVal = Console.readDouble("Empty Weight:");
            } while (emptyWeightVal <= 0);

            double mtowVal;

            do {
                mtowVal = Console.readDouble("MTOW:");
            } while (mtowVal <= emptyWeightVal);

            double mzfwVal;

            do {
                mzfwVal = Console.readDouble("MZFW:");
            } while (mzfwVal > mtowVal);

            double fuelVal;

            do {
                fuelVal = Console.readDouble("Fuel Capacity:");
            } while (fuelVal <= 0);

            // =========================================
            // AERODYNAMICS
            // =========================================

            double dragVal;

            do {
                dragVal = Console.readDouble("Drag Coefficient:");
            } while (dragVal <= 0);

            double liftVal;

            do {
                liftVal = Console.readDouble("Lift Coefficient:");
            } while (liftVal <= 0);

            // =========================================
            // BUILD SPECS
            // =========================================

            final PhysicalSpecs specs =
                    new PhysicalSpecs(

                            new Altitude(
                                    altitudeVal,
                                    altitudeUnit
                            ),

                            new Speed(
                                    speedVal,
                                    speedUnit
                            ),

                            new Area(
                                    wingAreaVal,
                                    areaUnit
                            ),

                            new Weight(
                                    emptyWeightVal,
                                    weightUnit
                            ),

                            new Weight(
                                    mtowVal,
                                    weightUnit
                            ),

                            new Weight(
                                    mzfwVal,
                                    weightUnit
                            ),

                            new Weight(
                                    fuelVal,
                                    weightUnit
                            ),

                            new LiftCoefficient(liftVal),

                            new DragCoefficient(dragVal)
                    );

            // =========================================
            // CREATE
            // =========================================

            controller.registerAircraftModel(
                    aircraftModelId,
                    name,
                    makerId,
                    engineIds,
                    specs,
                    type,
                    maximumSeats
            );

            System.out.println(
                    "\nAircraft Model successfully created!"
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

    // =====================================================
    // UNIT HELPERS
    // =====================================================

    // =====================================================
// UNIT HELPERS
// =====================================================

    private AltitudeUnit chooseAltitudeUnit() {

        while (true) {

            System.out.println("\nAltitude Units:");

            AltitudeUnit[] values = AltitudeUnit.values();

            for (int i = 0; i < values.length; i++) {

                System.out.println(
                        (i + 1) + " - " + values[i]
                );
            }

            int option = Console.readInteger("Choose option:");

            if (option >= 1 && option <= values.length) {
                return values[option - 1];
            }

            System.out.println("Invalid option.");
        }
    }

    private SpeedUnit chooseSpeedUnit() {

        while (true) {

            System.out.println("\nSpeed Units:");

            SpeedUnit[] values = SpeedUnit.values();

            for (int i = 0; i < values.length; i++) {

                System.out.println(
                        (i + 1) + " - " + values[i]
                );
            }

            int option = Console.readInteger("Choose option:");

            if (option >= 1 && option <= values.length) {
                return values[option - 1];
            }

            System.out.println("Invalid option.");
        }
    }

    private AreaUnit chooseAreaUnit() {

        while (true) {

            System.out.println("\nArea Units:");

            AreaUnit[] values = AreaUnit.values();

            for (int i = 0; i < values.length; i++) {

                System.out.println(
                        (i + 1) + " - " + values[i]
                );
            }

            int option = Console.readInteger("Choose option:");

            if (option >= 1 && option <= values.length) {
                return values[option - 1];
            }

            System.out.println("Invalid option.");
        }
    }

    private WeightUnit chooseWeightUnit() {

        while (true) {

            System.out.println("\nWeight Units:");

            WeightUnit[] values = WeightUnit.values();

            for (int i = 0; i < values.length; i++) {

                System.out.println(
                        (i + 1) + " - " + values[i]
                );
            }

            int option = Console.readInteger("Choose option:");

            if (option >= 1 && option <= values.length) {
                return values[option - 1];
            }

            System.out.println("Invalid option.");
        }
    }

    @Override
    public String headline() {
        return "Register Aircraft Model";
    }
}