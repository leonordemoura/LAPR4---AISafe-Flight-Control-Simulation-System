package aiSafe.aircraft.application;

import aiSafe.aircraft.domain.Aircraft;
import aiSafe.aircraft.repositories.AircraftRepository;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;

import java.util.List;

public class ListFleetController {

    private final AircraftRepository aircraftRepo =
            PersistenceContext.repositories().aircrafts();

    private final AirTransportCompanyRepository companyRepo =
            PersistenceContext.repositories().getAirTransportCompanyRepository();

    public List<AirTransportCompany> availableCompanies() {
        return (List<AirTransportCompany>) companyRepo.findAll();
    }

    private AirTransportCompany resolveCompany(final String iataCode) {
        return companyRepo.findByIataCode(IataCode.valueOf(iataCode))
                .orElseThrow(() -> new IllegalStateException(
                        "Company not found for iata code: " + iataCode));
    }

    public List<Aircraft> getFleet(final String iataCode) {
        return aircraftRepo.findByCompany(resolveCompany(iataCode));
    }

    public List<Aircraft> getFleetByModel(final String iataCode, final String modelName) {
        return aircraftRepo.findByCompanyAndModel(resolveCompany(iataCode), modelName);
    }

    public List<Aircraft> getFleetByMaker(final String iataCode, final String makerName) {
        return aircraftRepo.findByCompanyAndMaker(resolveCompany(iataCode), makerName);
    }

    public List<Aircraft> getFleetByMinCapacity(final String iataCode, final int minCapacity) {
        return aircraftRepo.findByCompanyAndMinCapacity(resolveCompany(iataCode), minCapacity);
    }

    public List<Aircraft> getFleetByMaxCapacity(final String iataCode, final int maxCapacity) {
        return aircraftRepo.findByCompanyAndMaxCapacity(resolveCompany(iataCode), maxCapacity);
    }

    public List<Aircraft> getFleetByMaxAge(final String iataCode, final int maxAge) {
        return aircraftRepo.findByCompanyAndMaxAge(resolveCompany(iataCode), maxAge);
    }
}