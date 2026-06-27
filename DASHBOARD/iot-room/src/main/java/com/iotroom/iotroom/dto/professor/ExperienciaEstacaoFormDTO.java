package com.iotroom.iotroom.dto.professor;

public class ExperienciaEstacaoFormDTO {

    private Long estacaoId;
    private Integer ordem;
    private String observacao;

    public ExperienciaEstacaoFormDTO() {
    }

    public Long getEstacaoId() {
        return estacaoId;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setEstacaoId(Long estacaoId) {
        this.estacaoId = estacaoId;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}