package com.iotroom.auth.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizador_id", nullable = false)
    private Utilizador utilizador;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "device_id_hash", length = 255)
    private String deviceIdHash;

    @Column(name = "app_client", nullable = false, length = 50)
    private String appClient = "WEB";

    @Column(name = "ip", length = 100)
    private String ip;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "ultimo_uso_em")
    private LocalDateTime ultimoUsoEm;

    @Column(name = "revogado_em")
    private LocalDateTime revogadoEm;

    public RefreshToken() {
    }

    @PrePersist
    public void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
        if (appClient == null || appClient.isBlank()) {
            appClient = "WEB";
        }
    }

    public boolean estaRevogado() {
        return revogadoEm != null;
    }

    public boolean estaExpirado() {
        return expiraEm != null && expiraEm.isBefore(LocalDateTime.now());
    }

    public boolean estaValido() {
        return !estaRevogado() && !estaExpirado();
    }

    public Long getId() {
        return id;
    }

    public Utilizador getUtilizador() {
        return utilizador;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public String getDeviceIdHash() {
        return deviceIdHash;
    }

    public String getAppClient() {
        return appClient;
    }

    public String getIp() {
        return ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public LocalDateTime getUltimoUsoEm() {
        return ultimoUsoEm;
    }

    public LocalDateTime getRevogadoEm() {
        return revogadoEm;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUtilizador(Utilizador utilizador) {
        this.utilizador = utilizador;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public void setDeviceIdHash(String deviceIdHash) {
        this.deviceIdHash = deviceIdHash;
    }

    public void setAppClient(String appClient) {
        this.appClient = appClient;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public void setExpiraEm(LocalDateTime expiraEm) {
        this.expiraEm = expiraEm;
    }

    public void setUltimoUsoEm(LocalDateTime ultimoUsoEm) {
        this.ultimoUsoEm = ultimoUsoEm;
    }

    public void setRevogadoEm(LocalDateTime revogadoEm) {
        this.revogadoEm = revogadoEm;
    }
}