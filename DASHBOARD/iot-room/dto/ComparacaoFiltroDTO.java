package com.iotroom.iotroom.dto;

public class ComparacaoFiltroDTO {

    private Long grupoId;
    private Long experienciaId;
    private Long estacaoId;
    private String tipoSensor;
    private Integer limite;

    public ComparacaoFiltroDTO() {
    }

    public ComparacaoFiltroDTO(
            Long grupoId,
            Long experienciaId,
            Long estacaoId,
            String tipoSensor,
            Integer limite
    ) {
        this.grupoId = grupoId;
        this.experienciaId = experienciaId;
        this.estacaoId = estacaoId;
        this.tipoSensor = tipoSensor;
        this.limite = limite;
    }

    public Long getGrupoId() { return grupoId; }
    public Long getExperienciaId() { return experienciaId; }
    public Long getEstacaoId() { return estacaoId; }
    public String getTipoSensor() { return tipoSensor; }
    public Integer getLimite() { return limite; }

    public void setGrupoId(Long grupoId) { this.grupoId = grupoId; }
    public void setExperienciaId(Long experienciaId) { this.experienciaId = experienciaId; }
    public void setEstacaoId(Long estacaoId) { this.estacaoId = estacaoId; }
    public void setTipoSensor(String tipoSensor) { this.tipoSensor = tipoSensor; }
    public void setLimite(Integer limite) { this.limite = limite; }
}