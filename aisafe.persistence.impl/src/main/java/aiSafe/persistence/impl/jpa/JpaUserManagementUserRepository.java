package aiSafe.persistence.impl.jpa;

import aiSafe.Application;
import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.PhoneNumber;
import aiSafe.usermanagement.domain.User;
import aiSafe.usermanagement.repositories.UserRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class JpaUserManagementUserRepository extends JpaAutoTxRepository<User, Long, Long> implements UserRepository {

    public JpaUserManagementUserRepository(final TransactionalContext autoTx) {
        super(autoTx, "id");
    }

    public JpaUserManagementUserRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "id");
    }

    @Override
    public boolean existsByEmail(final Email email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByPhoneNumber(final PhoneNumber phoneNumber) {
        final Map<String, Object> params = new HashMap<>();
        params.put("code", phoneNumber.code());
        params.put("number", phoneNumber.number());
        return matchOne("e.phoneNumber.code = :code and e.phoneNumber.number = :number", params).isPresent();
    }

    @Override
    public Optional<User> findByEmail(final Email email) {
        final Map<String, Object> params = new HashMap<>();
        params.put("email", email.value());

        return matchOne(
                "e.email.emailAddress = :email",
                params
        );
    }
}

