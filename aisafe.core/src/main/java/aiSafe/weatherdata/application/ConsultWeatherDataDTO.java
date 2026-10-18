package aiSafe.weatherdata.application;

import java.time.LocalDateTime;

public class ConsultWeatherDataDTO {

    public final String areaCode;
    public final LocalDateTime observationStartDate;
    public final LocalDateTime observationEndDate;
    public final double windDirection;   // <-- double, não String
    public final double windSpeed;
    public final double temperature;
    public final double pressure;
    public final String weatherSubArea;

    public ConsultWeatherDataDTO(
            final String areaCode,
            final LocalDateTime observationStartDate,
            final LocalDateTime observationEndDate,
            final double windDirection,
            final double windSpeed,
            final double temperature,
            final double pressure,
            final String weatherSubArea
    ) {
        this.areaCode = areaCode;
        this.observationStartDate = observationStartDate;
        this.observationEndDate = observationEndDate;
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
        this.temperature = temperature;
        this.pressure = pressure;
        this.weatherSubArea = weatherSubArea;
    }
}