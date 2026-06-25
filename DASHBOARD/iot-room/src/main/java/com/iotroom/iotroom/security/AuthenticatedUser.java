package com.iotroom.iotroom.security;

public class AuthenticatedUser {

    private final Long id;
    private final String email;
    private final String nome;
    private final String role;

    public AuthenticatedUser(Long id, String email, String nome, String role) {
        this.id = id;
        this.email = email;
        this.nome = nome;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNome() {
        return nome;
    }

    public String getRole() {
        return role;
    }
}