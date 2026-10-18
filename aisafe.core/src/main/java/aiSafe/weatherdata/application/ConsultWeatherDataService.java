package aiSafe.weatherdata.application;

import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.weatherdata.domain.WeatherData;
import aiSafe.weatherdata.domain.WeatherSubArea;
import aiSafe.weatherdata.repositories.WeatherDataRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ConsultWeatherDataService {

    private final WeatherDataRepository weatherDataRepository;

    public ConsultWeatherDataService() {
        this.weatherDataRepository = PersistenceContext.repositories().weatherData();
    }

    ConsultWeatherDataService(final WeatherDataRepository weatherDataRepository) {
        this.weatherDataRepository = weatherDataRepository;
    }

    public List<ConsultWeatherDataDTO> consultWeatherData(final String areaCodeValue, final LocalDate date) {
        Objects.requireNonNull(areaCodeValue, "Area code must not be null");
        Objects.requireNonNull(date, "Date must not be null");

        final AreaCode areaCode = new AreaCode(areaCodeValue);
        return weatherDataRepository.findByAreaCodeAndDate(areaCode, date)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private ConsultWeatherDataDTO toDTO(final WeatherData w) {
        return new ConsultWeatherDataDTO(
                w.areaCode().value(),
                w.observationDate().startDate(),
                w.observationDate().endDate(),
                w.windDirection().value(),
                w.windSpeed().value(),
                w.temperature().value(),
                w.pressure().value(),
                formatSubArea(w.weatherSubArea())
        );
    }
    private String formatSubArea(final WeatherSubArea subArea) {
        return String.format(Locale.US, "Lat[%.4f,%.4f] Lon[%.4f,%.4f] Alt[%.1f,%.1f]",
                subArea.minLatitude(), subArea.maxLatitude(),
                subArea.minLongitude(), subArea.maxLongitude(),
                subArea.minAltitude(), subArea.maxAltitude());
    }
}