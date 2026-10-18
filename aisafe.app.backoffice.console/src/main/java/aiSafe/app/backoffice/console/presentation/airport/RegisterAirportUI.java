package aiSafe.app.backoffice.console.presentation.airport;

import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.aircraftmanagement.application.RegisterEngineModelController;
import aiSafe.aircraftmanagement.domain.*;
import aiSafe.airport.application.RegisterAirportController;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * User interface class responsible for facilitating the registration of a new airport in the system. This class will interact with the user to gather necessary information about the airport and then delegate the registration process to the appropriate application service.
 * @author 2DE - Group 3
 * @version 1.0
 */

public class RegisterAirportUI extends AbstractUI {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RegisterAirportUI.class);

    private final RegisterAirportController theController;

    public RegisterAirportUI() {
        theController = new RegisterAirportController();
    }

    @Override
    protected boolean doShow() {

        try {

            String iata;
            while (true) {
                try {

                    iata = Console.readNonEmptyLine(
                            "IATA code:",
                            "IATA code is mandatory."
                    );

                    theController.validateIata(iata);
                    break;

                } catch (IllegalArgumentException ex) {

                    System.out.println("Invalid IATA: " + ex.getMessage());
                }
            }

            String icao;

            while (true) {
                try {

                    icao = Console.readNonEmptyLine(
                            "ICAO code:",
                            "ICAO code is mandatory."
                    );

                    theController.validateIcao(icao);
                    break;

                } catch (IllegalArgumentException ex) {

                    System.out.println("Invalid ICAO: " + ex.getMessage());
                }
            }

            final String airportName =
                    Console.readNonEmptyLine(
                            "Airport name:",
                            "Airport name is mandatory."
                    );

            final String town =
                    Console.readNonEmptyLine(
                            "Town:",
                            "Town is mandatory."
                    );

            final String countryName =
                    Console.readNonEmptyLine(
                            "Country name:",
                            "Country name is mandatory."
                    );

            final String countryCode =
                    Console.readNonEmptyLine(
                            "Country code:",
                            "Country code is mandatory."
                    );

            final Double elevation =
                    Console.readDouble("Elevation (meters):");

            final Iterable<AirControlArea> areas =
                    theController.allAirControlAreas();

            System.out.println("\nAvailable Air Control Areas:");

            int index = 1;
            final List<AirControlArea> areaList = new ArrayList<>();

            for (AirControlArea area : areas) {

                areaList.add(area);

                System.out.println(
                        index + ". " +
                                area.areaCode().value() +
                                " - " +
                                area.areaName().value() +
                                " | Boundary: " +
                                formatBoundary(area)
                );

                index++;
            }

            if (areaList.isEmpty()) {
                System.out.println("No Air Control Areas available.");
                return false;
            }

            final int option =
                    Console.readInteger("Choose an Air Control Area:");

            final AirControlArea selectedArea =
                    areaList.get(option - 1);

            Double latitude;
            Double longitude;

            while (true) {

                try {

                    latitude = Console.readDouble("Latitude:");
                    longitude = Console.readDouble("Longitude:");

                    theController.validateCoordinates(
                            latitude,
                            longitude,
                            selectedArea.areaCode().value()
                    );

                    break;

                } catch (IllegalArgumentException ex) {

                    System.out.println(
                            "Invalid coordinates: " + ex.getMessage()
                    );
                }
            }

            theController.registerAirport(
                    iata,
                    icao,
                    airportName,
                    town,
                    countryName,
                    countryCode,
                    elevation,
                    latitude,
                    longitude,
                    selectedArea.areaCode().value()
            );

            System.out.println("\nAirport successfully registered.");
            return true;

        } catch (final IllegalArgumentException ex) {

            System.out.println(
                    "Unable to register airport: "
                            + ex.getMessage()
            );

        } catch (final Exception ex) {

            LOGGER.error(
                    "Unexpected error while registering airport",
                    ex
            );

            System.out.println(
                    "Unexpected error while registering airport."
            );
        }

        return false;
    }

    @Override
    public String headline() {
        return "Register New Airport";
    }

    private String formatBoundary(final AirControlArea area) {

        final var coords = area.geoBoundary().coordinates();

        double minLat = coords.stream()
                .mapToDouble(c -> c.latitude())
                .min()
                .orElseThrow();

        double maxLat = coords.stream()
                .mapToDouble(c -> c.latitude())
                .max()
                .orElseThrow();

        double minLon = coords.stream()
                .mapToDouble(c -> c.longitude())
                .min()
                .orElseThrow();

        double maxLon = coords.stream()
                .mapToDouble(c -> c.longitude())
                .max()
                .orElseThrow();

        return "Lat: [" + minLat + " , " + maxLat + "] "
                + "Lon: [" + minLon + " , " + maxLon + "]";
    }
}