package com.iotroom.iotroom.dto;

import java.math.BigDecimal;

public class RegraAlertaSensorFormDTO {

    private Long id;
    private Long grupoId;
    private Long experienciaId;
    private Long estacaoId;
    private String tipoSensor;
    private String operador;
    private BigDecimal valorMin;
    private BigDecimal valorMax;
    private String titulo;
    private String mensagem;
    private String severidade;
    private Boolean ativo = true;
    private Integer cooldownMinutos = 10;

    public RegraAlertaSensorFormDTO() {}

    public Long getId() { return id; }
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

    public void setId(Long id) { this.id = id; }
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
}