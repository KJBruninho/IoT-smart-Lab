package com.iotroom.iotroom.dto;

public class GrupoMembroFormDTO {

    private Long utilizadorId;
    private Long roleGrupoId;

    public GrupoMembroFormDTO() {
    }

    public Long getUtilizadorId() { return utilizadorId; }
    public Long getRoleGrupoId() { return roleGrupoId; }

    public void setUtilizadorId(Long utilizadorId) { this.utilizadorId = utilizadorId; }
    public void setRoleGrupoId(Long roleGrupoId) { this.roleGrupoId = roleGrupoId; }
}