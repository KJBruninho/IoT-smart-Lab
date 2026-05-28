package com.iotroom.iotroom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "estacoes")
public class Estacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String localizacao;

    @Column(name = "device_id", nullable = false, unique = true)
    private String deviceId;

    private Boolean ativa;

    @Column(name = "criada_em")
    private LocalDateTime criadaEm;

    @OneToMany(mappedBy = "estacao")
    private List<Sensor> sensores;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public Boolean getAtiva() { return ativa; }
    public void setAtiva(Boolean ativa) { this.ativa = ativa; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public void setCriadaEm(LocalDateTime criadaEm) { this.criadaEm = criadaEm; }
    public List<Sensor> getSensores() { return sensores; }
    public void setSensores(List<Sensor> sensores) { this.sensores = sensores; }
}
