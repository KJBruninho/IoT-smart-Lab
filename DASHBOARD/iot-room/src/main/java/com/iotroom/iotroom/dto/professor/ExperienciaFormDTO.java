package com.iotroom.iotroom.dto.professor;

public class ExperienciaFormDTO {

    private Long id;
    private String nome;
    private String descricao;
    private Long grupoId;

    public ExperienciaFormDTO() {
    }

    public ExperienciaFormDTO(Long id, String nome, String descricao, Long grupoId) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.grupoId = grupoId;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public Long getGrupoId() { return grupoId; }

    public void setId(Long id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setGrupoId(Long grupoId) { this.grupoId = grupoId; }
}