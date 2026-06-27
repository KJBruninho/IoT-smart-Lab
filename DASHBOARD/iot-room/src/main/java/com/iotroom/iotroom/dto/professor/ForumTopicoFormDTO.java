package com.iotroom.iotroom.dto.professor;

public class ForumTopicoFormDTO {

    private Long id;
    private String titulo;
    private String mensagem;
    private Long grupoId;
    private Long experienciaId;

    public ForumTopicoFormDTO() {
    }

    public ForumTopicoFormDTO(Long id, String titulo, String mensagem, Long grupoId, Long experienciaId) {
        this.id = id;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.grupoId = grupoId;
        this.experienciaId = experienciaId;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getMensagem() { return mensagem; }
    public Long getGrupoId() { return grupoId; }
    public Long getExperienciaId() { return experienciaId; }

    public void setId(Long id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public void setGrupoId(Long grupoId) { this.grupoId = grupoId; }
    public void setExperienciaId(Long experienciaId) { this.experienciaId = experienciaId; }
}