package aiSafe.app.backoffice.console.presentation.flight;

import aiSafe.flight.application.RegisterFlightPlanController;
import aiSafe.flight.application.RegisterFlightPlanDTO;
import aiSafe.flight.application.RegisterFlightPlanLegInput;
import aiSafe.flight.application.RegisterFlightPlanSegmentInput;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class RegisterFlightPlanUI extends AbstractUI {

    private final RegisterFlightPlanController controller = new RegisterFlightPlanController();

    @Override
    protected boolean doShow() {
        try {
            System.out.println("=== Register Flight Plan ===");

            final String airlineCode = Console.readNonEmptyLine(
                    "Flight airline code (IATA): ",
                    "Airline code is mandatory."
            ).toUpperCase();
            final int flightNumber = Console.readInteger("Flight number: ");
            final String operationalSuffix = Console.readLine("Operational suffix (optional): ");

            controller.validateFlight(airlineCode, flightNumber, operationalSuffix);
            System.out.println("Aircraft (from flight): " + controller.validatedFlightAircraftRegistration());
            controller.currentFlightPlanNotice().ifPresent(notice -> System.out.println(notice));

            final String pilotId = Console.readNonEmptyLine(
                    "Pilot ID (PilotCertification id, e.g. P-001): ",
                    "Pilot ID is mandatory."
            );
            controller.validatePilot(pilotId);

            final LocalDateTime departureDateTime = readDepartureDateTime();
            final LocalDateTime arrivalDateTime = readArrivalDateTime(departureDateTime);
            final double fuelQuantity = Console.readDouble("Operational fuel quantity:");
            final String fuelUnit = Console.readNonEmptyLine("Operational fuel unit (e.g. kg): ", "Fuel unit is mandatory.");

            final DslInputResult dslInput = collectDslInput();

            final RegisterFlightPlanDTO dto = new RegisterFlightPlanDTO(
                    airlineCode,
                    flightNumber,
                    operationalSuffix,
                    pilotId,
                    departureDateTime,
                    arrivalDateTime,
                    fuelQuantity,
                    fuelUnit,
                    dslInput.dslContent(),
                    dslInput.legs(),
                    dslInput.flightTypeForDsl()
            );

            controller.registerFlightPlan(dto);
            System.out.println("Flight plan registered (status: DRAFT).");
            return true;
        } catch (final IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Error: " + ex.getMessage());
        } catch (final Exception ex) {
            System.out.println("Unexpected error while registering flight plan: " + ex.getMessage());
        }
        return false;
    }

    private DslInputResult collectDslInput() throws Exception {
        System.out.println();
        System.out.println("Core Flight DSL:");
        System.out.println(" 1. Enter legs and segments manually");
        System.out.println(" 2. Load from file (.flight)");

        while (true) {
            final String choice = Console.readNonEmptyLine("Select option (1-2): ", "Option is mandatory.");
            switch (choice.trim()) {
                case "1" -> {
                    return collectManualLegs();
                }
                case "2" -> {
                    return new DslInputResult(readDslFromFile(), List.of(), null);
                }
                default -> System.out.println("Invalid option. Enter 1 or 2.");
            }
        }
    }

    private static String readDslFromFile() throws Exception {
        final String pathValue = Console.readNonEmptyLine(
                "Path to DSL file (.flight): ",
                "File path is mandatory.");
        final Path path = Path.of(pathValue.trim());
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("File not found: " + path.toAbsolutePath().normalize());
        }
        final String content = Files.readString(path, StandardCharsets.UTF_8);
        if (content.isBlank()) {
            throw new IllegalArgumentException("DSL file is empty.");
        }
        return content;
    }

    private DslInputResult collectManualLegs() {
        final String flightType = Console.readNonEmptyLine(
                "Flight type for DSL (REGULAR/CHARTER): ",
                "Flight type is mandatory."
        ).toUpperCase();

        final int legCount = Console.readInteger("Number of legs:");
        if (legCount < 1) {
            throw new IllegalArgumentException("At least one leg is required.");
        }

        final List<RegisterFlightPlanLegInput> legs = new ArrayList<>();
        for (int i = 1; i <= legCount; i++) {
            System.out.println("--- Leg " + i + " ---");
            final String depAirport = Console.readNonEmptyLine("Departure airport (e.g. OPO): ", "Mandatory.");
            final String depDate = Console.readNonEmptyLine("Departure date (yyyy-MM-dd): ", "Mandatory.");
            final String depTime = Console.readNonEmptyLine("Departure time (HH:mm): ", "Mandatory.");
            final String arrAirport = Console.readNonEmptyLine("Arrival airport: ", "Mandatory.");
            final String arrDate = Console.readNonEmptyLine("Arrival date (yyyy-MM-dd): ", "Mandatory.");
            final String arrTime = Console.readNonEmptyLine("Arrival time (HH:mm): ", "Mandatory.");
            final String routeOrigin = Console.readNonEmptyLine("Route origin airport: ", "Mandatory.");
            final String routeDestination = Console.readNonEmptyLine("Route destination airport: ", "Mandatory.");
            final double legFuel = Console.readDouble("Leg fuel amount:");
            final String legFuelUnit = Console.readNonEmptyLine("Leg fuel unit (kg/l): ", "Mandatory.");

            final int segmentCount = Console.readInteger("Number of segments in this leg:");
            if (segmentCount < 1) {
                throw new IllegalArgumentException("Each leg needs at least one segment.");
            }

            final List<RegisterFlightPlanSegmentInput> segments = new ArrayList<>();
            for (int s = 1; s <= segmentCount; s++) {
                System.out.println("  Segment " + s + ":");
                final double startLat = Console.readDouble("  Start latitude:");
                final double startLon = Console.readDouble("  Start longitude:");
                final double endLat = Console.readDouble("  End latitude:");
                final double endLon = Console.readDouble("  End longitude:");
                final double altitude = Console.readDouble("  Altitude (m):");
                final double widthKm = Console.readDouble("  Corridor width (km, e.g. 10):");
                final double windDirectionDeg = Console.readDouble("  Wind direction (deg, 0-359):");
                final double windSpeed = Console.readDouble("  Wind speed:");
                final String windUnit = readWindUnit();
                segments.add(new RegisterFlightPlanSegmentInput(
                        startLat, startLon, endLat, endLon,
                        altitude, widthKm, windDirectionDeg, windSpeed, windUnit));
            }

            legs.add(new RegisterFlightPlanLegInput(
                    depAirport, depDate, depTime,
                    arrAirport, arrDate, arrTime,
                    routeOrigin, routeDestination,
                    legFuel, legFuelUnit,
                    segments
            ));
        }

        return new DslInputResult(null, legs, flightType);
    }

    private static String readWindUnit() {
        while (true) {
            final String raw = Console.readNonEmptyLine(
                    "  Wind unit (mps / kmh / kt): ",
                    "Wind unit is mandatory."
            ).trim().toLowerCase();
            if (raw.equals("mps") || raw.equals("kmh") || raw.equals("kt")) {
                return raw;
            }
            System.out.println("Invalid wind unit. Use mps, kmh, or kt.");
        }
    }

    private LocalDateTime readArrivalDateTime(final LocalDateTime departure) {
        final String example = departure.plusHours(1).withSecond(0).withNano(0).toString();
        while (true) {
            final String raw = Console.readNonEmptyLine(
                    "Arrival date/time (ISO-8601, after departure, e.g. " + example + "): ",
                    "Arrival date/time is mandatory."
            );
            try {
                final LocalDateTime parsed = LocalDateTime.parse(raw.trim());
                if (!parsed.isAfter(departure)) {
                    System.out.println("Arrival must be after departure (" + departure + ").");
                    continue;
                }
                return parsed;
            } catch (final DateTimeParseException ex) {
                System.out.println("Invalid format. Use ISO-8601, e.g. " + example);
            }
        }
    }

    private LocalDateTime readDepartureDateTime() {
        final String example = LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0).toString();
        while (true) {
            final String raw = Console.readNonEmptyLine(
                    "Departure date/time (ISO-8601, future, e.g. " + example + "): ",
                    "Departure date/time is mandatory."
            );
            try {
                final LocalDateTime parsed = LocalDateTime.parse(raw.trim());
                if (parsed.isBefore(LocalDateTime.now())) {
                    System.out.println(
                            "That date/time is already in the past (now is "
                                    + LocalDateTime.now()
                                    + "). Use a later value, e.g. " + example);
                    continue;
                }
                return parsed;
            } catch (final DateTimeParseException ex) {
                System.out.println("Invalid format. Use ISO-8601, e.g. " + example);
            }
        }
    }

    @Override
    public String headline() {
        return "Register Flight Plan";
    }

    private record DslInputResult(String dslContent, List<RegisterFlightPlanLegInput> legs, String flightTypeForDsl) {}
}
