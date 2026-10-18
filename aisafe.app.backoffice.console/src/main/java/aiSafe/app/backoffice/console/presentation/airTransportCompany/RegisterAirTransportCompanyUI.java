package aiSafe.app.backoffice.console.presentation.airTransportCompany;

import aiSafe.aircraftmanagement.application.RegisterEngineModelController;
import aiSafe.aircraftmanagement.domain.*;
import aiSafe.airtransportcompany.application.RegisterAirTransportCompanyController;
import aiSafe.airtransportcompany.domain.CompanyName;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.domain.IcaoCode;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User interface class responsible for facilitating the registration of a new air transport company in the system. This class will interact with the user to gather necessary information about the air transport company and then delegate the registration process to the appropriate application service.
 * @author 2DE - Group 3
 * @version 1.0
 */

public class RegisterAirTransportCompanyUI extends AbstractUI {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterAirTransportCompanyUI.class);

    private final RegisterAirTransportCompanyController theController;

    public RegisterAirTransportCompanyUI(){
        theController = new RegisterAirTransportCompanyController();
    }

    @Override
    protected boolean doShow() {
        final String companyName = Console.readNonEmptyLine("Company name:", "Company name is mandatory.");
        final String iataCode = Console.readNonEmptyLine("IATA Code: ", "IATA Code is mandatory.");
        final String icaoCode = Console.readNonEmptyLine("ICAO Code: ", "ICAO Code is mandatory.");
        try {
            CompanyName companyName1 = CompanyName.valueOf(companyName);
            IataCode iataCode1 = IataCode.valueOf(iataCode);
            IcaoCode icaoCode1 = IcaoCode.valueOf(icaoCode);
            theController.registerAirTransportCompany(companyName1, iataCode1, icaoCode1);
            return true;
        } catch (final IllegalArgumentException ex) {
            System.out.println("Unable to register air transport company: " + ex.getMessage());
        } catch (final Exception ex) {
            System.out.println("Unexpected error while registering air transport company: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public String headline() {
        return "Register New Air Transport Company";
    }
}
