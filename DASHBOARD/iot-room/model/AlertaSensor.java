package com.iotroom.iotroom.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alertas_sensor")
public class AlertaSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "regra_id", nullable = false)
    private Long regraId;

    @Column(name = "leitura_id")
    private Long leituraId;

    @Column(name = "professor_id", nullable = false)
    private Long professorId;

    @Column(name = "experiencia_id")
    private Long experienciaId;

    @Column(name = "grupo_id")
    private Long grupoId;

    @Column(name = "estacao_id")
    private Long estacaoId;

    @Column(name = "sensor_id")
    private Long sensorId;

    @Column(name = "tipo_sensor", nullable = false)
    private String tipoSensor;

    @Column(name = "valor_lido", nullable = false)
    private BigDecimal valorLido;

    @Column(name = "valor_min")
    private BigDecimal valorMin;

    @Column(name = "valor_max")
    private BigDecimal valorMax;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String mensagem;

    @Column(nullable = false)
    private String severidade = "AVISO";

    @Column(nullable = false)
    private String estado = "NOVO";

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "lido_em")
    private LocalDateTime lidoEm;

    @Column(name = "resolvido_em")
    private LocalDateTime resolvidoEm;

    public AlertaSensor() {}

    @PrePersist
    public void prePersist() {
        if (criadoEm == null) criadoEm = LocalDateTime.now();
        if (estado == null) estado = "NOVO";
        if (severidade == null) severidade = "AVISO";
    }

    public Long getId() { return id; }
    public Long getRegraId() { return regraId; }
    public Long getLeituraId() { return leituraId; }
    public Long getProfessorId() { return professorId; }
    public Long getExperienciaId() { return experienciaId; }
    public Long getGrupoId() { return grupoId; }
    public Long getEstacaoId() { return estacaoId; }
    public Long getSensorId() { return sensorId; }
    public String getTipoSensor() { return tipoSensor; }
    public BigDecimal getValorLido() { return valorLido; }
    public BigDecimal getValorMin() { return valorMin; }
    public BigDecimal getValorMax() { return valorMax; }
    public String getTitulo() { return titulo; }
    public String getMensagem() { return mensagem; }
    public String getSeveridade() { return severidade; }
    public String getEstado() { return estado; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getLidoEm() { return lidoEm; }
    public LocalDateTime getResolvidoEm() { return resolvidoEm; }

    public void setId(Long id) { this.id = id; }
    public void setRegraId(Long regraId) { this.regraId = regraId; }
    public void setLeituraId(Long leituraId) { this.leituraId = leituraId; }
    public void setProfessorId(Long professorId) { this.professorId = professorId; }
    public void setExperienciaId(Long experienciaId) { this.experienciaId = experienciaId; }
    public void setGrupoId(Long grupoId) { this.grupoId = grupoId; }
    public void setEstacaoId(Long estacaoId) { this.estacaoId = estacaoId; }
    public void setSensorId(Long sensorId) { this.sensorId = sensorId; }
    public void setTipoSensor(String tipoSensor) { this.tipoSensor = tipoSensor; }
    public void setValorLido(BigDecimal valorLido) { this.valorLido = valorLido; }
    public void setValorMin(BigDecimal valorMin) { this.valorMin = valorMin; }
    public void setValorMax(BigDecimal valorMax) { this.valorMax = valorMax; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public void setSeveridade(String severidade) { this.severidade = severidade; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public void setLidoEm(LocalDateTime lidoEm) { this.lidoEm = lidoEm; }
    public void setResolvidoEm(LocalDateTime resolvidoEm) { this.resolvidoEm = resolvidoEm; }
}