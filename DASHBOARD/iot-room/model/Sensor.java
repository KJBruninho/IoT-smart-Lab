package com.iotroom.iotroom.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sensores")
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String tipo;

    private String unidade;

    private Boolean ativo;

    @Column(name = "remoto_ativo")
    private Boolean remotoAtivo = true;

    @Column(name = "fator_calibracao")
    private BigDecimal fatorCalibracao = BigDecimal.ONE;

    @Column(name = "offset_calibracao")
    private BigDecimal offsetCalibracao = BigDecimal.ZERO;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estacao_id", nullable = false)
    private Estacao estacao;

    @OneToMany(mappedBy = "sensor")
    private List<LeituraSensor> leituras;

    @PrePersist
    public void prePersist() {
        if (ativo == null) ativo = true;
        if (remotoAtivo == null) remotoAtivo = true;
        if (fatorCalibracao == null) fatorCalibracao = BigDecimal.ONE;
        if (offsetCalibracao == null) offsetCalibracao = BigDecimal.ZERO;
        if (criadoEm == null) criadoEm = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public String getUnidade() { return unidade; }
    public Boolean getAtivo() { return ativo; }
    public Boolean getRemotoAtivo() { return remotoAtivo; }
    public BigDecimal getFatorCalibracao() { return fatorCalibracao; }
    public BigDecimal getOffsetCalibracao() { return offsetCalibracao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public Estacao getEstacao() { return estacao; }
    public List<LeituraSensor> getLeituras() { return leituras; }

    public void setId(Long id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setUnidade(String unidade) { this.unidade = unidade; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public void setRemotoAtivo(Boolean remotoAtivo) { this.remotoAtivo = remotoAtivo; }
    public void setFatorCalibracao(BigDecimal fatorCalibracao) { this.fatorCalibracao = fatorCalibracao; }
    public void setOffsetCalibracao(BigDecimal offsetCalibracao) { this.offsetCalibracao = offsetCalibracao; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public void setEstacao(Estacao estacao) { this.estacao = estacao; }
    public void setLeituras(List<LeituraSensor> leituras) { this.leituras = leituras; }
}