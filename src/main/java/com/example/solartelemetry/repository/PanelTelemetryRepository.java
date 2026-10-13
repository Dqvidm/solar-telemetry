package com.example.solartelemetry.repository;

import com.example.solartelemetry.model.PanelTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PanelTelemetryRepository extends JpaRepository<PanelTelemetry, Long> {

    List<PanelTelemetry> findByPanelIdOrderByRecordedAtDesc(String panelId);

    Optional<PanelTelemetry> findFirstByPanelIdOrderByRecordedAtDesc(String panelId);

    List<PanelTelemetry> findByPanelIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            String panelId, Instant start, Instant end
    );
}