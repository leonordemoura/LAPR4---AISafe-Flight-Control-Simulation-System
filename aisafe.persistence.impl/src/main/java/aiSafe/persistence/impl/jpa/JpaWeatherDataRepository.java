package aiSafe.persistence.impl.jpa;

import aiSafe.Application;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.weatherdata.domain.ObservationDate;
import aiSafe.weatherdata.domain.WeatherData;
import aiSafe.weatherdata.domain.WeatherSubArea;
import aiSafe.weatherdata.domain.WeatherSource;
import aiSafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JpaWeatherDataRepository extends JpaAutoTxRepository<WeatherData, Long, Long>
        implements WeatherDataRepository {

    public JpaWeatherDataRepository(final TransactionalContext autoTx) {
        super(autoTx, "id");
    }

    public JpaWeatherDataRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "id");
    }

    @Override
    public boolean existsByAreaCodeAndObservationDateAndWeatherSource(final AreaCode areaCode,
                                                                       final ObservationDate observationDate,
                                                                       final WeatherSource weatherSource,
                                                                       final WeatherSubArea weatherSubArea) {
        for (final var weatherData : findAll()) {
            if (weatherData.areaCode().equals(areaCode)
                    && weatherData.observationDate().equals(observationDate)
                    && weatherData.weatherSource().equals(weatherSource)
                    && weatherData.weatherSubArea().equals(weatherSubArea)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean existsByAreaCodeAndObservationDateAndWeatherSourceWithOverlappingSubArea(
            final AreaCode areaCode, final ObservationDate observationDate,
            final WeatherSource weatherSource, final WeatherSubArea weatherSubArea) {
        for (final var weatherData : findAll()) {
            if (weatherData.areaCode().equals(areaCode)
                    && weatherData.observationDate().equals(observationDate)
                    && weatherData.weatherSource().equals(weatherSource)
                    && weatherData.weatherSubArea().overlapsVolumeWith(weatherSubArea)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<WeatherData> findByAreaCodeAndDate(final AreaCode areaCode, final LocalDate date) {
        final List<WeatherData> result = new ArrayList<>();
        for (final WeatherData wd : findAll()) {
            if (wd.areaCode().equals(areaCode)
                    && !wd.observationDate().startDate().toLocalDate().isAfter(date)
                    && !wd.observationDate().endDate().toLocalDate().isBefore(date)) {
                result.add(wd);
            }
        }
        return result;
    }
}
