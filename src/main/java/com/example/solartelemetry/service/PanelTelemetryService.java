package com.example.solartelemetry.service;

import com.example.solartelemetry.model.PanelTelemetry;
import com.example.solartelemetry.repository.PanelTelemetryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PanelTelemetryService {

    public static final double MAX_OPERATING_TEMP_C = 70.0;
    public static final double MIN_OPERATING_VOLTAGE_V = 20.0;

    private final PanelTelemetryRepository repository;

    public PanelTelemetryService(PanelTelemetryRepository repository) {
        this.repository = repository;
    }

    public List<PanelTelemetry> getAllTelemetry() {
        return repository.findAll();
    }

    public PanelTelemetry saveTelemetry(PanelTelemetry telemetry) {
        if (telemetry.getVoltage() != null && telemetry.getCurrent() != null) {
            telemetry.setPower(Math.round((telemetry.getVoltage() * telemetry.getCurrent()) * 100.0) / 100.0);
        }
        return repository.save(telemetry);
    }

    public void deleteTelemetry(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public IngestionResult processTelemetry(PanelTelemetry telemetry) {
        if (telemetry.getVoltage() != null && telemetry.getCurrent() != null) {
            telemetry.setPower(Math.round((telemetry.getVoltage() * telemetry.getCurrent()) * 100.0) / 100.0);
        }

        PanelTelemetry saved = repository.save(telemetry);
        List<String> alerts = evaluateAlerts(saved);

        return new IngestionResult(saved, alerts);
    }

    public List<PanelTelemetry> getHistory(String panelId) {
        return repository.findByPanelIdOrderByRecordedAtDesc(panelId);
    }

    public PanelHealthStatus evaluateHealth(String panelId) {
        return repository.findFirstByPanelIdOrderByRecordedAtDesc(panelId)
                .map(latest -> {
                    List<String> activeAlerts = evaluateAlerts(latest);
                    String state = activeAlerts.isEmpty() ? "HEALTHY" : "MAINTENANCE_REQUIRED";
                    return new PanelHealthStatus(panelId, state, latest.getPower(), latest.getTemperature(), activeAlerts);
                })
                .orElseGet(() -> new PanelHealthStatus(panelId, "NO_DATA", 0.0, 0.0, List.of()));
    }

    private List<String> evaluateAlerts(PanelTelemetry data) {
        List<String> alerts = new ArrayList<>();
        if (data.getTemperature() != null && data.getTemperature() > MAX_OPERATING_TEMP_C) {
            alerts.add("CRITICAL_THERMAL_ALERT: Panel temperature (" + data.getTemperature() + "°C) exceeds 70°C threshold.");
        }
        if (data.getVoltage() != null && data.getVoltage() < MIN_OPERATING_VOLTAGE_V && data.getCurrent() != null && data.getCurrent() > 1.0) {
            alerts.add("FAULT_UNDERVOLTAGE: Detected potential cell degradation or bypass diode conduction (" + data.getVoltage() + "V).");
        }
        return alerts;
    }

    public record IngestionResult(PanelTelemetry data, List<String> alerts) {}
    public record PanelHealthStatus(String panelId, String status, Double lastPowerW, Double temperatureC, List<String> activeAlerts) {}
}