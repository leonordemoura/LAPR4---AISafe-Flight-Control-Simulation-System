package aiSafe.reportGeneration.domain;

import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.flight.domain.*;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import aiSafe.simulationreport.domain.*;
import aiSafe.simulationreport.repositories.SimulationReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonthlyStatsCollectorTest {

    @Mock private SimulationReportRepository simulationReportRepository;
    @Mock private FlightRepository flightRepository;

    private MonthlyStatsCollector collector;
    private YearMonth period;

    @BeforeEach
    void setUp() {
        collector = new MonthlyStatsCollector(simulationReportRepository, flightRepository);
        period = YearMonth.now().plusMonths(1);
    }

    @Test
    void collect_buildsAllRequiredSections() {
        // Arrange
        final Flight regularFlight = flightWithPlan("TP", 100, FlightType.REGULAR, FlightPlanStatus.VALIDATED);
        final Flight charterFlight = flightWithPlan("FR", 200, FlightType.CHARTER, FlightPlanStatus.DRAFT);

        when(flightRepository.findByPeriodAndArea(eq(period), any(AreaCode.class)))
                .thenReturn(List.of(regularFlight, charterFlight));

        final SimulationReport simReport = new SimulationReport(
                new ReportDate(LocalDateTime.now()),
                new TotalFlights(1),
                new TotalViolations(2),
                new ViolationThreshold(100),
                ValidationStatus.FAILED,
                List.of(), List.of(), List.of(),
                String.valueOf(regularFlight.designator().flightNumber()),
                null);

        when(simulationReportRepository.findByPeriodAndDesignators(eq(period), any()))
                .thenReturn(List.of(simReport));

        // Act
        final List<ReportSection> sections = collector.collect(period, new AreaCode("LIS"));

        // Assert
        assertEquals(5, sections.size());
        assertEquals("Executive Summary", sections.get(0).title());
        assertEquals("Flight Operations Statistics", sections.get(1).title());
        assertEquals("Safety Violations Summary", sections.get(2).title());
        assertEquals("Validation Rate", sections.get(3).title());
        assertEquals("Statistical Charts", sections.get(4).title());

        final String summary = sections.get(0).content();
        assertTrue(summary.contains("Total flights    : 2"));
        assertTrue(summary.contains("Total simulations: 1"));
        assertTrue(summary.contains("Safety violations: 2"));

        final String operations = sections.get(1).content();
        assertTrue(operations.contains("Regular flights: 1"));
        assertTrue(operations.contains("Charter flights: 1"));
        assertTrue(operations.contains("Top Routes (by volume):"));
        assertTrue(operations.contains("OPO-LIS"));
        assertTrue(operations.contains(regularFlight.designator().toString()));

        final String violations = sections.get(2).content();
        assertTrue(violations.contains("Total safety violations: 2"));
        assertTrue(violations.contains("FAILED : 1 simulation(s)"));
        assertTrue(violations.contains(String.valueOf(regularFlight.designator().flightNumber())));

        final String validation = sections.get(3).content();
        assertTrue(validation.contains("Validated flight plans : 1 / 2"));
        assertTrue(validation.contains("Validation rate        : 50.0%"));
        assertTrue(validation.contains("REQUIRES ATTENTION"));

        assertTrue(sections.get(4).content().contains("Flight type distribution"));
        assertTrue(sections.get(4).content().contains("Simulation validation outcomes"));
    }

    @Test
    void collect_marksValidationRateAsSatisfactoryWhenAboveThreshold() {
        // Arrange
        final Flight validatedA = flightWithPlan("TP", 101, FlightType.REGULAR, FlightPlanStatus.VALIDATED);
        final Flight validatedB = flightWithPlan("TP", 102, FlightType.REGULAR, FlightPlanStatus.VALIDATED);

        when(flightRepository.findByPeriodAndArea(eq(period), any(AreaCode.class)))
                .thenReturn(List.of(validatedA, validatedB));
        when(simulationReportRepository.findByPeriodAndDesignators(eq(period), any()))
                .thenReturn(List.of());

        // Act
        final List<ReportSection> sections = collector.collect(period, new AreaCode("LIS"));

        // Assert
        assertTrue(sections.get(3).content().contains("SATISFACTORY"));
        assertTrue(sections.get(3).content().contains("Validation rate        : 100.0%"));
    }

    @Test
    void collect_handlesEmptyPeriodGracefully() {
        when(flightRepository.findByPeriodAndArea(eq(period), any(AreaCode.class)))
                .thenReturn(List.of());

        final List<ReportSection> sections = collector.collect(period, new AreaCode("LIS"));

        assertEquals(5, sections.size());
        assertTrue(sections.get(0).content().contains("Total flights    : 0"));
        assertTrue(sections.get(1).content().contains("No flight operations were recorded"));
        assertTrue(sections.get(2).content().contains("No safety violations were recorded"));
        verify(simulationReportRepository, never()).findByPeriodAndDesignators(any(), any());
    }

    @Test
    void collect_listsFlightWithoutPlanAsNone() {
        final Flight bareFlight = new Flight(
                FlightRoute.valueOf(RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS")),
                FlightDesignator.of("TP", 999, null),
                FlightType.REGULAR,
                AircraftRegistration.valueOf("CS-TKY"));

        when(flightRepository.findByPeriodAndArea(eq(period), any(AreaCode.class)))
                .thenReturn(List.of(bareFlight));

        final List<ReportSection> sections = collector.collect(period, new AreaCode("LIS"));

        assertTrue(sections.get(1).content().contains("no plan"));
    }

    @Test
    void collect_rejectsNullRepositories() {
        assertThrows(IllegalArgumentException.class,
                () -> new MonthlyStatsCollector(null, flightRepository));
        assertThrows(IllegalArgumentException.class,
                () -> new MonthlyStatsCollector(simulationReportRepository, null));
    }

    private Flight flightWithPlan(final String airline, final int number,
                                  final FlightType type, final FlightPlanStatus status) {
        final Flight flight = new Flight(
                FlightRoute.valueOf(RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS")),
                FlightDesignator.of(airline, number, null),
                type,
                AircraftRegistration.valueOf("CS-TKY"));

        final DepartureDateTime departure = new DepartureDateTime(period.atDay(10).atTime(10, 0));
        final FlightPlan plan = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TKY"),
                new PilotID("P-001"),
                departure,
                new ArrivalDateTime(period.atDay(10).atTime(11, 0), departure.value()),
                new FuelQuantity(1000, "KG"),
                new DslSource("flight TP100 { type REGULAR leg { departure OPO 2026-12-01 08:00 arrival LIS 2026-12-01 09:00 } }"));

        setStatus(plan, status);
        return flight;
    }

    private static void setStatus(final FlightPlan plan, final FlightPlanStatus status) {
        try {
            final Field field = FlightPlan.class.getDeclaredField("status");
            field.setAccessible(true);
            field.set(plan, status);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
