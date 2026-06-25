package com.iotroom.iotroom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "permissoes_grupo_estacao")
public class PermissaoGrupoEstacao {

    @EmbeddedId
    private PermissaoGrupoEstacaoId id;

    @ManyToOne
    @MapsId("grupoId")
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @ManyToOne
    @MapsId("estacaoId")
    @JoinColumn(name = "estacao_id")
    private Estacao estacao;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

    public PermissaoGrupoEstacao() {}

    public PermissaoGrupoEstacao(Grupo grupo, Estacao estacao) {
        this.grupo = grupo;
        this.estacao = estacao;
        this.id = new PermissaoGrupoEstacaoId(grupo.getId(), estacao.getId());
        this.criadoEm = LocalDateTime.now();
    }

    public PermissaoGrupoEstacaoId getId() {
        return id;
    }

    public void setId(PermissaoGrupoEstacaoId id) {
        this.id = id;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public Estacao getEstacao() {
        return estacao;
    }

    public void setEstacao(Estacao estacao) {
        this.estacao = estacao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}