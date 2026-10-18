package aiSafe.persistence.impl.jpa;

import aiSafe.Application;
import aiSafe.aircraft.domain.Aircraft;
import aiSafe.aircraft.repositories.AircraftRepository;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JpaAircraftRepository
        extends JpaAutoTxRepository<Aircraft, Long, Long>
        implements AircraftRepository {

    public JpaAircraftRepository(final TransactionalContext autoTx) {
        super(autoTx, "aircraftId");
    }

    public JpaAircraftRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "aircraftId");
    }

    // ── existing methods ─────────────────────────────────────────────────────

    @Override
    public boolean existsByRegistration(final String registration) {
        final Map<String, Object> params = new HashMap<>();
        params.put("registration", registration);
        return matchOne(
                "lower(e.registration.value) = lower(:registration)",
                params
        ).isPresent();
    }

    @Override
    public Optional<Aircraft> findByRegistration(final String registration) {
        final Map<String, Object> params = new HashMap<>();
        params.put("registration", registration);
        return matchOne(
                "lower(e.registration.value) = lower(:registration)",
                params
        );
    }

    @Override
    public List<Aircraft> findByCompany(final AirTransportCompany company) {
        final Map<String, Object> params = new HashMap<>();
        params.put("company", company);
        return match("e.company = :company", params);
    }

    @Override
    public List<Aircraft> findByCompanyAndModel(final AirTransportCompany company,
                                                final String modelName) {
        final Map<String, Object> params = new HashMap<>();
        params.put("company", company);
        params.put("modelName", modelName);
        return match(
                "e.company = :company " +
                        "AND lower(e.aircraftModel.name.aircraftModelName) = lower(:modelName)",
                params
        );
    }

    @Override
    public List<Aircraft> findByCompanyAndMaker(final AirTransportCompany company,
                                                final String makerName) {
        final Map<String, Object> params = new HashMap<>();
        params.put("company", company);
        params.put("makerName", makerName);
        return match(
                "e.company = :company " +
                        "AND lower(e.aircraftModel.maker.name.name) = lower(:makerName)",
                params
        );
    }

    @Override
    public List<Aircraft> findByCompanyAndMinCapacity(final AirTransportCompany company,
                                                      final int minCapacity) {
        final Map<String, Object> params = new HashMap<>();
        params.put("company", company);
        params.put("minCapacity", minCapacity);
        return match(
                "e.company = :company " +
                        "AND (e.cabinConfiguration.firstClassSeats " +
                        "   + e.cabinConfiguration.businessSeats " +
                        "   + e.cabinConfiguration.economySeats) >= :minCapacity",
                params
        );
    }

    @Override
    public List<Aircraft> findByCompanyAndMaxCapacity(final AirTransportCompany company,
                                                      final int maxCapacity) {
        final Map<String, Object> params = new HashMap<>();
        params.put("company", company);
        params.put("maxCapacity", maxCapacity);
        return match(
                "e.company = :company " +
                        "AND (e.cabinConfiguration.firstClassSeats " +
                        "   + e.cabinConfiguration.businessSeats " +
                        "   + e.cabinConfiguration.economySeats) <= :maxCapacity",
                params
        );
    }

    @Override
    public List<Aircraft> findByCompanyAndMaxAge(final AirTransportCompany company,
                                                 final int maxAge) {
        final int minYear = LocalDate.now().getYear() - maxAge;
        final Map<String, Object> params = new HashMap<>();
        params.put("company", company);
        params.put("minYear", minYear);
        return match(
                "e.company = :company " +
                        "AND e.registrationYear >= :minYear",
                params
        );
    }
}