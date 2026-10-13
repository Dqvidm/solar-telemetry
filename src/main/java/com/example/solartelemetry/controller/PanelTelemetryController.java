package com.example.solartelemetry.controller;

import com.example.solartelemetry.model.PanelTelemetry;
import com.example.solartelemetry.service.PanelTelemetryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/telemetry")
public class PanelTelemetryController {

    private final PanelTelemetryService service;

    @Autowired
    public PanelTelemetryController(PanelTelemetryService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<PanelTelemetry>> getAll() {
        return ResponseEntity.ok(service.getAllTelemetry());
    }

    // ingests telemetry from edge sensors, checks alert thresholds
    @PostMapping
    public ResponseEntity<PanelTelemetryService.IngestionResult> create(@RequestBody PanelTelemetry telemetry) {
        return new ResponseEntity<>(service.processTelemetry(telemetry), HttpStatus.CREATED);
    }

    @GetMapping("/panel/{panelId}")
    public ResponseEntity<List<PanelTelemetry>> getPanelHistory(@PathVariable String panelId) {
        return ResponseEntity.ok(service.getHistory(panelId));
    }

    // eval electrical health metrics, returns maintenance alerts
    @GetMapping("/panel/{panelId}/health")
    public ResponseEntity<PanelTelemetryService.PanelHealthStatus> getPanelHealth(@PathVariable String panelId) {
        return ResponseEntity.ok(service.evaluateHealth(panelId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteTelemetry(id);
        return ResponseEntity.noContent().build();
    }
}