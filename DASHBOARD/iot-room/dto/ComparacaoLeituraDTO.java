package com.iotroom.iotroom.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ComparacaoLeituraDTO {

    private Long leituraId;
    private String grupoNome;
    private String experienciaNome;
    private String estacaoNome;
    private String deviceId;
    private String sensorNome;
    private String tipoSensor;
    private String unidade;
    private BigDecimal valor;
    private LocalDateTime dataRegisto;

    public ComparacaoLeituraDTO() {
    }

    public ComparacaoLeituraDTO(ComparacaoLeituraProjection projection) {
        this.leituraId = projection.getLeituraId();
        this.grupoNome = projection.getGrupoNome();
        this.experienciaNome = projection.getExperienciaNome();
        this.estacaoNome = projection.getEstacaoNome();
        this.deviceId = projection.getDeviceId();
        this.sensorNome = projection.getSensorNome();
        this.tipoSensor = projection.getTipoSensor();
        this.unidade = projection.getUnidade();
        this.valor = projection.getValor();

        if (projection.getDataRegisto() != null) {
            this.dataRegisto = projection.getDataRegisto().toLocalDateTime();
        }
    }

    public Long getLeituraId() { return leituraId; }
    public String getGrupoNome() { return grupoNome; }
    public String getExperienciaNome() { return experienciaNome; }
    public String getEstacaoNome() { return estacaoNome; }
    public String getDeviceId() { return deviceId; }
    public String getSensorNome() { return sensorNome; }
    public String getTipoSensor() { return tipoSensor; }
    public String getUnidade() { return unidade; }
    public BigDecimal getValor() { return valor; }
    public LocalDateTime getDataRegisto() { return dataRegisto; }
}