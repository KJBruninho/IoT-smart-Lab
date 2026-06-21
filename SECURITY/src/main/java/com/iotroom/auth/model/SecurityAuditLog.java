package com.iotroom.auth.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_audit_logs")
public class SecurityAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilizador_id")
    private Utilizador utilizador;

    @Column(name = "tipo", nullable = false, length = 80)
    private String tipo;

    @Column(name = "detalhe", length = 255)
    private String detalhe;

    @Column(name = "app_client", length = 50)
    private String appClient;

    @Column(name = "ip", length = 100)
    private String ip;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public SecurityAuditLog() {
    }

    @PrePersist
    public void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Utilizador getUtilizador() {
        return utilizador;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDetalhe() {
        return detalhe;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setUtilizador(Utilizador utilizador) {
        this.utilizador = utilizador;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setDetalhe(String detalhe) {
        this.detalhe = detalhe;
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
}