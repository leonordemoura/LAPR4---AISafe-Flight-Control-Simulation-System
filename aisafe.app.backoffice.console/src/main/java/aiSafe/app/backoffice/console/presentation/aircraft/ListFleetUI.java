package aiSafe.app.backoffice.console.presentation.aircraft;

import aiSafe.aircraft.application.ListFleetController;
import aiSafe.aircraft.domain.Aircraft;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ListFleetUI extends AbstractUI {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListFleetUI.class);

    private final ListFleetController theController = new ListFleetController();

    @Override
    protected boolean doShow() {

        final List<AirTransportCompany> companies = theController.availableCompanies();

        System.out.println("\nAvailable Companies:");
        for (AirTransportCompany c : companies) {
            System.out.printf("  IATA: %s | %s%n", c.iataCode().code(), c.companyName().name());
        }

        final String iataCode = Console.readNonEmptyLine("Company IATA code:", "IATA code is mandatory.");

        final boolean validCompany = companies.stream()
                .anyMatch(c -> c.iataCode().code().equalsIgnoreCase(iataCode));

        if (!validCompany) {
            System.out.println("No company found with IATA code " + iataCode + ".");
            return false;
        }

        System.out.println("\nSelect filter:");
        System.out.println("  1 - No filter (full fleet)");
        System.out.println("  2 - By aircraft model");
        System.out.println("  3 - By maker");
        System.out.println("  4 - By minimum capacity");
        System.out.println("  5 - By maximum capacity");
        System.out.println("  6 - By maximum age (years)");

        final int choice = Console.readInteger("Option:");

        List<Aircraft> fleet;

        try {
            fleet = switch (choice) {
                case 1 -> theController.getFleet(iataCode);
                case 2 -> {
                    final String model = Console.readNonEmptyLine(
                            "Aircraft model name:", "Model name is mandatory.");
                    yield theController.getFleetByModel(iataCode, model);
                }
                case 3 -> {
                    final String maker = Console.readNonEmptyLine(
                            "Maker name:", "Maker name is mandatory.");
                    yield theController.getFleetByMaker(iataCode, maker);
                }
                case 4 -> {
                    final int min = Console.readInteger("Minimum number of seats:");
                    yield theController.getFleetByMinCapacity(iataCode, min);
                }
                case 5 -> {
                    final int max = Console.readInteger("Maximum number of seats:");
                    yield theController.getFleetByMaxCapacity(iataCode, max);
                }
                case 6 -> {
                    final int age = Console.readInteger("Maximum age in years:");
                    yield theController.getFleetByMaxAge(iataCode, age);
                }
                default -> {
                    System.out.println("Invalid option.");
                    yield List.of();
                }
            };

        } catch (final IllegalStateException | IllegalArgumentException ex) {
            LOGGER.debug("Business rule violation: {}", ex.getMessage());
            System.out.println("Unable to list fleet: " + ex.getMessage());
            return false;
        } catch (final Exception ex) {
            LOGGER.error("Unexpected error", ex);
            System.out.println("Unexpected error: " + ex.getMessage());
            return false;
        }

        displayFleet(fleet);
        return false;
    }

    @Override
    public String headline() {
        return "List Aircraft Fleet";
    }

    private void displayFleet(final List<Aircraft> fleet) {

        System.out.println();

        if (fleet.isEmpty()) {
            System.out.println("No aircraft found for the selected criteria.");
            return;
        }

        System.out.printf("Found %d aircraft(s):%n%n", fleet.size());

        System.out.printf("  %-12s | %-16s | %-14s | %-14s | %-6s | %s%n",
                "Registration", "Model", "Maker", "Status", "Age", "Seats (F/J/Y)");

        System.out.println("  " + "-".repeat(80));

        for (Aircraft a : fleet) {

            final int age = a.age();

            final int total = a.cabinConfiguration().firstClassSeats()
                    + a.cabinConfiguration().businessSeats()
                    + a.cabinConfiguration().economySeats();

            System.out.printf("  %-12s | %-16s | %-14s | %-14s | %-6d | %d (%d/%d/%d)%n",
                    a.registration().value(),
                    a.aircraftModel().name().value(),
                    a.aircraftModel().maker().name().name(),
                    a.status().name(),
                    age,
                    total,
                    a.cabinConfiguration().firstClassSeats(),
                    a.cabinConfiguration().businessSeats(),
                    a.cabinConfiguration().economySeats());
        }

        System.out.println("  " + "-".repeat(80));
    }
}