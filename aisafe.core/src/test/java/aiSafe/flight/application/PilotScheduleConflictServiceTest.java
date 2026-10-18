package aiSafe.flight.application;

import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PilotScheduleConflictServiceTest {

    @Mock private RepositoryFactory repositories;
    @Mock private FlightRepository flightRepository;

    private PilotScheduleConflictService service;

    @BeforeEach
    void setUp() {
        when(repositories.flights()).thenReturn(flightRepository);
        service = new PilotScheduleConflictService(repositories);
    }

    @Test
    void throws_when_repository_reports_overlap() {
        when(flightRepository.hasOverlappingScheduleForPilot(
                any(), any(), any(), any(), any())).thenReturn(true);

        final FlightPlanSchedule schedule = new FlightPlanSchedule(
                LocalDateTime.of(2026, 6, 10, 10, 0),
                LocalDateTime.of(2026, 6, 10, 11, 0));

        assertThrows(IllegalArgumentException.class,
                () -> service.ensureNoOverlap(new PilotID("P-001"), schedule, null, null));
    }

    @Test
    void passes_when_no_overlap() {
        when(flightRepository.hasOverlappingScheduleForPilot(
                any(), any(), any(), any(), any())).thenReturn(false);

        final FlightPlanSchedule schedule = new FlightPlanSchedule(
                LocalDateTime.of(2026, 6, 10, 10, 0),
                LocalDateTime.of(2026, 6, 10, 11, 0));

        assertDoesNotThrow(
                () -> service.ensureNoOverlap(new PilotID("P-001"), schedule, null, null));
    }
}
