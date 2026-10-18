package aiSafe.infrastructure.persistence;

import aiSafe.PilotCertification.domain.PilotCertification;
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
import aiSafe.weatherdata.repositories.WeatherDataRepository;
import aiSafe.usermanagement.repositories.UserRepository;
import eapli.framework.domain.repositories.TransactionalContext;

public interface RepositoryFactory {
    TransactionalContext newTransactionalContext();

    eapli.framework.infrastructure.authz.domain.repositories.UserRepository users(TransactionalContext autoTx);

    eapli.framework.infrastructure.authz.domain.repositories.UserRepository users();

    UserRepository userManagementUsers(TransactionalContext autoTx);

    UserRepository userManagementUsers();

    AirControlAreaRepository airControlAreas(TransactionalContext autoTx);

    AirControlAreaRepository airControlAreas();

    AirportRepository airports(TransactionalContext autoTx);

    AirportRepository airports();

    PilotCertificationRepository pilots(TransactionalContext autoTx);

    PilotCertificationRepository pilots();

    AircraftModelRepository aircraftModels(TransactionalContext autoTx);

    AircraftModelRepository aircraftModels();

    EngineModelRepository getEngineModelRepository(TransactionalContext autoTx);

    EngineModelRepository getEngineModelRepository();

    MakerRepository getMakerRepository(TransactionalContext autoTx);

    MakerRepository getMakerRepository();

    FuelTypeRepository getFuelTypeRepository(TransactionalContext autoTx);

    FuelTypeRepository getFuelTypeRepository();

    WeatherDataRepository weatherData(TransactionalContext autoTx);

    WeatherDataRepository weatherData();

    AirTransportCompanyRepository getAirTransportCompanyRepository(TransactionalContext autoTx);

    AirTransportCompanyRepository getAirTransportCompanyRepository();

    AircraftRepository aircrafts(TransactionalContext autoTx);

    AircraftRepository aircrafts();

    FlightRepository flights(TransactionalContext autoTx);

    FlightRepository flights();

    FlightRouteRepository flightRoutes(TransactionalContext autoTx);

    FlightRouteRepository flightRoutes();

    SimulationReportRepository simulationReports(TransactionalContext autoTx);

    SimulationReportRepository simulationReports();

    ReportRepository reports(TransactionalContext autoTx);

    ReportRepository reports();

}
