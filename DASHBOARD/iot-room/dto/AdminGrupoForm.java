package com.iotroom.iotroom.dto;

public class AdminGrupoForm {

    private Long id;
    private String nome;
    private String descricao;
    private boolean ativo = true;

    public static AdminGrupoForm from(AdminGrupoDTO dto) {
        AdminGrupoForm form = new AdminGrupoForm();
        form.setId(dto.id());
        form.setNome(dto.nome());
        form.setDescricao(dto.descricao());
        form.setAtivo(dto.ativo());
        return form;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome != null ? nome.trim() : null;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao != null ? descricao.trim() : null;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}