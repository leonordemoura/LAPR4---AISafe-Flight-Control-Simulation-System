package aiSafe.infrastructure.remote;

import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.weatherdata.application.ConsultWeatherDataController;
import aiSafe.weatherdata.application.ConsultWeatherDataDTO;
import aiSafe.weatherdata.application.ImportBulkWeatherDataController;
import aiSafe.weatherdata.application.RegisterWeatherDataController;
import aiSafe.weatherdata.domain.PressureUnit;
import aiSafe.weatherdata.domain.TemperatureUnit;
import aiSafe.weatherdata.domain.WeatherData;
import aiSafe.weatherdata.domain.WindSpeedUnit;
import aiSafe.weatherdata.importer.WeatherDataImportResult;
import aiSafe.weatherdata.importer.csv.CsvWeatherDataImporter;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class WeatherRemoteCommandHandler implements RemoteCommandHandler {

    private static final String SERVICE_ID = "US44";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final RegisterWeatherDataController registerController = new RegisterWeatherDataController();
    private final ConsultWeatherDataController consultController = new ConsultWeatherDataController();
    private final ImportBulkWeatherDataController importController = createImportController();

    @Override
    public String handle(final String commandLine, final RemoteConnectionContext context) {
        final String[] parts = commandLine.split(";", -1);
        if (parts.length == 0 || parts[0].isBlank()) {
            return RemoteResponse.error("BAD_REQUEST", "Empty command");
        }

        final String command = parts[0].trim().toUpperCase();
        return switch (command) {
            case "LOGIN" -> handleLogin(parts, context);
            case "LOGOUT" -> handleLogout(context);
            case "REGISTER_WEATHER" -> requireAuth(context, () -> handleRegisterWeather(parts));
            case "CONSULT_WEATHER" -> requireAuth(context, () -> handleConsultWeather(parts));
            case "IMPORT_WEATHER_BULK" -> requireAuth(context, () -> handleImportBulk(parts));
            default -> RemoteResponse.error("BAD_REQUEST", "Unknown command: " + command);
        };
    }

    @Override
    public String serviceId() {
        return SERVICE_ID;
    }

    private String handleLogin(final String[] parts, final RemoteConnectionContext context) {
        if (parts.length < 3) {
            return RemoteResponse.error("BAD_REQUEST", "Usage: LOGIN;username;password");
        }
        final String username = parts[1].trim();
        final String password = parts[2];

        final var auth = AuthzRegistry.authenticationService();
        final var session = auth.authenticate(username, password, AisafeRoles.WEATHER_PERSON);
        if (session.isEmpty()) {
            context.udpLogger().log(username, context.clientIp(), context.clientPort(), SERVICE_ID, "LOGIN_FAILED");
            return RemoteResponse.error("AUTH_FAILED", "Invalid credentials or role");
        }

        context.setAuthenticated(username, AisafeRoles.WEATHER_PERSON.toString(), SERVICE_ID);
        context.udpLogger().log(username, context.clientIp(), context.clientPort(), SERVICE_ID, "LOGIN_SUCCESS");
        return RemoteResponse.ok("SESSION", username);
    }

    private String handleLogout(final RemoteConnectionContext context) {
        if (!context.isAuthenticated() || !AisafeRoles.WEATHER_PERSON.toString().equals(context.role())) {
            return RemoteResponse.error("SESSION_REQUIRED", "Not authenticated");
        }
        final String username = context.username();
        AuthzRegistry.authorizationService().clearSession();
        context.clearAuthentication();
        context.udpLogger().log(username, context.clientIp(), context.clientPort(), SERVICE_ID, "LOGOUT");
        return RemoteResponse.ok("LOGOUT", username);
    }

    private String requireAuth(final RemoteConnectionContext context, final CommandAction action) {
        if (!context.isAuthenticated() || !AisafeRoles.WEATHER_PERSON.toString().equals(context.role())) {
            return RemoteResponse.error("SESSION_REQUIRED", "Login required");
        }
        try {
            return action.run();
        } catch (final IllegalArgumentException ex) {
            return RemoteResponse.error("VALIDATION_ERROR", ex.getMessage());
        } catch (final Exception ex) {
            return RemoteResponse.error("INTERNAL_ERROR", ex.getMessage());
        }
    }

    private String handleRegisterWeather(final String[] parts) {
        if (parts.length < 18) {
            return RemoteResponse.error("BAD_REQUEST", "REGISTER_WEATHER requires 17 parameters");
        }

        final LocalDateTime start = LocalDateTime.parse(parts[2].trim(), DATE_TIME);
        final LocalDateTime end = LocalDateTime.parse(parts[3].trim(), DATE_TIME);

        final WeatherData saved = registerController.registerWeatherData(
                parts[1].trim(),
                start,
                end,
                Double.parseDouble(parts[4]),
                Double.parseDouble(parts[5]),
                Double.parseDouble(parts[6]),
                Double.parseDouble(parts[7]),
                Double.parseDouble(parts[8]),
                Double.parseDouble(parts[9]),
                parts[10].trim(),
                Double.parseDouble(parts[11]),
                Double.parseDouble(parts[12]),
                WindSpeedUnit.valueOf(parts[13].trim().toUpperCase()),
                Double.parseDouble(parts[14]),
                TemperatureUnit.valueOf(parts[15].trim().toUpperCase()),
                Double.parseDouble(parts[16]),
                PressureUnit.valueOf(parts[17].trim().toUpperCase())
        );

        return RemoteResponse.ok("WEATHER_REGISTERED", String.valueOf(saved.identity()));
    }

    private String handleConsultWeather(final String[] parts) {
        if (parts.length < 3) {
            return RemoteResponse.error("BAD_REQUEST", "Usage: CONSULT_WEATHER;areaCode;yyyy-MM-dd");
        }
        final LocalDate date = LocalDate.parse(parts[2].trim(), DATE);
        final List<ConsultWeatherDataDTO> records = consultController.consultWeatherData(parts[1].trim(), date);
        if (records.isEmpty()) {
            return RemoteResponse.ok("CONSULT", "0");
        }
        final String payload = records.size() + "|" + records.stream()
                .map(this::formatWeatherRecord)
                .collect(Collectors.joining("|"));
        return RemoteResponse.ok("CONSULT", payload);
    }


    private String handleImportBulk(final String[] parts) {
        if (parts.length < 3) {
            return RemoteResponse.error("BAD_REQUEST", "Usage: IMPORT_WEATHER_BULK;filePath;format");
        }
        final List<WeatherDataImportResult> results = importController.importFromFile(parts[1].trim(), parts[2].trim());
        final long ok = results.stream().filter(WeatherDataImportResult::success).count();
        final long failed = results.size() - ok;
        return RemoteResponse.ok("IMPORT", "imported=" + ok + ",failed=" + failed);
    }

    private String formatWeatherRecord(final ConsultWeatherDataDTO data) {
        return data.areaCode + ","
                + data.observationStartDate.format(DATE_TIME) + ","
                + data.observationEndDate.format(DATE_TIME) + ","
                + data.windDirection + ","
                + data.windSpeed + ","
                + data.temperature + ","
                + data.pressure + ","
                + data.weatherSubArea;
    }

    private static ImportBulkWeatherDataController createImportController() {
        final ImportBulkWeatherDataController controller = new ImportBulkWeatherDataController();
        controller.registerImporter(new CsvWeatherDataImporter());
        return controller;
    }

    @FunctionalInterface
    private interface CommandAction {
        String run() throws Exception;
    }
}
