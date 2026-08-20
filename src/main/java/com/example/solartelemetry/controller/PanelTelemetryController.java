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

    @PostMapping
    public ResponseEntity<PanelTelemetry> create(@RequestBody PanelTelemetry telemetry) {
        return new ResponseEntity<>(service.saveTelemetry(telemetry), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteTelemetry(id);
        return ResponseEntity.noContent().build();
    }
}