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

    @ManyToOne
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    private BigDecimal valor;

    @Column(name = "data_registo")
    private LocalDateTime registadoEm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Sensor getSensor() { return sensor; }
    public void setSensor(Sensor sensor) { this.sensor = sensor; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDateTime getRegistadoEm() { return registadoEm; }
    public void setRegistadoEm(LocalDateTime registadoEm) { this.registadoEm = registadoEm; }
}
