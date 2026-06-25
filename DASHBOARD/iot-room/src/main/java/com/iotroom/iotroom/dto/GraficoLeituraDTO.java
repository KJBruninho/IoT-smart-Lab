package com.iotroom.iotroom.dto;

import java.math.BigDecimal;

public class GraficoLeituraDTO {
    private String hora;
    private BigDecimal valor;

    public GraficoLeituraDTO(String hora, BigDecimal valor) {
        this.hora = hora;
        this.valor = valor;
    }

    public String getHora() { return hora; }
    public BigDecimal getValor() { return valor; }
}
