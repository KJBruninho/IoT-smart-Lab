package com.iotroom.iotroom.dto.professor;

import java.time.LocalDateTime;

public class GrupoMembroDTO {

    private Long utilizadorId;
    private String nome;
    private String email;
    private Long roleGrupoId;
    private String roleGrupo;
    private LocalDateTime criadoEm;

    public GrupoMembroDTO() {
    }

    public GrupoMembroDTO(GrupoMembroProjection projection) {
        this.utilizadorId = projection.getUtilizadorId();
        this.nome = projection.getNome();
        this.email = projection.getEmail();
        this.roleGrupoId = projection.getRoleGrupoId();
        this.roleGrupo = projection.getRoleGrupo();

        if (projection.getCriadoEm() != null) {
            this.criadoEm = projection.getCriadoEm().toLocalDateTime();
        }
    }

    public Long getUtilizadorId() { return utilizadorId; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public Long getRoleGrupoId() { return roleGrupoId; }
    public String getRoleGrupo() { return roleGrupo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }

    public void setUtilizadorId(Long utilizadorId) { this.utilizadorId = utilizadorId; }
    public void setNome(String nome) { this.nome = nome; }
    public void setEmail(String email) { this.email = email; }
    public void setRoleGrupoId(Long roleGrupoId) { this.roleGrupoId = roleGrupoId; }
    public void setRoleGrupo(String roleGrupo) { this.roleGrupo = roleGrupo; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}