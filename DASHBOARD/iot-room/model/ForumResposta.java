package com.iotroom.iotroom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forum_respostas")
public class ForumResposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topico_id", nullable = false)
    private Long topicoId;

    @Column(name = "autor_id", nullable = false)
    private Long autorId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    public ForumResposta() {
    }

    @PrePersist
    public void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }

        if (ativo == null) {
            ativo = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getTopicoId() { return topicoId; }
    public Long getAutorId() { return autorId; }
    public String getMensagem() { return mensagem; }
    public Boolean getAtivo() { return ativo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }

    public void setId(Long id) { this.id = id; }
    public void setTopicoId(Long topicoId) { this.topicoId = topicoId; }
    public void setAutorId(Long autorId) { this.autorId = autorId; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}