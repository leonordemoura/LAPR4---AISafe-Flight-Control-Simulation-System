package aiSafe.app.backoffice.console.presentation.aircraft;

import aiSafe.aircraft.application.AddAircraftController;
import aiSafe.aircraft.domain.AircraftStatus;
import aiSafe.aircraftModel.domain.AircraftModel;
import aiSafe.aircraftmanagement.domain.EngineModel;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AddAircraftUI extends AbstractUI {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddAircraftUI.class);

    private final AddAircraftController theController = new AddAircraftController();

    @Override
    protected boolean doShow() {

        final String registration = Console.readNonEmptyLine(
                "Aircraft Registration Number:", "Registration is mandatory.");

        if (theController.registrationExists(registration)) {
            System.out.println("Unable to add aircraft: Aircraft registration already exists");
            return false;
        }

        System.out.println("\nAvailable Aircraft Models:");
        for (AircraftModel m : theController.availableModels()) {
            System.out.printf("  ID: %s | %s | Max Seats: %d%n",
                    m.identity().value(),
                    m.name().toString(),
                    m.maximumSeats().seats());
        }

        final String modelId = Console.readNonEmptyLine(
                "Aircraft Model ID:", "Model ID is mandatory.");

        AircraftModel model = null;

        for (AircraftModel m : theController.availableModels()) {
            if (m.identity().value().equals(modelId)) {
                model = m;
                break;
            }
        }

        if (model == null) {
            System.out.println("Invalid aircraft model selected.");
            return false;
        }

        System.out.println("\nAvailable Engine Models:");
        for (EngineModel e : theController.availableEngineModels()) {
            System.out.printf("  ID: %s | %s%n",
                    e.modelName().name(),
                    e.engineType().name());
        }

        final String enginesInput = Console.readNonEmptyLine(
                "Engine Model ID(s) (comma-separated):", "At least one engine is mandatory.");

        final List<String> engineIds = List.of(enginesInput.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (engineIds.isEmpty()) {
            System.out.println("At least one engine must be provided.");
            return false;
        }

        final int firstClass = Console.readInteger("Number of First Class seats:");
        final int business   = Console.readInteger("Number of Business Class seats:");
        final int economy    = Console.readInteger("Number of Economy Class seats:");

        int totalSeats = firstClass + business + economy;

        if (totalSeats > model.maximumSeats().seats()) {
            System.out.println("Invalid seats: total exceeds model capacity (" +
                    model.maximumSeats().seats() + ")");
            return false;
        }

        final int registrationYear = Console.readInteger("Registration Year:");

        final String countryName = Console.readNonEmptyLine(
                "Country of Registration (name):", "Country name is mandatory.");

        final int countryCode = Console.readInteger("Country Numeric Code:");

        System.out.println("\nOperational Status:");
        final AircraftStatus[] statuses = AircraftStatus.values();

        for (int i = 0; i < statuses.length; i++) {
            System.out.printf("  %d - %s%n", i + 1, statuses[i]);
        }

        final int statusChoice = Console.readInteger("Choose status (number):");

        if (statusChoice < 1 || statusChoice > statuses.length) {
            System.out.println("Invalid status choice.");
            return false;
        }

        final AircraftStatus status = statuses[statusChoice - 1];

        System.out.println("\nAvailable Companies:");
        for (AirTransportCompany c : theController.availableCompanies()) {
            System.out.printf("  IATA: %s | %s%n",
                    c.iataCode().code(),
                    c.companyName().name());
        }

        final String iataCode = Console.readNonEmptyLine("Company IATA code:", "IATA code is mandatory.");

        try {
            theController.addAircraft(
                    registration,
                    modelId,
                    engineIds,
                    firstClass,
                    business,
                    economy,
                    registrationYear,
                    countryName,
                    countryCode,
                    status,
                    iataCode
            );

            System.out.println("\nAircraft successfully added to the fleet.");
            return true;

        } catch (final IllegalStateException | IllegalArgumentException ex) {
            LOGGER.debug("Business rule violation: {}", ex.getMessage());
            System.out.println("Unable to add aircraft: " + ex.getMessage());

        } catch (final Exception ex) {
            LOGGER.error("Unexpected error", ex);
            System.out.println("Unexpected error: " + ex.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Add an Aircraft to the Fleet";
    }
}