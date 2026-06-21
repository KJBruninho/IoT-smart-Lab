package com.iotroom.iotroom.dto;

import java.time.LocalDateTime;

public class DashboardEstacaoStatusDTO {

    private Long estacaoId;
    private String estacaoNome;
    private String deviceId;
    private LocalDateTime ultimaLeituraEm;
    private String ultimaLeituraTexto;
    private Long totalSensores;
    private String tiposSensores;
    private String estado;
    private String estadoCss;

    public DashboardEstacaoStatusDTO(
            DashboardEstacaoProjection projection,
            String ultimaLeituraTexto,
            String estado,
            String estadoCss
    ) {
        this.estacaoId = projection.getEstacaoId();
        this.estacaoNome = projection.getEstacaoNome();
        this.deviceId = projection.getDeviceId();
        this.totalSensores = projection.getTotalSensores();
        this.tiposSensores = projection.getTiposSensores();
        this.ultimaLeituraTexto = ultimaLeituraTexto;
        this.estado = estado;
        this.estadoCss = estadoCss;

        if (projection.getUltimaLeituraEm() != null) {
            this.ultimaLeituraEm = projection.getUltimaLeituraEm().toLocalDateTime();
        }
    }

    public Long getEstacaoId() { return estacaoId; }
    public String getEstacaoNome() { return estacaoNome; }
    public String getDeviceId() { return deviceId; }
    public LocalDateTime getUltimaLeituraEm() { return ultimaLeituraEm; }
    public String getUltimaLeituraTexto() { return ultimaLeituraTexto; }
    public Long getTotalSensores() { return totalSensores; }
    public String getTiposSensores() { return tiposSensores; }
    public String getEstado() { return estado; }
    public String getEstadoCss() { return estadoCss; }
}