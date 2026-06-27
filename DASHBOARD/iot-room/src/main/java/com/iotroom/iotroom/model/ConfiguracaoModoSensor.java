package com.iotroom.iotroom.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracoes_modo_sensor")
public class ConfiguracaoModoSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sensor_id", nullable = false, unique = true)
    private Long sensorId;

    @Column(name = "intervalo_rapido_ms", nullable = false)
    private Integer intervaloRapidoMs = 1000;

    @Column(name = "intervalo_estavel_ms", nullable = false)
    private Integer intervaloEstavelMs = 30000;

    @Column(name = "duracao_modo_rapido_ms", nullable = false)
    private Integer duracaoModoRapidoMs = 120000;

    @Column(name = "delta_significativo", nullable = false)
    private BigDecimal deltaSignificativo = BigDecimal.ONE;

    @Column(name = "atualizado_por")
    private Long atualizadoPor;

    @Column(name = "criada_em")
    private LocalDateTime criadaEm;

    @Column(name = "atualizada_em")
    private LocalDateTime atualizadaEm;

    @PrePersist
    public void prePersist() {
        if (criadaEm == null) criadaEm = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        atualizadaEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getSensorId() { return sensorId; }
    public Integer getIntervaloRapidoMs() { return intervaloRapidoMs; }
    public Integer getIntervaloEstavelMs() { return intervaloEstavelMs; }
    public Integer getDuracaoModoRapidoMs() { return duracaoModoRapidoMs; }
    public BigDecimal getDeltaSignificativo() { return deltaSignificativo; }
    public Long getAtualizadoPor() { return atualizadoPor; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public LocalDateTime getAtualizadaEm() { return atualizadaEm; }

    public void setId(Long id) { this.id = id; }
    public void setSensorId(Long sensorId) { this.sensorId = sensorId; }
    public void setIntervaloRapidoMs(Integer intervaloRapidoMs) { this.intervaloRapidoMs = intervaloRapidoMs; }
    public void setIntervaloEstavelMs(Integer intervaloEstavelMs) { this.intervaloEstavelMs = intervaloEstavelMs; }
    public void setDuracaoModoRapidoMs(Integer duracaoModoRapidoMs) { this.duracaoModoRapidoMs = duracaoModoRapidoMs; }
    public void setDeltaSignificativo(BigDecimal deltaSignificativo) { this.deltaSignificativo = deltaSignificativo; }
    public void setAtualizadoPor(Long atualizadoPor) { this.atualizadoPor = atualizadoPor; }
    public void setCriadaEm(LocalDateTime criadaEm) { this.criadaEm = criadaEm; }
    public void setAtualizadaEm(LocalDateTime atualizadaEm) { this.atualizadaEm = atualizadaEm; }
}