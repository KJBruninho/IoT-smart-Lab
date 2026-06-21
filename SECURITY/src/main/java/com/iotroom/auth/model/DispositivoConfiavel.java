package com.iotroom.auth.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dispositivos_confiaveis")
public class DispositivoConfiavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizador_id", nullable = false)
    private Utilizador utilizador;

    @Column(name = "device_id_hash", nullable = false, length = 255)
    private String deviceIdHash;

    @Column(name = "nome_dispositivo", length = 150)
    private String nomeDispositivo;

    @Column(name = "plataforma", length = 50)
    private String plataforma;

    @Column(name = "app_client", nullable = false, length = 50)
    private String appClient = "WEB";

    @Column(name = "biometria_ativa", nullable = false)
    private Boolean biometriaAtiva = false;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "ultimo_acesso_em")
    private LocalDateTime ultimoAcessoEm;

    public DispositivoConfiavel() {
    }

    @PrePersist
    public void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
        if (ativo == null) {
            ativo = true;
        }
        if (biometriaAtiva == null) {
            biometriaAtiva = false;
        }
        if (appClient == null || appClient.isBlank()) {
            appClient = "WEB";
        }
    }

    public Long getId() {
        return id;
    }

    public Utilizador getUtilizador() {
        return utilizador;
    }

    public String getDeviceIdHash() {
        return deviceIdHash;
    }

    public String getNomeDispositivo() {
        return nomeDispositivo;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public String getAppClient() {
        return appClient;
    }

    public Boolean getBiometriaAtiva() {
        return biometriaAtiva;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getUltimoAcessoEm() {
        return ultimoAcessoEm;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUtilizador(Utilizador utilizador) {
        this.utilizador = utilizador;
    }

    public void setDeviceIdHash(String deviceIdHash) {
        this.deviceIdHash = deviceIdHash;
    }

    public void setNomeDispositivo(String nomeDispositivo) {
        this.nomeDispositivo = nomeDispositivo;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }

    public void setAppClient(String appClient) {
        this.appClient = appClient;
    }

    public void setBiometriaAtiva(Boolean biometriaAtiva) {
        this.biometriaAtiva = biometriaAtiva;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public void setUltimoAcessoEm(LocalDateTime ultimoAcessoEm) {
        this.ultimoAcessoEm = ultimoAcessoEm;
    }
}