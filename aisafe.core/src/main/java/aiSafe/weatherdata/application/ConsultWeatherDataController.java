package aiSafe.weatherdata.application;

import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.weatherdata.domain.WeatherData;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.time.LocalDate;
import java.util.List;

@UseCaseController
public class ConsultWeatherDataController {

    private final AuthorizationService authz;
    private final ConsultWeatherDataService service;

    public ConsultWeatherDataController() {
        this.authz = AuthzRegistry.authorizationService();
        this.service = new ConsultWeatherDataService();
    }

    ConsultWeatherDataController(final AuthorizationService authz,
                                 final ConsultWeatherDataService service) {
        this.authz = authz;
        this.service = service;
    }

    public List<ConsultWeatherDataDTO> consultWeatherData(final String areaCodeValue, final LocalDate date) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AisafeRoles.WEATHER_PERSON,
                AisafeRoles.PILOT,
                AisafeRoles.FLIGHT_CONTROL_OPERATOR,
                AisafeRoles.ADMIN
        );
        return service.consultWeatherData(areaCodeValue, date);
    }
}