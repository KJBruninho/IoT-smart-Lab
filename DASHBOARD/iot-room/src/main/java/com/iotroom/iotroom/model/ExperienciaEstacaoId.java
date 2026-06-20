package com.iotroom.iotroom.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ExperienciaEstacaoId implements Serializable {

    @Column(name = "experiencia_id")
    private Long experienciaId;

    @Column(name = "estacao_id")
    private Long estacaoId;

    public ExperienciaEstacaoId() {
    }

    public ExperienciaEstacaoId(Long experienciaId, Long estacaoId) {
        this.experienciaId = experienciaId;
        this.estacaoId = estacaoId;
    }

    public Long getExperienciaId() {
        return experienciaId;
    }

    public Long getEstacaoId() {
        return estacaoId;
    }

    public void setExperienciaId(Long experienciaId) {
        this.experienciaId = experienciaId;
    }

    public void setEstacaoId(Long estacaoId) {
        this.estacaoId = estacaoId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExperienciaEstacaoId that)) return false;
        return Objects.equals(experienciaId, that.experienciaId)
                && Objects.equals(estacaoId, that.estacaoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(experienciaId, estacaoId);
    }
}