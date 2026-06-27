package com.iotroom.iotroom.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DashboardAlertaDTO {

    private Long id;
    private String titulo;
    private String mensagem;
    private String severidade;
    private String estado;
    private String tipoSensor;
    private BigDecimal valorLido;
    private LocalDateTime criadoEm;
    private String tempoRelativo;

    public DashboardAlertaDTO(DashboardAlertaProjection projection, String tempoRelativo) {
        this.id = projection.getId();
        this.titulo = projection.getTitulo();
        this.mensagem = projection.getMensagem();
        this.severidade = projection.getSeveridade();
        this.estado = projection.getEstado();
        this.tipoSensor = projection.getTipoSensor();
        this.valorLido = projection.getValorLido();
        this.tempoRelativo = tempoRelativo;

        if (projection.getCriadoEm() != null) {
            this.criadoEm = projection.getCriadoEm().toLocalDateTime();
        }
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getMensagem() { return mensagem; }
    public String getSeveridade() { return severidade; }
    public String getEstado() { return estado; }
    public String getTipoSensor() { return tipoSensor; }
    public BigDecimal getValorLido() { return valorLido; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public String getTempoRelativo() { return tempoRelativo; }
}