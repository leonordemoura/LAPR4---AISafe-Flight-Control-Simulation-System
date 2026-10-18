package aiSafe.weatherdata.application;

import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircontrolarea.repositories.AirControlAreaRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.weatherdata.domain.ObservationDate;
import aiSafe.weatherdata.domain.Pressure;
import aiSafe.weatherdata.domain.PressureUnit;
import aiSafe.weatherdata.domain.Temperature;
import aiSafe.weatherdata.domain.TemperatureUnit;
import aiSafe.weatherdata.domain.WeatherData;
import aiSafe.weatherdata.domain.WeatherSubArea;
import aiSafe.weatherdata.domain.WeatherSource;
import aiSafe.weatherdata.domain.WindDirection;
import aiSafe.weatherdata.domain.WindSpeed;
import aiSafe.weatherdata.domain.WindSpeedUnit;
import aiSafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Supplier;

@UseCaseController
public class RegisterWeatherDataController {
    private final AuthorizationService authz;
    private final RepositoryFactory repositories;

    public RegisterWeatherDataController() {
        this(AuthzRegistry.authorizationService(), PersistenceContext.repositories());
    }

    RegisterWeatherDataController(final AuthorizationService authz, final RepositoryFactory repositories) {
        this.authz = Objects.requireNonNull(authz);
        this.repositories = Objects.requireNonNull(repositories);
    }

    private RepositoryFactory repos() {
        return repositories;
    }

    private WeatherDataRepository weatherDataRepository(final TransactionalContext tx) {
        return repos().weatherData(tx);
    }

    private AirControlAreaRepository airControlAreaRepository(final TransactionalContext tx) {
        return repos().airControlAreas(tx);
    }

    private void ensureAuthorization() {
        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.WEATHER_PERSON, AisafeRoles.ADMIN);
    }

    /**
     * Loads the air control area aggregate for the given code (same entity used for bounds validation below).
     */
    public AirControlArea controlAreaByAreaCode(final String areaCodeValue) {
        ensureAuthorization();
        final TransactionalContext tx = repos().newTransactionalContext();
        tx.beginTransaction();
        try {
            final AreaCode areaCode = new AreaCode(areaCodeValue);
            final AirControlArea result = airControlAreaRepository(tx)
                    .findByAreaCode(areaCode)
                    .orElseThrow(() -> new IllegalArgumentException("Air control area does not exist."));

            tx.commit();
            return result;
        } catch (final RuntimeException ex) {
            tx.rollback();
            throw ex;
        }
        finally {
            tx.close();
        }
    }

    public void validateSubArea(final String areaCodeValue,
                                final double minLat, final double maxLat,
                                final double minLon, final double maxLon,
                                final double minAlt, final double maxAlt) {
        ensureAuthorization();
        final TransactionalContext tx = repos().newTransactionalContext();
        tx.beginTransaction();
        try {
            final AreaCode areaCode = new AreaCode(areaCodeValue);
            final var controlArea = airControlAreaRepository(tx).findByAreaCode(areaCode)
                    .orElseThrow(() -> new IllegalArgumentException("Air control area does not exist."));

            final WeatherSubArea weatherSubArea = new WeatherSubArea(minLat, maxLat, minLon, maxLon, minAlt, maxAlt);
            if (!weatherSubArea.isWithinControlArea(controlArea.geoBoundary(), controlArea.maxAltitude().value())) {
                throw new IllegalArgumentException("Sub-area must be fully contained within the control area boundary and altitude.");
            }
            tx.commit();
        } catch (final RuntimeException ex) {
            tx.rollback();
            throw ex;
        } finally {
            tx.close();
        }
    }

    public WeatherData registerWeatherData(final String areaCodeValue, final LocalDateTime observationStartDateValue,
                                           final LocalDateTime observationEndDateValue,
                                           final double minLat, final double maxLat,
                                           final double minLon, final double maxLon,
                                           final double minAlt, final double maxAlt,
                                           final String weatherSourceValue, final double windDirectionValue,
                                           final double windSpeedValue,
                                           final WindSpeedUnit windSpeedUnit,
                                           final double temperatureValue,
                                           final TemperatureUnit temperatureUnit,
                                           final double pressureValue,
                                           final PressureUnit pressureUnit) {
        ensureAuthorization();

        final TransactionalContext tx = repos().newTransactionalContext();
        tx.beginTransaction();

        try{
        final AreaCode areaCode = new AreaCode(areaCodeValue);
        final var controlArea = airControlAreaRepository(tx).findByAreaCode(areaCode)
                .orElseThrow(() -> new IllegalArgumentException("Air control area does not exist."));
        final WeatherSubArea weatherSubArea = new WeatherSubArea(minLat, maxLat, minLon, maxLon, minAlt, maxAlt);
        if (!weatherSubArea.isWithinControlArea(controlArea.geoBoundary(), controlArea.maxAltitude().value())) {
            throw new IllegalArgumentException("Sub-area must be fully contained within the control area boundary and altitude.");
        }

        final ObservationDate observationDate = new ObservationDate(observationStartDateValue, observationEndDateValue);
        final WeatherSource weatherSource = new WeatherSource(weatherSourceValue);

        if (weatherDataRepository(tx).existsByAreaCodeAndObservationDateAndWeatherSource(
                areaCode, observationDate, weatherSource, weatherSubArea)) {
            throw new IllegalArgumentException("Weather data already exists for the given area, date, source and sub-area.");
        }

        if (weatherDataRepository(tx).existsByAreaCodeAndObservationDateAndWeatherSourceWithOverlappingSubArea(
                areaCode, observationDate, weatherSource, weatherSubArea)) {
            throw new IllegalArgumentException("Sub-area/camada overlaps with an existing weather record.");
        }

        final WeatherData weatherData = new WeatherData(
                areaCode,
                observationDate,
                weatherSource,
                weatherSubArea,
                new WindDirection(windDirectionValue),
                new WindSpeed(windSpeedValue, windSpeedUnit),
                new Temperature(temperatureValue, temperatureUnit),
                new Pressure(pressureValue, pressureUnit)
        );

        final WeatherData result = weatherDataRepository(tx).save(weatherData);
        tx.commit();
        return result;
        } catch (final RuntimeException ex) {
            tx.rollback();
            throw ex;
        } finally {
            tx.close();
        }

    }
}
