package com.example.solartelemetry;

import com.example.solartelemetry.model.PanelTelemetry;
import com.example.solartelemetry.repository.PanelTelemetryRepository;
import com.example.solartelemetry.service.PanelTelemetryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SolarTelemetryApplicationTests {

    @Autowired
    private PanelTelemetryService service;

    @Autowired
    private PanelTelemetryRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Application context loads successfully")
    void contextLoads() {
        assertNotNull(service);
        assertNotNull(repository);
    }

    @Test
    @DisplayName("Process telemetry, compute power, and verify PostgreSQL persistence")
    void testTelemetryIngestionAndPersistence() {
        PanelTelemetry payload = new PanelTelemetry("PV-TEST-STRING-01", 40.0, 8.5, 32.0);

        PanelTelemetryService.IngestionResult result = service.processTelemetry(payload);

        assertNotNull(result);
        assertEquals(340.0, result.data().getPower());
        assertTrue(result.alerts().isEmpty());

        // verification retrieval directly from repo
        assertTrue(repository.findFirstByPanelIdOrderByRecordedAtDesc("PV-TEST-STRING-01").isPresent());
    }

    @Test
    @DisplayName("Evaluate health indicators and alerts for thermal fault")
    void testThermalFaultAlert() {
        PanelTelemetry hotPanel = new PanelTelemetry("PV-TEST-HOT-01", 38.0, 7.0, 75.0);
        service.processTelemetry(hotPanel);

        PanelTelemetryService.PanelHealthStatus status = service.evaluateHealth("PV-TEST-HOT-01");

        assertEquals("MAINTENANCE_REQUIRED", status.status());
        assertFalse(status.activeAlerts().isEmpty());
    }

    @Test
    @DisplayName("Time-series ordering: Latest record is returned first regardless of insertion order")
    void testTimeSeriesOrdering() throws InterruptedException {
        String panelId = "PV-SERIES-01";

        PanelTelemetry t1 = new PanelTelemetry(panelId, 40.0, 8.0, 30.0);
        service.processTelemetry(t1);

        Thread.sleep(10);

        PanelTelemetry t2 = new PanelTelemetry(panelId, 42.0, 8.5, 32.0);
        service.processTelemetry(t2);

        List<PanelTelemetry> history = service.getHistory(panelId);

        assertEquals(2, history.size());
        assertEquals(42.0, history.get(0).getVoltage(), "First item should be the latest recorded entry");
        assertEquals(40.0, history.get(1).getVoltage());
    }

    @Test
    @DisplayName("Boundary check: Exactly 70.0°C should NOT trigger critical thermal alert")
    void testThermalThresholdBoundary() {
        PanelTelemetry boundaryReading = new PanelTelemetry("PV-BOUND-01", 40.0, 8.0, 70.0);
        PanelTelemetryService.IngestionResult result = service.processTelemetry(boundaryReading);

        assertTrue(result.alerts().isEmpty(), "70.0°C should be considered within the safe operating envelope");
    }

    @Test
    @DisplayName("Electrical check: Low voltage with low current (nighttime) should NOT trigger undervoltage fault")
    void testLowVoltageAtNightDoesNotTriggerFault() {
        // nighttime conditions
        PanelTelemetry nightReading = new PanelTelemetry("PV-NIGHT-01", 5.0, 0.2, 18.0);
        PanelTelemetryService.IngestionResult result = service.processTelemetry(nightReading);

        assertTrue(result.alerts().isEmpty(), "Low voltage due to darkness should not register as a hardware failure");
    }

    @Test
    @DisplayName("Electrical check: Low voltage with significant current (>1.0A) MUST trigger bypass diode / degradation fault")
    void testBypassDiodeFaultCondition() {
        PanelTelemetry damagedString = new PanelTelemetry("PV-FAULT-01", 12.0, 5.0, 42.0);
        PanelTelemetryService.IngestionResult result = service.processTelemetry(damagedString);

        assertEquals(1, result.alerts().size());
        assertTrue(result.alerts().get(0).contains("FAULT_UNDERVOLTAGE"));
    }

    @Test
    @DisplayName("Persistence integrity: Reject entity if panelId violates NOT NULL constraint")
    void testNullPanelIdConstraint() {
        PanelTelemetry invalidEntity = new PanelTelemetry(null, 40.0, 8.0, 25.0);

        assertThrows(Exception.class, () -> {
            service.processTelemetry(invalidEntity);
        });
    }
}