package aiSafe.simulationreport.application;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulationReportApplicationDtoTest {

    @Test
    void simulationRunResultDTO_exposesFields() {
        // Arrange
        final FlightHistorySnapshotDTO history =
                new FlightHistorySnapshotDTO(1, List.of(new HistoryPointDTO(540, 0, 0, 1000)));
        final FlightExecutionStatusDTO status =
                new FlightExecutionStatusDTO(1, 0, 100.0, "ok");
        final SafetyViolationEventDTO event = new SafetyViolationEventDTO(
                1, 2, 540, 541,
                1, 2, 1000, 3, 4, 1100,
                1, 0, 0, 0, 1, 0,
                0.5, 100
        );

        // Act
        final SimulationRunResultDTO dto = new SimulationRunResultDTO(
                2, 1, 50, true, false, null, "TP123",
                List.of(history), List.of(status), List.of(event)
        );

        // Assert
        assertEquals(2, dto.totalFlights);
        assertEquals(1, dto.flightHistories.size());
        assertEquals(1, dto.flightStatuses.size());
        assertEquals(1, dto.violationEvents.size());
        assertEquals("TP123", dto.relatedFlightDesignator);
    }

    @Test
    void flightHistorySnapshotDTO_usesEmptyListWhenPointsNull() {
        // Arrange & Act
        final FlightHistorySnapshotDTO dto = new FlightHistorySnapshotDTO(1, null);

        // Assert
        assertTrue(dto.points.isEmpty());
    }

    @Test
    void simulationRunResultDTO_usesEmptyListsWhenNull() {
        // Arrange & Act
        final SimulationRunResultDTO dto = new SimulationRunResultDTO(
                0, 0, 0, false, true, "msg", null,
                null, null, null
        );

        // Assert
        assertTrue(dto.flightHistories.isEmpty());
        assertTrue(dto.flightStatuses.isEmpty());
        assertTrue(dto.violationEvents.isEmpty());
    }
}
