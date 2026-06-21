package com.iotroom.iotroom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "utilizador_grupos")
public class UtilizadorGrupo {

    @EmbeddedId
    private UtilizadorGrupoId id;

    @Column(name = "role_grupo_id", nullable = false)
    private Long roleGrupoId;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    public UtilizadorGrupo() {
    }

    @PrePersist
    public void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public UtilizadorGrupoId getId() { return id; }
    public Long getRoleGrupoId() { return roleGrupoId; }
    public LocalDateTime getCriadoEm() { return criadoEm; }

    public Long getUtilizadorId() {
        return id != null ? id.getUtilizadorId() : null;
    }

    public Long getGrupoId() {
        return id != null ? id.getGrupoId() : null;
    }

    public void setId(UtilizadorGrupoId id) { this.id = id; }
    public void setRoleGrupoId(Long roleGrupoId) { this.roleGrupoId = roleGrupoId; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}