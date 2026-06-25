package com.iotroom.iotroom.dto;

import java.math.BigDecimal;

public class AlunoPedidoModoForm {
    private Long sensorId;
    private Integer intervaloRapidoMs = 1000;
    private Integer intervaloEstavelMs = 30000;
    private Integer duracaoModoRapidoMs = 120000;
    private BigDecimal deltaSignificativo = new BigDecimal("1.0000");
    private String motivo;

    public Long getSensorId() {
        return sensorId;
    }

    public void setSensorId(Long sensorId) {
        this.sensorId = sensorId;
    }

    public Integer getIntervaloRapidoMs() {
        return intervaloRapidoMs;
    }

    public void setIntervaloRapidoMs(Integer intervaloRapidoMs) {
        this.intervaloRapidoMs = intervaloRapidoMs;
    }

    public Integer getIntervaloEstavelMs() {
        return intervaloEstavelMs;
    }

    public void setIntervaloEstavelMs(Integer intervaloEstavelMs) {
        this.intervaloEstavelMs = intervaloEstavelMs;
    }

    public Integer getDuracaoModoRapidoMs() {
        return duracaoModoRapidoMs;
    }

    public void setDuracaoModoRapidoMs(Integer duracaoModoRapidoMs) {
        this.duracaoModoRapidoMs = duracaoModoRapidoMs;
    }

    public BigDecimal getDeltaSignificativo() {
        return deltaSignificativo;
    }

    public void setDeltaSignificativo(BigDecimal deltaSignificativo) {
        this.deltaSignificativo = deltaSignificativo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
