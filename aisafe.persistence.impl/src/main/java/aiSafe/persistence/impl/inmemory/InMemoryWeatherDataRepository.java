package aiSafe.persistence.impl.inmemory;

import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.weatherdata.domain.ObservationDate;
import aiSafe.weatherdata.domain.WeatherData;
import aiSafe.weatherdata.domain.WeatherSubArea;
import aiSafe.weatherdata.domain.WeatherSource;
import aiSafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryWeatherDataRepository extends InMemoryDomainRepository<WeatherData, Long>
        implements WeatherDataRepository {
    private static final AtomicLong SEQUENCE = new AtomicLong(1L);

    @Override
    public <S extends WeatherData> S save(final S entity) {
        if (entity.identity() == null) {
            assignIdentity(entity);
        }
        return super.save(entity);
    }

    private void assignIdentity(final WeatherData entity) {
        try {
            final Field idField = WeatherData.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, SEQUENCE.getAndIncrement());
        } catch (final NoSuchFieldException | IllegalAccessException ex) {
            throw new IllegalStateException("Unable to assign in-memory identity to WeatherData", ex);
        }
    }

    @Override
    public boolean existsByAreaCodeAndObservationDateAndWeatherSource(final AreaCode areaCode,
                                                                       final ObservationDate observationDate,
                                                                       final WeatherSource weatherSource,
                                                                       final WeatherSubArea weatherSubArea) {
        return matchOne(weatherData ->
                weatherData.areaCode().equals(areaCode)
                        && weatherData.observationDate().equals(observationDate)
                        && weatherData.weatherSource().equals(weatherSource)
                        && weatherData.weatherSubArea().equals(weatherSubArea)
        ).isPresent();
    }

    @Override
    public boolean existsByAreaCodeAndObservationDateAndWeatherSourceWithOverlappingSubArea(
            final AreaCode areaCode, final ObservationDate observationDate,
            final WeatherSource weatherSource, final WeatherSubArea weatherSubArea) {
        return matchOne(weatherData ->
                weatherData.areaCode().equals(areaCode)
                        && weatherData.observationDate().equals(observationDate)
                        && weatherData.weatherSource().equals(weatherSource)
                        && weatherData.weatherSubArea().overlapsVolumeWith(weatherSubArea)
        ).isPresent();
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
