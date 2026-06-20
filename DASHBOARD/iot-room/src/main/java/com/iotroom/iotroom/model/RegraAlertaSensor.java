package com.iotroom.iotroom.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "regras_alerta_sensor")
public class RegraAlertaSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "professor_id", nullable = false)
    private Long professorId;

    @Column(name = "grupo_id")
    private Long grupoId;

    @Column(name = "experiencia_id")
    private Long experienciaId;

    @Column(name = "estacao_id")
    private Long estacaoId;

    @Column(name = "tipo_sensor", nullable = false)
    private String tipoSensor;

    @Column(nullable = false)
    private String operador;

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
    private Boolean ativo = true;

    @Column(name = "cooldown_minutos", nullable = false)
    private Integer cooldownMinutos = 10;

    @Column(name = "criada_em")
    private LocalDateTime criadaEm;

    @Column(name = "atualizada_em")
    private LocalDateTime atualizadaEm;

    public RegraAlertaSensor() {}

    @PrePersist
    public void prePersist() {
        if (criadaEm == null) criadaEm = LocalDateTime.now();
        if (ativo == null) ativo = true;
        if (severidade == null) severidade = "AVISO";
        if (cooldownMinutos == null) cooldownMinutos = 10;
    }

    @PreUpdate
    public void preUpdate() {
        atualizadaEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getProfessorId() { return professorId; }
    public Long getGrupoId() { return grupoId; }
    public Long getExperienciaId() { return experienciaId; }
    public Long getEstacaoId() { return estacaoId; }
    public String getTipoSensor() { return tipoSensor; }
    public String getOperador() { return operador; }
    public BigDecimal getValorMin() { return valorMin; }
    public BigDecimal getValorMax() { return valorMax; }
    public String getTitulo() { return titulo; }
    public String getMensagem() { return mensagem; }
    public String getSeveridade() { return severidade; }
    public Boolean getAtivo() { return ativo; }
    public Integer getCooldownMinutos() { return cooldownMinutos; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public LocalDateTime getAtualizadaEm() { return atualizadaEm; }

    public void setId(Long id) { this.id = id; }
    public void setProfessorId(Long professorId) { this.professorId = professorId; }
    public void setGrupoId(Long grupoId) { this.grupoId = grupoId; }
    public void setExperienciaId(Long experienciaId) { this.experienciaId = experienciaId; }
    public void setEstacaoId(Long estacaoId) { this.estacaoId = estacaoId; }
    public void setTipoSensor(String tipoSensor) { this.tipoSensor = tipoSensor; }
    public void setOperador(String operador) { this.operador = operador; }
    public void setValorMin(BigDecimal valorMin) { this.valorMin = valorMin; }
    public void setValorMax(BigDecimal valorMax) { this.valorMax = valorMax; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public void setSeveridade(String severidade) { this.severidade = severidade; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public void setCooldownMinutos(Integer cooldownMinutos) { this.cooldownMinutos = cooldownMinutos; }
    public void setCriadaEm(LocalDateTime criadaEm) { this.criadaEm = criadaEm; }
    public void setAtualizadaEm(LocalDateTime atualizadaEm) { this.atualizadaEm = atualizadaEm; }
}