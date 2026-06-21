package com.iotroom.auth.dto;

import java.time.LocalDateTime;

public class DeviceResponseDTO {

    private Long id;
    private String nomeDispositivo;
    private String plataforma;
    private String appClient;
    private Boolean biometriaAtiva;
    private Boolean ativo;
    private LocalDateTime criadoEm;
    private LocalDateTime ultimoAcessoEm;

    public DeviceResponseDTO() {
    }

    public DeviceResponseDTO(
            Long id,
            String nomeDispositivo,
            String plataforma,
            String appClient,
            Boolean biometriaAtiva,
            Boolean ativo,
            LocalDateTime criadoEm,
            LocalDateTime ultimoAcessoEm
    ) {
        this.id = id;
        this.nomeDispositivo = nomeDispositivo;
        this.plataforma = plataforma;
        this.appClient = appClient;
        this.biometriaAtiva = biometriaAtiva;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
        this.ultimoAcessoEm = ultimoAcessoEm;
    }

    public Long getId() {
        return id;
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
}