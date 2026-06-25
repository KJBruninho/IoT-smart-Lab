package com.iotroom.iotroom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forum_topicos")
public class ForumTopico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(name = "criado_por", nullable = false)
    private Long criadoPorId;

    @Column(name = "grupo_id")
    private Long grupoId;

    @Column(name = "experiencia_id")
    private Long experienciaId;

    @Column(nullable = false)
    private String estado = "ABERTO";

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @Column(name = "fechado_em")
    private LocalDateTime fechadoEm;

    public ForumTopico() {
    }

    @PrePersist
    public void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }

        if (estado == null) {
            estado = "ABERTO";
        }
    }

    @PreUpdate
    public void preUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getMensagem() { return mensagem; }
    public Long getCriadoPorId() { return criadoPorId; }
    public Long getGrupoId() { return grupoId; }
    public Long getExperienciaId() { return experienciaId; }
    public String getEstado() { return estado; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public LocalDateTime getFechadoEm() { return fechadoEm; }

    public void setId(Long id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public void setCriadoPorId(Long criadoPorId) { this.criadoPorId = criadoPorId; }
    public void setGrupoId(Long grupoId) { this.grupoId = grupoId; }
    public void setExperienciaId(Long experienciaId) { this.experienciaId = experienciaId; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public void setFechadoEm(LocalDateTime fechadoEm) { this.fechadoEm = fechadoEm; }
}