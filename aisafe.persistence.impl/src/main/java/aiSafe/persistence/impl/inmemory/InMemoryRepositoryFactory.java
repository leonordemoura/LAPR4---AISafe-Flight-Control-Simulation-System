package aiSafe.persistence.impl.inmemory;

import aiSafe.PilotCertification.repositories.PilotCertificationRepository;
import aiSafe.aircontrolarea.repositories.AirControlAreaRepository;
import aiSafe.aircraft.repositories.AircraftRepository;
import aiSafe.aircraftModel.repositories.AircraftModelRepository;
import aiSafe.aircraftmanagement.repositories.EngineModelRepository;
import aiSafe.aircraftmanagement.repositories.FuelTypeRepository;
import aiSafe.aircraftmanagement.repositories.MakerRepository;
import aiSafe.airport.repositories.AirportRepository;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.flightRoute.repositories.FlightRouteRepository;
import aiSafe.reportGeneration.repositories.ReportRepository;
import aiSafe.simulationreport.repositories.SimulationReportRepository;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.repositories.UserRepository;
import aiSafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.repositories.impl.inmemory.InMemoryUserRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryTransactionalContext;

public class InMemoryRepositoryFactory implements RepositoryFactory {

    private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
    private final InMemoryUserManagementUserRepository userManagementUserRepository = new InMemoryUserManagementUserRepository();
    private final InMemoryAirControlAreaRepository airControlAreaRepository = new InMemoryAirControlAreaRepository();
    private final InMemoryEngineModelRepository engineModelRepository = new InMemoryEngineModelRepository();
    private final InMemoryMakerRepository makerRepository = new InMemoryMakerRepository();
    private final InMemoryFuelTypeRepository fuelTypeRepository = new InMemoryFuelTypeRepository();
    private final InMemoryAirTransportCompanyRepository airTransportCompanyRepository = new InMemoryAirTransportCompanyRepository();
    private final InMemoryWeatherDataRepository weatherDataRepository = new InMemoryWeatherDataRepository();
    private final InMemoryAirportRepository airportRepository = new InMemoryAirportRepository();
    private final InMemoryAircraftModelRepository aircraftModelRepository = new InMemoryAircraftModelRepository();
    private final InMemoryAircraftRepository aircraftRepository = new InMemoryAircraftRepository();
    private final InMemoryFlightRepository flightsRepository = new InMemoryFlightRepository(airportRepository);
    private final InMemoryFlightRouteRepository flightRouteRepository = new InMemoryFlightRouteRepository();
    private final InMemoryPilotCertificationRepository pilotCertificationRepository = new InMemoryPilotCertificationRepository();
    private final InMemorySimulationReportRepository simulationReportRepository = new InMemorySimulationReportRepository();
    private final InMemoryReportRepository reportRepository = new InMemoryReportRepository();

    public InMemoryRepositoryFactory() {
        InMemoryInitializer.init();
    }

    @Override
    public TransactionalContext newTransactionalContext() {
        return new InMemoryTransactionalContext();
    }

    @Override
    public eapli.framework.infrastructure.authz.domain.repositories.UserRepository users(final TransactionalContext autoTx) {
        return userRepository;
    }

    @Override
    public eapli.framework.infrastructure.authz.domain.repositories.UserRepository users() {
        return users(null);
    }

    @Override
    public UserRepository userManagementUsers(final TransactionalContext autoTx) {
        return userManagementUserRepository;
    }

    @Override
    public UserRepository userManagementUsers() {
        return userManagementUsers(null);
    }

    @Override
    public AirControlAreaRepository airControlAreas(final TransactionalContext autoTx) {
        return airControlAreaRepository;
    }

    @Override
    public AirControlAreaRepository airControlAreas() {
        return airControlAreas(null);
    }

    @Override
    public AirportRepository airports(final TransactionalContext autoTx) {
        return airportRepository;
    }

    @Override
    public AirportRepository airports() {
        return airports(null);
    }

    @Override
    public PilotCertificationRepository pilots(final TransactionalContext autoTx) {
        return pilotCertificationRepository;
    }

    @Override
    public PilotCertificationRepository pilots() {
        return pilots(null);
    }

    @Override
    public AircraftModelRepository aircraftModels(final TransactionalContext autoTx) {
        return aircraftModelRepository;
    }

    @Override
    public AircraftModelRepository aircraftModels() {
        return aircraftModels(null);
    }

    @Override
    public EngineModelRepository getEngineModelRepository(final TransactionalContext autoTx) {
        return engineModelRepository;
    }

    @Override
    public EngineModelRepository getEngineModelRepository() {
        return getEngineModelRepository(null);
    }

    @Override
    public MakerRepository getMakerRepository(final TransactionalContext autoTx) {
        return makerRepository;
    }

    @Override
    public MakerRepository getMakerRepository() {
        return getMakerRepository(null);
    }

    @Override
    public FuelTypeRepository getFuelTypeRepository(final TransactionalContext autoTx) {
        return fuelTypeRepository;
    }

    @Override
    public FuelTypeRepository getFuelTypeRepository() {
        return getFuelTypeRepository(null);
    }

    @Override
    public AirTransportCompanyRepository getAirTransportCompanyRepository(final TransactionalContext autoTx) {
        return airTransportCompanyRepository;
    }

    @Override
    public AirTransportCompanyRepository getAirTransportCompanyRepository() {
        return getAirTransportCompanyRepository(null);
    }

    @Override
    public WeatherDataRepository weatherData(final TransactionalContext autoTx) {
        return weatherDataRepository;
    }

    @Override
    public WeatherDataRepository weatherData() {
        return weatherData(null);
    }

    @Override
    public AircraftRepository aircrafts(final TransactionalContext autoTx) {
        return aircraftRepository;
    }

    @Override
    public AircraftRepository aircrafts() {
        return aircrafts(null);
    }

    @Override
    public FlightRepository flights(final TransactionalContext autoTx) {
        return flightsRepository;
    }

    @Override
    public FlightRepository flights() {
        return flights(null);
    }

    @Override
    public FlightRouteRepository flightRoutes(final TransactionalContext autoTx) {
        return flightRouteRepository;
    }

    @Override
    public FlightRouteRepository flightRoutes() {
        return flightRoutes(null);
    }

    @Override
    public SimulationReportRepository simulationReports(final TransactionalContext autoTx) {
        return simulationReportRepository;
    }

    @Override
    public SimulationReportRepository simulationReports() {
        return simulationReports(null);
    }

    @Override
    public ReportRepository reports(final TransactionalContext autoTx) {
        return reportRepository;
    }

    @Override
    public ReportRepository reports() {
        return reports(null);
    }
}