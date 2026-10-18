package aiSafe.usermanagement.domain;

import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserBuilderHelperTest {

    // =========================================================
    // TEST 1 - builder is created successfully
    // =========================================================

    @Test
    void ensureBuilderIsCreatedSuccessfully() {

        final var builder = UserBuilderHelper.builder();

        assertNotNull(builder);
    }

    // =========================================================
    // TEST 2 - builder is correct type
    // =========================================================

    @Test
    void ensureBuilderIsSystemUserBuilder() {

        final var builder = UserBuilderHelper.builder();

        assertTrue(builder instanceof SystemUserBuilder);
    }

    // =========================================================
    // TEST 3 - multiple calls return valid instances
    // =========================================================

    @Test
    void ensureMultipleCallsReturnIndependentInstances() {

        final var b1 = UserBuilderHelper.builder();
        final var b2 = UserBuilderHelper.builder();

        assertNotNull(b1);
        assertNotNull(b2);
        assertNotSame(b1, b2);
    }

    // =========================================================
    // TEST 4 - builder does not throw exceptions
    // =========================================================

    @Test
    void ensureBuilderDoesNotThrowException() {

        assertDoesNotThrow(UserBuilderHelper::builder);
    }
}