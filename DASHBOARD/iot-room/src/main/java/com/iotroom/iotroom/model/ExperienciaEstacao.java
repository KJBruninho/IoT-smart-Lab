package com.iotroom.iotroom.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "experiencia_estacoes")
public class ExperienciaEstacao {

    @EmbeddedId
    private ExperienciaEstacaoId id;

    @Column(nullable = false)
    private Integer ordem = 1;

    @Column(length = 255)
    private String observacao;

    @Column(name = "adicionada_em")
    private LocalDateTime adicionadaEm;

    public ExperienciaEstacao() {
    }

    @PrePersist
    public void prePersist() {
        if (adicionadaEm == null) {
            adicionadaEm = LocalDateTime.now();
        }

        if (ordem == null) {
            ordem = 1;
        }
    }

    public ExperienciaEstacaoId getId() {
        return id;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public String getObservacao() {
        return observacao;
    }

    public LocalDateTime getAdicionadaEm() {
        return adicionadaEm;
    }

    public Long getExperienciaId() {
        return id != null ? id.getExperienciaId() : null;
    }

    public Long getEstacaoId() {
        return id != null ? id.getEstacaoId() : null;
    }

    public void setId(ExperienciaEstacaoId id) {
        this.id = id;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public void setAdicionadaEm(LocalDateTime adicionadaEm) {
        this.adicionadaEm = adicionadaEm;
    }
}