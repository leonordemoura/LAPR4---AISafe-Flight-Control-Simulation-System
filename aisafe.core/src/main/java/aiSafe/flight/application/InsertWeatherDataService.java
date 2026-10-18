package aiSafe.flight.application;

import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.flight.domain.Flight;
import aiSafe.flight.domain.FlightDesignator;
import aiSafe.flight.domain.FlightPlan;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.weatherdata.domain.WeatherData;
import aiSafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.application.UseCaseController;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * US082 — Insert weather data in a flight plan.
 */
@Service
@UseCaseController
public class InsertWeatherDataService {

    private final FlightRepository flightRepository;
    private final WeatherDataRepository weatherDataRepository;

    public InsertWeatherDataService(
            final FlightRepository flightRepository,
            final WeatherDataRepository weatherDataRepository
    ) {
        this.flightRepository = flightRepository;
        this.weatherDataRepository = weatherDataRepository;
    }

    /**
     * Lists WeatherData records available for the flight's departure date and the given area.
     * Used to populate the selection list before the pilot picks one.
     *
     * @param designator the flight designator
     * @param areaCode   the air control area to search weather data for
     * @return list of matching WeatherData records
     */
    @Transactional(readOnly = true)
    public List<WeatherData> listAvailableWeatherData(
            final FlightDesignator designator,
            final AreaCode areaCode
    ) {
        final Flight flight = findFlight(designator);

        final FlightPlan plan = flight.flightPlan();
        if (plan == null || !plan.isPending()) {
            throw new IllegalStateException(
                    "No pending flight plan found for flight " + designator + ".");
        }

        final LocalDate departureDate = plan.departureDateTime().value().toLocalDate();

        return weatherDataRepository.findByAreaCodeAndDate(areaCode, departureDate);
    }

    /**
     * Inserts a chosen WeatherData record into the pending flight plan.
     * If the plan was VALIDATED, it reverts to DRAFT (test deemed void).
     *
     * @param designator    the flight designator
     * @param weatherDataId id of an existing WeatherData aggregate
     * @return the updated FlightPlan
     */
    @Transactional
    public FlightPlan insertWeatherData(
            final FlightDesignator designator,
            final Long weatherDataId
    ) {
        final Flight flight = findFlight(designator);

        final WeatherData weatherData = weatherDataRepository.ofIdentity(weatherDataId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "WeatherData not found with id: " + weatherDataId));

        flight.insertWeatherData(weatherData);

        flightRepository.save(flight);

        return flight.flightPlan();
    }

    private Flight findFlight(final FlightDesignator designator) {
        return flightRepository.findByDesignator(designator)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Flight not found: " + designator));
    }
}