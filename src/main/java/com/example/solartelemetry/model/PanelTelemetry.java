package com.example.solartelemetry.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "panel_telemetry",
        indexes = {
                @Index(name = "idx_panel_timestamp", columnList = "panel_id, recorded_at DESC")
        }
)
public class PanelTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "panel_id", nullable = false)
    private String panelId;

    @Column(nullable = false)
    private Double voltage;

    @Column(nullable = false)
    private Double current;

    @Column(nullable = false)
    private Double power; // Computed as voltage * current

    @Column(nullable = false)
    private Double temperature;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    public PanelTelemetry() {
    }

    public PanelTelemetry(String panelId, Double voltage, Double current, Double temperature) {
        this.panelId = panelId;
        this.voltage = voltage;
        this.current = current;
        this.power = Math.round((voltage * current) * 100.0) / 100.0;
        this.temperature = temperature;
    }

    @PrePersist
    public void prePersist() {
        if (this.recordedAt == null) {
            this.recordedAt = Instant.now();
        }
        if (this.voltage != null && this.current != null) {
            this.power = Math.round((this.voltage * this.current) * 100.0) / 100.0;
        }
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPanelId() { return panelId; }
    public void setPanelId(String panelId) { this.panelId = panelId; }

    public Double getVoltage() { return voltage; }
    public void setVoltage(Double voltage) { this.voltage = voltage; }

    public Double getCurrent() { return current; }
    public void setCurrent(Double current) { this.current = current; }

    public Double getPower() { return power; }
    public void setPower(Double power) { this.power = power; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
}