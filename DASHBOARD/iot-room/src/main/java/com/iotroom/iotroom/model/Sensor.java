package com.iotroom.iotroom.model;

import jakarta.persistence.*;
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

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @ManyToOne
    @JoinColumn(name = "estacao_id", nullable = false)
    private Estacao estacao;

    @OneToMany(mappedBy = "sensor")
    private List<LeituraSensor> leituras;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public Estacao getEstacao() { return estacao; }
    public void setEstacao(Estacao estacao) { this.estacao = estacao; }
    public List<LeituraSensor> getLeituras() { return leituras; }
    public void setLeituras(List<LeituraSensor> leituras) { this.leituras = leituras; }
}
