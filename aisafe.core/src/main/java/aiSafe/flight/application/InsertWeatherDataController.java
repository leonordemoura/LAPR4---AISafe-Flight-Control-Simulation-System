package aiSafe.flight.application;

import aiSafe.flight.domain.Flight;
import aiSafe.flight.domain.FlightDesignator;
import aiSafe.flight.domain.FlightPlan;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.weatherdata.domain.WeatherData;
import aiSafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.application.UseCaseController;

import java.util.List;
import java.util.stream.StreamSupport;

/**
 * US082 — Insert weather data in a flight plan.
 */
@UseCaseController
public class InsertWeatherDataController {

    private final FlightRepository flightRepository;
    private final WeatherDataRepository weatherDataRepository;
    private final InsertWeatherDataService insertWeatherDataService;

    public InsertWeatherDataController() {
        this.flightRepository = PersistenceContext.repositories().flights();
        this.weatherDataRepository = PersistenceContext.repositories().weatherData();
        this.insertWeatherDataService = new InsertWeatherDataService(flightRepository, weatherDataRepository);
    }

    /**
     * Returns all flights that have a pending flight plan (DRAFT or VALIDATED).
     */
    public List<Flight> pendingFlights() {
        return StreamSupport.stream(flightRepository.findAll().spliterator(), false)
                .filter(f -> f.flightPlan() != null && f.flightPlan().isPending())
                .toList();
    }

    /**
     * Returns all WeatherData available in the system.
     * The pilot browses and picks the relevant one(s).
     */
    public List<WeatherData> allAvailableWeatherData() {
        return StreamSupport.stream(weatherDataRepository.findAll().spliterator(), false)
                .toList();
    }

    /**
     * Inserts the chosen WeatherData into the pending flight plan.
     * If the plan was VALIDATED, it reverts to DRAFT (test deemed void).
     */
    public FlightPlan insertWeatherData(final FlightDesignator designator, final Long weatherDataId) {
        return insertWeatherDataService.insertWeatherData(designator, weatherDataId);
    }
}