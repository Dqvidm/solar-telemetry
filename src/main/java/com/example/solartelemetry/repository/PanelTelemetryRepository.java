package com.example.solartelemetry.repository;

import com.example.solartelemetry.model.PanelTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PanelTelemetryRepository extends JpaRepository<PanelTelemetry, Long> {
}