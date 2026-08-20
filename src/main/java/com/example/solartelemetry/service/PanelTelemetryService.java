package com.example.solartelemetry.service;

import com.example.solartelemetry.model.PanelTelemetry;
import com.example.solartelemetry.repository.PanelTelemetryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PanelTelemetryService {

    private final PanelTelemetryRepository repository;

    @Autowired
    public PanelTelemetryService(PanelTelemetryRepository repository) {
        this.repository = repository;
    }

    public List<PanelTelemetry> getAllTelemetry() {
        return repository.findAll();
    }

    public PanelTelemetry saveTelemetry(PanelTelemetry telemetry) {
        return repository.save(telemetry);
    }

    public void deleteTelemetry(Long id) {
        repository.deleteById(id);
    }
}