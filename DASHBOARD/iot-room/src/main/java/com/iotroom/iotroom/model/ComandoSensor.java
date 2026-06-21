package com.iotroom.iotroom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comandos_sensor")
public class ComandoSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "professor_id", nullable = false)
    private Long professorId;

    @Column(name = "sensor_id", nullable = false)
    private Long sensorId;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "tipo_sensor", nullable = false)
    private String tipoSensor;

    @Column(nullable = false)
    private String comando;

    @Column(nullable = false)
    private String estado = "ENVIADO";

    @Column(columnDefinition = "TEXT")
    private String resposta;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "confirmado_em")
    private LocalDateTime confirmadoEm;

    @PrePersist
    public void prePersist() {
        if (criadoEm == null) criadoEm = LocalDateTime.now();
        if (estado == null) estado = "ENVIADO";
    }

    public Long getId() { return id; }
    public Long getProfessorId() { return professorId; }
    public Long getSensorId() { return sensorId; }
    public String getDeviceId() { return deviceId; }
    public String getTipoSensor() { return tipoSensor; }
    public String getComando() { return comando; }
    public String getEstado() { return estado; }
    public String getResposta() { return resposta; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getConfirmadoEm() { return confirmadoEm; }

    public void setId(Long id) { this.id = id; }
    public void setProfessorId(Long professorId) { this.professorId = professorId; }
    public void setSensorId(Long sensorId) { this.sensorId = sensorId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public void setTipoSensor(String tipoSensor) { this.tipoSensor = tipoSensor; }
    public void setComando(String comando) { this.comando = comando; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setResposta(String resposta) { this.resposta = resposta; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public void setConfirmadoEm(LocalDateTime confirmadoEm) { this.confirmadoEm = confirmadoEm; }
}