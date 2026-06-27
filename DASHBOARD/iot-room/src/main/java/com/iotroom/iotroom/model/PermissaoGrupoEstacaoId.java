package com.iotroom.iotroom.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@SuppressWarnings("serial")
@Embeddable
public class PermissaoGrupoEstacaoId implements Serializable {

    @Column(name = "grupo_id")
    private Long grupoId;

    @Column(name = "estacao_id")
    private Long estacaoId;

    public PermissaoGrupoEstacaoId() {}

    public PermissaoGrupoEstacaoId(Long grupoId, Long estacaoId) {
        this.grupoId = grupoId;
        this.estacaoId = estacaoId;
    }

    public Long getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(Long grupoId) {
        this.grupoId = grupoId;
    }

    public Long getEstacaoId() {
        return estacaoId;
    }

    public void setEstacaoId(Long estacaoId) {
        this.estacaoId = estacaoId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PermissaoGrupoEstacaoId that)) return false;
        return Objects.equals(grupoId, that.grupoId)
                && Objects.equals(estacaoId, that.estacaoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(grupoId, estacaoId);
    }
}