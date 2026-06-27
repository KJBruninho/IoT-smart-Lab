package com.iotroom.iotroom.dto.professor;

import java.math.BigDecimal;

public class SensorModoFormDTO {

    private Integer intervaloRapidoMs;
    private Integer intervaloEstavelMs;
    private Integer duracaoModoRapidoMs;
    private BigDecimal deltaSignificativo;

    public SensorModoFormDTO() {}

    public SensorModoFormDTO(
            Integer intervaloRapidoMs,
            Integer intervaloEstavelMs,
            Integer duracaoModoRapidoMs,
            BigDecimal deltaSignificativo
    ) {
        this.intervaloRapidoMs = intervaloRapidoMs;
        this.intervaloEstavelMs = intervaloEstavelMs;
        this.duracaoModoRapidoMs = duracaoModoRapidoMs;
        this.deltaSignificativo = deltaSignificativo;
    }

    public Integer getIntervaloRapidoMs() { return intervaloRapidoMs; }
    public Integer getIntervaloEstavelMs() { return intervaloEstavelMs; }
    public Integer getDuracaoModoRapidoMs() { return duracaoModoRapidoMs; }
    public BigDecimal getDeltaSignificativo() { return deltaSignificativo; }

    public void setIntervaloRapidoMs(Integer intervaloRapidoMs) { this.intervaloRapidoMs = intervaloRapidoMs; }
    public void setIntervaloEstavelMs(Integer intervaloEstavelMs) { this.intervaloEstavelMs = intervaloEstavelMs; }
    public void setDuracaoModoRapidoMs(Integer duracaoModoRapidoMs) { this.duracaoModoRapidoMs = duracaoModoRapidoMs; }
    public void setDeltaSignificativo(BigDecimal deltaSignificativo) { this.deltaSignificativo = deltaSignificativo; }
}