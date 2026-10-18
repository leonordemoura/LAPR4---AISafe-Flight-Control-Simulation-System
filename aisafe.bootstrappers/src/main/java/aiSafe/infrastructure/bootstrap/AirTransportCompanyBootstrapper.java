package aiSafe.infrastructure.bootstrap;

import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.CompanyName;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.domain.IcaoCode;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;

public class AirTransportCompanyBootstrapper {
    private final AirTransportCompanyRepository repository = PersistenceContext.repositories().getAirTransportCompanyRepository();

    public void execute(){
        registerIfMissing("TAP Air Portugal", "TP", "TAP");
        registerIfMissing("Lufthansa", "LH", "DLH");
    }

    private void registerIfMissing(
            String name,
            String iataCode,
            String icaoCode){


        CompanyName companyName = CompanyName.valueOf(name);
        if(repository.findByCompanyName(companyName).isPresent()){
            System.out.println("An air transport company with the name '" + name + "' already exists. Skipping.");
            return;
        }

        IataCode iata = IataCode.valueOf(iataCode);
        if(repository.findByIataCode(iata).isPresent()){
            System.out.println("An air transport company with the IATA code '" + iataCode + "' already exists. Skipping.");
            return;
        }

        IcaoCode icao = IcaoCode.valueOf(icaoCode);
        if(repository.findByIcaoCode(icao).isPresent()){
            System.out.println("An air transport company with the ICAO code '" + icaoCode + "' already exists. Skipping.");
            return;
        }

        AirTransportCompany company = new AirTransportCompany(companyName, iata, icao);

        try{
            repository.save(company);
            System.out.println("Air transport company created: " + name);
        } catch(Exception ex){
            System.out.println("Error creating air transport company '" + name + "': " + ex.getMessage());
        }
    }
}
