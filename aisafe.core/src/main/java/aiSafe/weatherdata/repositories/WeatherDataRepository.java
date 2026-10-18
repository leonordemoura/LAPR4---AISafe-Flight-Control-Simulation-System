package aiSafe.weatherdata.repositories;

import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.weatherdata.domain.ObservationDate;
import aiSafe.weatherdata.domain.WeatherData;
import aiSafe.weatherdata.domain.WeatherSubArea;
import aiSafe.weatherdata.domain.WeatherSource;
import eapli.framework.domain.repositories.DomainRepository;

import java.time.LocalDate;
import java.util.List;

public interface WeatherDataRepository extends DomainRepository<Long, WeatherData> {
    boolean existsByAreaCodeAndObservationDateAndWeatherSource(AreaCode areaCode, ObservationDate observationDate,
                                                               WeatherSource weatherSource, WeatherSubArea weatherSubArea);

    boolean existsByAreaCodeAndObservationDateAndWeatherSourceWithOverlappingSubArea(
            AreaCode areaCode, ObservationDate observationDate, WeatherSource weatherSource, WeatherSubArea weatherSubArea);

    List<WeatherData> findByAreaCodeAndDate(AreaCode areaCode, LocalDate date);
}

