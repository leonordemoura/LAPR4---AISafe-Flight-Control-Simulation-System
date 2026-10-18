package aiSafe.app.backoffice.console.presentation.aircraftmanagement;

import aiSafe.aircraftmanagement.application.RegisterEngineModelController;
import aiSafe.aircraftmanagement.domain.*;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * User interface class responsible for facilitating the registration of a new engine model in the system. This class will interact with the user to gather necessary information about the engine model and then delegate the registration process to the appropriate application service.
 * @author 2DE - Group 3
 * @version 1.0
 */

public class RegisterEngineModelUI extends AbstractUI {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterEngineModelUI.class);

    private final RegisterEngineModelController theController;

    public RegisterEngineModelUI(){
        theController = new RegisterEngineModelController();
    }

    @Override
    protected boolean doShow() {
        final String fakeModelName = Console.readNonEmptyLine("Model name:", "Model name is mandatory.");
        final Long makerID = Console.readLong("Maker ID: ");
        final MotorizationType engineType = readEngineType();
        final FuelTypeName fuelType = readFuelType();
        final Integer fakePower = Console.readInteger("Power Value: ");
        final Unit unitPower = readUnitPower();
        final Double fakeTSFC = Console.readDouble("TSFC value:");
        try {
            EngineModelName modelName = EngineModelName.valueOf(fakeModelName);
            Power power = Power.valueOf(fakePower, unitPower);
            TSFC tsfc = TSFC.valueOf(fakeTSFC);
            theController.registerEngineModel(modelName, power, tsfc, fuelType, engineType, makerID);
            return true;
        } catch (final IllegalArgumentException ex) {
            System.out.println("Unable to register engine model: " + ex.getMessage());
        } catch (final Exception ex) {
            System.out.println("Unexpected error while registering engine model.");
        }
        return false;
    }

    private FuelTypeName readFuelType() {
        final List<FuelType> fuelTypes = theController.getFuelTypes();
        if (fuelTypes == null || fuelTypes.isEmpty()) {
            throw new IllegalStateException("No fuel types available. Please run bootstrap or register fuel types first.");
        }

        System.out.println("Fuel type options:");
        for (int i = 0; i < fuelTypes.size(); i++) {
            final FuelType ft = fuelTypes.get(i);
            System.out.println(" " + (i + 1) + ". " + ft.type().type());
        }

        while (true) {
            final String input = Console.readNonEmptyLine("Fuel type (1-" + fuelTypes.size() + "):", "Fuel type is mandatory.");
            try {
                final int choice = Integer.parseInt(input.trim());
                if (choice >= 1 && choice <= fuelTypes.size()) {
                    return fuelTypes.get(choice - 1).type();
                }
            } catch (final NumberFormatException ignored) {}

            System.out.println("Invalid choice. Please enter a number between 1 and " + fuelTypes.size() + ".");
        }
    }

    private MotorizationType readEngineType() {
        return readEnumChoice("Engine type", MotorizationType.class);
    }

    private Unit readUnitPower() {
        return readEnumChoice("Power unit", Unit.class);
    }

    private <T extends Enum<T>> T readEnumChoice(String label, Class<T> enumClass) {
        final T[] values = enumClass.getEnumConstants();

        System.out.println(label + " options:");
        for (int i = 0; i < values.length; i++) {
            System.out.println(" " + (i + 1) + ". " + values[i]);
        }

        while (true) {
            final String input = Console.readNonEmptyLine(label + " (1-" + values.length + "):", label + " is mandatory.");
            try {
                final int choice = Integer.parseInt(input.trim());
                if (choice >= 1 && choice <= values.length) {
                    return values[choice - 1];
                }
            } catch (final NumberFormatException ignored) {}

            System.out.println("Invalid choice. Please enter a number between 1 and " + values.length + ".");
        }
    }


    @Override
    public String headline() {
        return "Register New Engine Model";
    }
}
