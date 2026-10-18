package aiSafe.aircontrolarea.application;

import aiSafe.Application;
import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircontrolarea.domain.AreaName;
import aiSafe.aircontrolarea.domain.GeoBoundary;
import aiSafe.aircontrolarea.domain.MaxAltitude;
import aiSafe.aircontrolarea.domain.MinFuelAtArrival;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.usermanagement.domain.AisafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import org.hibernate.Transaction;
import org.springframework.transaction.reactive.TransactionContext;

import java.util.List;
import java.util.Objects;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

@UseCaseController
public class RegisterAirControlAreaController {
    private static final String MAX_ALTITUDE_CONFIG_KEY = "aircontrolarea.maxAltitudeMeters";

    private final AuthorizationService authz;
    private final RepositoryFactory repositories;
    private final DoubleSupplier configuredMaxAltitudeMeters;

    public RegisterAirControlAreaController() {
        this(
                AuthzRegistry.authorizationService(),
                PersistenceContext.repositories(),
                () -> RegisterAirControlAreaController.resolveMaxAltitudeFromSettings()
        );
    }

    RegisterAirControlAreaController(
            final AuthorizationService authz,
            final RepositoryFactory repositories,
            final DoubleSupplier configuredMaxAltitudeMeters
    ) {
        this.authz = Objects.requireNonNull(authz);
        this.repositories = Objects.requireNonNull(repositories);
        this.configuredMaxAltitudeMeters = Objects.requireNonNull(configuredMaxAltitudeMeters);
    }

    private RepositoryFactory repositoryFactory() {
        return repositories;
    }

    private void ensureAuthorization() {
        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.BACKOFFICE_OPERATOR);
    }

    public void checkAreaCodeExists(final String areaCodeValue) {
        ensureAuthorization();
        final AreaCode areaCode = new AreaCode(areaCodeValue);
        if (repositoryFactory().airControlAreas().existsByAreaCode(areaCode)) {
            throw new IllegalArgumentException("An air control area with this code already exists.");
        }
    }

    public void checkAreaNameExists(final String areaNameValue) {
        ensureAuthorization();
        final AreaName areaName = new AreaName(areaNameValue);
        if (repositoryFactory().airControlAreas().existsByAreaName(areaName)) {
            throw new IllegalArgumentException("An air control area with this name already exists.");
        }
    }

    private AirControlArea registerAirControlAreaInternal(
            final String areaCodeValue,
            final String areaNameValue,
            final double minFuelAtArrivalValue,
            final GeoBoundary geoBoundary,
            final TransactionalContext transactionContext
    ) {
        final AreaCode areaCode = new AreaCode(areaCodeValue);
        final AreaName areaName = new AreaName(areaNameValue);
        final MaxAltitude maxAltitude = new MaxAltitude(configuredMaxAltitudeMeters.getAsDouble());

        if (repositoryFactory().airControlAreas(transactionContext).existsByAreaCode(areaCode)) {
            throw new IllegalArgumentException("An air control area with this code already exists.");
        }
        if (repositoryFactory().airControlAreas(transactionContext).existsByAreaName(areaName)) {
            throw new IllegalArgumentException("An air control area with this name already exists.");
        }
        ensureBoundaryDoesNotOverlap(geoBoundary);

        final AirControlArea area = new AirControlArea(
            areaCode,
            areaName,
            new MinFuelAtArrival(minFuelAtArrivalValue),
            maxAltitude,
            geoBoundary
        );

        return repositoryFactory().airControlAreas(transactionContext).save(area);
    }

    public AirControlArea registerAirControlArea(
            final String areaCodeValue,
            final String areaNameValue,
            final double minFuelAtArrivalValue,
            final List<GeoBoundary> boundary
    ) {
        ensureAuthorization();

        final TransactionalContext transactionContext = repositoryFactory().newTransactionalContext();
        transactionContext.beginTransaction();
        try {

            if (boundary == null || boundary.size() != 1) {
                throw new IllegalArgumentException("Geo boundary must contain exactly one GeoBoundary.");
            }
            final GeoBoundary geoBoundary = boundary.get(0);
            final AirControlArea result = registerAirControlAreaInternal(areaCodeValue, areaNameValue, minFuelAtArrivalValue, geoBoundary, transactionContext);

            transactionContext.commit();

            return result;
        } catch (final RuntimeException ex) {
            transactionContext.rollback();
            throw ex;
        } finally {
            transactionContext.close();
        }
    }

    private void ensureBoundaryDoesNotOverlap(final GeoBoundary newBoundary) {
        for (final AirControlArea existingArea : repositoryFactory().airControlAreas().findAll()) {
            if (existingArea.geoBoundary().overlapsWith(newBoundary)) {
                throw new IllegalArgumentException(
                        "Geo boundary overlaps with existing area: " + existingArea.areaCode().value()
                );
            }
        }
    }

    static double resolveMaxAltitudeFromSettings() {
        final String rawValue = Application.settings().getProperty(MAX_ALTITUDE_CONFIG_KEY);
        return parseConfiguredMaxAltitudeMeters(rawValue);
    }

    static double parseConfiguredMaxAltitudeMeters(final String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalStateException("Missing configuration key: " + MAX_ALTITUDE_CONFIG_KEY);
        }
        try {
            return Double.parseDouble(rawValue.trim());
        } catch (final NumberFormatException ex) {
            throw new IllegalStateException(
                    "Invalid value for " + MAX_ALTITUDE_CONFIG_KEY + ": " + rawValue,
                    ex
            );
        }
    }
}
