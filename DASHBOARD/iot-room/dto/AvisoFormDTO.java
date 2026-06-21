package com.iotroom.iotroom.dto;

public class AvisoFormDTO {

    private Long id;
    private String titulo;
    private String mensagem;
    private Boolean ativo = true;
    private String expiraEm;

    public AvisoFormDTO() {
    }

    public AvisoFormDTO(Long id, String titulo, String mensagem, Boolean ativo, String expiraEm) {
        this.id = id;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.ativo = ativo;
        this.expiraEm = expiraEm;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getMensagem() { return mensagem; }
    public Boolean getAtivo() { return ativo; }
    public String getExpiraEm() { return expiraEm; }

    public void setId(Long id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public void setExpiraEm(String expiraEm) { this.expiraEm = expiraEm; }
}