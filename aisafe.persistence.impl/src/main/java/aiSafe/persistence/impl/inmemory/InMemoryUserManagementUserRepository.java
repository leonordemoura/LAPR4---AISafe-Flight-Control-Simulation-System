package aiSafe.persistence.impl.inmemory;

import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.PhoneNumber;
import aiSafe.usermanagement.domain.User;
import aiSafe.usermanagement.repositories.UserRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

class InMemoryUserManagementUserRepository extends InMemoryDomainRepository<User, Long> implements UserRepository {
    private static final AtomicLong SEQUENCE = new AtomicLong(1L);

    @Override
    public <S extends User> S save(final S entity) {
        if (entity.identity() == null) {
            assignIdentity(entity);
        }
        return super.save(entity);
    }

    private void assignIdentity(final User entity) {
        try {
            final Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, SEQUENCE.getAndIncrement());
        } catch (final NoSuchFieldException | IllegalAccessException ex) {
            throw new IllegalStateException("Unable to assign in-memory identity to SystemUser", ex);
        }
    }

    @Override
    public boolean existsByEmail(final Email email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByPhoneNumber(final PhoneNumber phoneNumber) {
        return matchOne(u -> u.phoneNumber().equals(phoneNumber)).isPresent();
    }

    @Override
    public Optional<User> findByEmail(final Email email) {
        return matchOne(u -> u.email().equals(email));
    }
}

