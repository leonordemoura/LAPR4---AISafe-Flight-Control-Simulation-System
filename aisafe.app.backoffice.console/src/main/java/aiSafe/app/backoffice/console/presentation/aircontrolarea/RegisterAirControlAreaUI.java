package aiSafe.app.backoffice.console.presentation.aircontrolarea;

import aiSafe.aircontrolarea.application.RegisterAirControlAreaController;
import aiSafe.aircontrolarea.domain.Coordinate;
import aiSafe.aircontrolarea.domain.GeoBoundary;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.io.util.Console;

import java.util.List;

public class RegisterAirControlAreaUI extends AbstractUI {
    private final RegisterAirControlAreaController controller = new RegisterAirControlAreaController();

    @Override
    protected boolean doShow() {
        try {
            final String areaCode = Console.readNonEmptyLine("Area code:", "Area code is mandatory.");
            controller.checkAreaCodeExists(areaCode);

            final String areaName = Console.readNonEmptyLine("Area name:", "Area name is mandatory.");
            controller.checkAreaNameExists(areaName);

            final double minFuelAtArrival = Console.readDouble("Minimum fuel at arrival:");

            final Coordinate p1 = readCoordinate(1);
            final Coordinate p2 = readCoordinate(2);
            final Coordinate p3 = readCoordinate(3);
            final Coordinate p4 = readCoordinate(4);

            final GeoBoundary geoBoundary = new GeoBoundary(List.of(p1, p2, p3, p4));
            controller.registerAirControlArea(areaCode, areaName, minFuelAtArrival, List.of(geoBoundary));
            System.out.println("Air control area registered successfully.");
        } catch (final IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        } catch (final Exception ex) {
            System.out.println("Unexpected error while registering air control area: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public String headline() {
        return "Register Air Control Area";
    }

    private Coordinate readCoordinate(final int index) {
        final double latitude = Console.readDouble("Coordinate " + index + " latitude:");
        final double longitude = Console.readDouble("Coordinate " + index + " longitude:");
        return new Coordinate(latitude, longitude);
    }
}
