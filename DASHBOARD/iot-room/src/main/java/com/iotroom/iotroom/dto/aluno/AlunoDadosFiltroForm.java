package com.iotroom.iotroom.dto.aluno;

import java.time.LocalDateTime;

public class AlunoDadosFiltroForm {
    private Long grupoId;
    private Long experienciaId;
    private Long estacaoId;
    private String tipoSensor;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private Integer limite = 50;

    public Long getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(Long grupoId) {
        this.grupoId = grupoId;
    }

    public Long getExperienciaId() {
        return experienciaId;
    }

    public void setExperienciaId(Long experienciaId) {
        this.experienciaId = experienciaId;
    }

    public Long getEstacaoId() {
        return estacaoId;
    }

    public void setEstacaoId(Long estacaoId) {
        this.estacaoId = estacaoId;
    }

    public String getTipoSensor() {
        return tipoSensor;
    }

    public void setTipoSensor(String tipoSensor) {
        this.tipoSensor = tipoSensor;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public Integer getLimite() {
        return limite;
    }

    public void setLimite(Integer limite) {
        this.limite = limite;
    }
}
