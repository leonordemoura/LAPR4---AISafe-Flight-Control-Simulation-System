package aiSafe.app.backoffice.console.presentation.flight;

import aiSafe.flight.application.InsertWeatherDataController;
import aiSafe.flight.domain.Flight;
import aiSafe.flight.domain.FlightDesignator;
import aiSafe.flight.domain.FlightPlan;
import aiSafe.flight.domain.FlightPlanStatus;
import aiSafe.weatherdata.domain.WeatherData;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * US082 — Insert weather data in a flight plan.
 */
public class InsertWeatherDataUI extends AbstractUI {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertWeatherDataUI.class);

    private final InsertWeatherDataController theController = new InsertWeatherDataController();

    @Override
    protected boolean doShow() {
        try {

            // ── Step 1: list flights with pending plans ───────────────────────
            final List<Flight> flights = theController.pendingFlights();

            if (flights.isEmpty()) {
                System.out.println("No flights with pending plans found.");
                return false;
            }

            System.out.println("\nFlights with pending plans:");
            for (int i = 0; i < flights.size(); i++) {
                final Flight f = flights.get(i);
                final FlightPlan plan = f.flightPlan();
                System.out.printf("  %d. %-10s [%-10s]  route: %-12s  departure: %s%n",
                        i + 1,
                        f.designator(),
                        plan.status(),
                        plan.routeSignature() != null ? plan.routeSignature() : "N/A",
                        plan.departureDateTime() != null ? plan.departureDateTime().value() : "N/A");
            }

            final int flightChoice = Console.readInteger("Choose a flight:");
            if (flightChoice < 1 || flightChoice > flights.size()) {
                System.out.println("Invalid choice.");
                return false;
            }

            final Flight selectedFlight = flights.get(flightChoice - 1);
            final FlightPlan selectedPlan = selectedFlight.flightPlan();
            final FlightDesignator designator = selectedFlight.designator();

            // ── Step 2: warn if VALIDATED — test will be voided ───────────────
            if (selectedPlan.status() == FlightPlanStatus.VALIDATED) {
                System.out.println("\n  WARNING: This flight plan has already been tested (VALIDATED).");
                System.out.println("  Inserting weather data will void the test and revert status to DRAFT.");
                final String confirm = Console.readNonEmptyLine("Proceed? (yes/no):", "Answer is required.");
                if (!confirm.equalsIgnoreCase("yes")) {
                    System.out.println("Operation cancelled.");
                    return false;
                }
            }

            // ── Step 3: list ALL available weather data in the system ─────────
            final List<WeatherData> weatherList = theController.allAvailableWeatherData();

            if (weatherList.isEmpty()) {
                System.out.println("\nNo weather data available in the system.");
                System.out.println("Please ask the Weather Person to register weather forecasts first.");
                return false;
            }

            System.out.printf("  %-4s %-8s %-6s %-8s %-18s %-18s %-10s %-10s%n",
                    "#", "ID", "Area", "Source", "Start", "End", "Wind Direction", "Wind Speed");
            System.out.println("  " + "-".repeat(90));

            for (int i = 0; i < weatherList.size(); i++) {
                final WeatherData wd = weatherList.get(i);
                System.out.printf("  %-4d %-8d %-6s %-8s %-18s %-18s %-10s %-10s%n",
                        i + 1,
                        wd.identity(),
                        wd.areaCode().value(),
                        wd.weatherSource().value(),
                        wd.observationDate().startDate(),
                        wd.observationDate().endDate(),
                        wd.windDirection().value() + "°",
                        wd.windSpeed().value() + " " + wd.windSpeed().unit());
            }

            final int weatherChoice = Console.readInteger("Choose a weather forecast:");
            if (weatherChoice < 1 || weatherChoice > weatherList.size()) {
                System.out.println("Invalid choice.");
                return false;
            }

            final WeatherData selectedWeather = weatherList.get(weatherChoice - 1);

            // ── Step 4: insert ────────────────────────────────────────────────
            final FlightPlan updated = theController.insertWeatherData(designator, selectedWeather.identity());

            System.out.println("\nWeather data successfully inserted into flight plan.");
            System.out.printf("Flight plan status: %s%n", updated.status());

            if (updated.status() == FlightPlanStatus.DRAFT
                    && selectedPlan.isValidated()) {
                System.out.println("Note: Previous test has been voided — plan reverted to DRAFT.");
            }

            return true;

        } catch (final IllegalArgumentException ex) {
            System.out.println("Unable to insert weather data: " + ex.getMessage());
        } catch (final IllegalStateException ex) {
            System.out.println("Operation not allowed: " + ex.getMessage());
        } catch (final Exception ex) {
            LOGGER.error("Unexpected error while inserting weather data", ex);
            System.out.println("Unexpected error while inserting weather data.");
        }

        return false;
    }

    @Override
    public String headline() {
        return "Insert Weather Data in Flight Plan";
    }
}