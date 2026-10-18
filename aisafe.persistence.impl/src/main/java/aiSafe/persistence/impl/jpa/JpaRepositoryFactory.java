package aiSafe.persistence.impl.jpa;

import aiSafe.Application;
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
import aiSafe.weatherdata.repositories.WeatherDataRepository;
import aiSafe.usermanagement.repositories.UserRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.repositories.impl.jpa.JpaAutoTxUserRepository;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

public class JpaRepositoryFactory implements RepositoryFactory {
    @Override
    public eapli.framework.infrastructure.authz.domain.repositories.UserRepository users(final TransactionalContext autoTx) {
        return new JpaAutoTxUserRepository(autoTx);
    }

    @Override
    public eapli.framework.infrastructure.authz.domain.repositories.UserRepository users() {
        return new JpaAutoTxUserRepository(
                Application.settings().getPersistenceUnitName(),
                Application.settings().getExtendedPersistenceProperties()
        );
    }

    @Override
    public UserRepository userManagementUsers(final TransactionalContext autoTx) {
        return new JpaUserManagementUserRepository(autoTx);
    }

    @Override
    public UserRepository userManagementUsers() {
        return new JpaUserManagementUserRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public AirControlAreaRepository airControlAreas(final TransactionalContext autoTx) {
        return new JpaAirControlAreaRepository(autoTx);
    }

    @Override
    public AirControlAreaRepository airControlAreas() {
        return new JpaAirControlAreaRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public AirportRepository airports(final TransactionalContext autoTx) {
        return new JpaAirportRepository(autoTx);
    }

    @Override
    public AirportRepository airports() {
        return new JpaAirportRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public PilotCertificationRepository pilots(final TransactionalContext autoTx) {
        return new JpaPilotCertificationRepository(autoTx);
    }

    @Override
    public PilotCertificationRepository pilots() {
        return new JpaPilotCertificationRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public AircraftModelRepository aircraftModels(final TransactionalContext autoTx) {
        return new JpaAircraftModelRepository(autoTx);
    }

    @Override
    public AircraftModelRepository aircraftModels() {
        return new JpaAircraftModelRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public EngineModelRepository getEngineModelRepository(final TransactionalContext autoTx) {
        return new JpaEngineModelRepository(autoTx);
    }

    @Override
    public EngineModelRepository getEngineModelRepository() {
        return new JpaEngineModelRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public MakerRepository getMakerRepository(final TransactionalContext autoTx) {
        return new JpaMakerRepository(autoTx);
    }

    @Override
    public FuelTypeRepository getFuelTypeRepository(final TransactionalContext autoTx) {
        return new JpaFuelTypeRepository(autoTx);
    }

    @Override
    public AirTransportCompanyRepository getAirTransportCompanyRepository(final TransactionalContext autoTx) {
        return new JpaAirTransportCompanyRepository(autoTx);
    }

    @Override
    public AirTransportCompanyRepository getAirTransportCompanyRepository() {
        return new JpaAirTransportCompanyRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public MakerRepository getMakerRepository() {
        return new JpaMakerRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public FuelTypeRepository getFuelTypeRepository() {
        return new JpaFuelTypeRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public WeatherDataRepository weatherData(final TransactionalContext autoTx) {
        return new JpaWeatherDataRepository(autoTx);
    }

    @Override
    public WeatherDataRepository weatherData() {
        return new JpaWeatherDataRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public AircraftRepository aircrafts(final TransactionalContext autoTx) {
        return new JpaAircraftRepository(autoTx);
    }

    @Override
    public AircraftRepository aircrafts() {
        return new JpaAircraftRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public FlightRepository flights(final TransactionalContext autoTx) {
        return new JpaFlightRepository(autoTx);
    }

    @Override
    public FlightRepository flights() {
        return new JpaFlightRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public FlightRouteRepository flightRoutes(final TransactionalContext autoTx) {
        return new JpaFlightRouteRepository(autoTx);
    }

    @Override
    public FlightRouteRepository flightRoutes() {
        return new JpaFlightRouteRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public SimulationReportRepository simulationReports(final TransactionalContext autoTx) {
        return new JpaSimulationReportRepository(autoTx);
    }

    @Override
    public SimulationReportRepository simulationReports() {
        return new JpaSimulationReportRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public ReportRepository reports(final TransactionalContext autoTx) {
        return new JpaReportRepository(autoTx);
    }

    @Override
    public ReportRepository reports() {
        return new JpaReportRepository(Application.settings().getPersistenceUnitName());
    }

    @Override
    public TransactionalContext newTransactionalContext() {
        return JpaAutoTxRepository.buildTransactionalContext(
                Application.settings().getPersistenceUnitName(),
                Application.settings().getExtendedPersistenceProperties()
        );
    }
}