package com.iotroom.iotroom.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@SuppressWarnings("serial")
@Embeddable
public class UtilizadorGrupoId implements Serializable {

    @Column(name = "utilizador_id")
    private Long utilizadorId;

    @Column(name = "grupo_id")
    private Long grupoId;

    public UtilizadorGrupoId() {
    }

    public UtilizadorGrupoId(Long utilizadorId, Long grupoId) {
        this.utilizadorId = utilizadorId;
        this.grupoId = grupoId;
    }

    public Long getUtilizadorId() { return utilizadorId; }
    public Long getGrupoId() { return grupoId; }

    public void setUtilizadorId(Long utilizadorId) { this.utilizadorId = utilizadorId; }
    public void setGrupoId(Long grupoId) { this.grupoId = grupoId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UtilizadorGrupoId that)) return false;
        return Objects.equals(utilizadorId, that.utilizadorId)
                && Objects.equals(grupoId, that.grupoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(utilizadorId, grupoId);
    }
}