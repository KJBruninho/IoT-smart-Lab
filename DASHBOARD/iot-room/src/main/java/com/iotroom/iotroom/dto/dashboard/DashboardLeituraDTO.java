package com.iotroom.iotroom.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DashboardLeituraDTO {

    private Long leituraId;
    private String sensorNome;
    private String tipoSensor;
    private String unidade;
    private BigDecimal valor;
    private LocalDateTime dataRegisto;
    private String tempoRelativo;
    private String estacaoNome;
    private String deviceId;
    private String experienciaNome;

    public DashboardLeituraDTO(DashboardLeituraProjection projection, String tempoRelativo) {
        this.leituraId = projection.getLeituraId();
        this.sensorNome = projection.getSensorNome();
        this.tipoSensor = projection.getTipoSensor();
        this.unidade = projection.getUnidade();
        this.valor = projection.getValor();
        this.estacaoNome = projection.getEstacaoNome();
        this.deviceId = projection.getDeviceId();
        this.experienciaNome = projection.getExperienciaNome();
        this.tempoRelativo = tempoRelativo;

        if (projection.getDataRegisto() != null) {
            this.dataRegisto = projection.getDataRegisto().toLocalDateTime();
        }
    }

    public Long getLeituraId() { return leituraId; }
    public String getSensorNome() { return sensorNome; }
    public String getTipoSensor() { return tipoSensor; }
    public String getUnidade() { return unidade; }
    public BigDecimal getValor() { return valor; }
    public LocalDateTime getDataRegisto() { return dataRegisto; }
    public String getTempoRelativo() { return tempoRelativo; }
    public String getEstacaoNome() { return estacaoNome; }
    public String getDeviceId() { return deviceId; }
    public String getExperienciaNome() { return experienciaNome; }
}