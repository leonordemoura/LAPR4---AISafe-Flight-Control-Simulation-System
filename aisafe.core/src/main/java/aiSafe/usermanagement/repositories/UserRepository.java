package aiSafe.usermanagement.repositories;

import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.PhoneNumber;
import aiSafe.usermanagement.domain.User;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

public interface UserRepository extends DomainRepository<Long, User> {
    boolean existsByEmail(Email email);

    boolean existsByPhoneNumber(PhoneNumber phoneNumber);

    Optional<User> findByEmail(Email email);
}
