package aiSafe.infrastructure.bootstrap;

import aiSafe.aircraft.domain.*;
import aiSafe.aircraft.repositories.AircraftRepository;
import aiSafe.aircraftModel.domain.AircraftModel;
import aiSafe.aircraftModel.domain.AircraftModelName;
import aiSafe.aircraftModel.repositories.AircraftModelRepository;
import aiSafe.aircraftmanagement.domain.Country;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.CompanyName;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;

import java.util.List;

public class AircraftBootstrapper {

    public void execute() {

        registerIfMissing(
                "CS-TUA",
                "A320",
                List.of("CFM56-7B", "CFM56-7B"),
                10,
                20,
                120,
                2018,
                "Portugal",
                351,
                AircraftStatus.ACTIVE,
                "TAP Air Portugal"
        );
    }

    private void registerIfMissing(
            final String registration,
            final String aircraftModelName,
            final List<String> engineModelIds,
            final int firstClassSeats,
            final int businessSeats,
            final int economySeats,
            final int registrationYearValue,
            final String countryName,
            final Integer countryCode,
            final AircraftStatus status,
            final String companyName
    ) {

        final var factory = PersistenceContext.repositories();

        final AircraftRepository aircraftRepo = factory.aircrafts();
        final AircraftModelRepository modelRepo = factory.aircraftModels();
        final AirTransportCompanyRepository companyRepo =
                factory.getAirTransportCompanyRepository();

        if (aircraftRepo.existsByRegistration(registration)) {
            System.out.println("Aircraft already exists: " + registration);
            return;
        }

        final AircraftModel model =
                modelRepo.findByName(new AircraftModelName(aircraftModelName))
                        .orElseThrow(() -> new IllegalStateException(
                                "Aircraft model not found: " + aircraftModelName));

        final AirTransportCompany company =
                companyRepo.findByCompanyName(CompanyName.valueOf(companyName))
                        .orElseThrow(() -> new IllegalStateException(
                                "Air transport company not found: " + companyName));

        final AircraftRegistration regVO =
                AircraftRegistration.valueOf(registration);

        final RegistrationYear registrationYear =
                new RegistrationYear(registrationYearValue);

        final List<CurrentEngine> engines = engineModelIds.stream()
                .map(CurrentEngine::valueOf)
                .toList();

        final CabinConfiguration cabin =
                new CabinConfiguration(firstClassSeats, businessSeats, economySeats);

        final Country country = Country.valueOf(countryName, countryCode);

        final Aircraft aircraft = new Aircraft(
                regVO,
                model,
                engines,
                cabin,
                registrationYear,
                country,
                status,
                company
        );

        try {

            aircraftRepo.save(aircraft);

            System.out.println("Aircraft created: " + registration);

        } catch (ConcurrencyException | IntegrityViolationException ex) {

            System.out.println("Aircraft already exists: " + registration);
        }
    }
}