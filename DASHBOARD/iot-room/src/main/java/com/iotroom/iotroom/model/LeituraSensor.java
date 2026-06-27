package com.iotroom.iotroom.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leituras_sensor")
public class LeituraSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "experiencia_id", nullable = false)
    private Long experienciaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(name = "unidade", nullable = false, length = 20)
    private String unidade;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_registo")
    private LocalDateTime registadoEm;

    public LeituraSensor() {
    }

    @PrePersist
    public void prePersist() {
        if (registadoEm == null) {
            registadoEm = LocalDateTime.now();
        }

        if (sensor != null && (unidade == null || unidade.isBlank())) {
            unidade = sensor.getUnidade();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getExperienciaId() {
        return experienciaId;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public String getUnidade() {
        return unidade;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDateTime getRegistadoEm() {
        return registadoEm;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setExperienciaId(Long experienciaId) {
        this.experienciaId = experienciaId;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public void setRegistadoEm(LocalDateTime registadoEm) {
        this.registadoEm = registadoEm;
    }
}