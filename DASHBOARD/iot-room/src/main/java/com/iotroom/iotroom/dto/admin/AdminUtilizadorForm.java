package com.iotroom.iotroom.dto.admin;

public class AdminUtilizadorForm {

    private Long id;
    private String nome;
    private String email;
    private String role = "ALUNO";
    private boolean ativo = true;

    public static AdminUtilizadorForm from(AdminUtilizadorDTO dto) {
        AdminUtilizadorForm form = new AdminUtilizadorForm();
        form.setId(dto.id());
        form.setNome(dto.nome());
        form.setEmail(dto.email());
        form.setRole(dto.role());
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role != null ? role.trim().toUpperCase() : "ALUNO";
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}