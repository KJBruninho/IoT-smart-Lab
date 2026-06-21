package com.iotroom.iotroom.dto;

import java.math.BigDecimal;

public class AdminSensorForm {

    private Long id;
    private String nome;
    private String tipo = "TEMPERATURA";
    private String unidade = "ºC";
    private Long estacaoId;

    private boolean ativo = true;
    private boolean remotoAtivo = true;

    private BigDecimal fatorCalibracao = new BigDecimal("1.000000");
    private BigDecimal offsetCalibracao = new BigDecimal("0.000000");

    private Integer intervaloRapidoMs = 1000;
    private Integer intervaloEstavelMs = 30000;
    private Integer duracaoModoRapidoMs = 120000;
    private BigDecimal deltaSignificativo = new BigDecimal("1.0000");

    public static AdminSensorForm from(AdminSensorDTO dto) {
        AdminSensorForm form = new AdminSensorForm();

        form.setId(dto.id());
        form.setNome(dto.nome());
        form.setTipo(dto.tipo());
        form.setUnidade(dto.unidade());
        form.setEstacaoId(dto.estacaoId());

        form.setAtivo(dto.ativo());
        form.setRemotoAtivo(dto.remotoAtivo());

        form.setFatorCalibracao(dto.fatorCalibracao());
        form.setOffsetCalibracao(dto.offsetCalibracao());

        form.setIntervaloRapidoMs(dto.intervaloRapidoMs());
        form.setIntervaloEstavelMs(dto.intervaloEstavelMs());
        form.setDuracaoModoRapidoMs(dto.duracaoModoRapidoMs());
        form.setDeltaSignificativo(dto.deltaSignificativo());

        return form;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome != null ? nome.trim() : null;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo != null ? tipo.trim().toUpperCase() : "TEMPERATURA";
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade != null ? unidade.trim() : null;
    }

    public Long getEstacaoId() {
        return estacaoId;
    }

    public void setEstacaoId(Long estacaoId) {
        this.estacaoId = estacaoId;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isRemotoAtivo() {
        return remotoAtivo;
    }

    public void setRemotoAtivo(boolean remotoAtivo) {
        this.remotoAtivo = remotoAtivo;
    }

    public BigDecimal getFatorCalibracao() {
        return fatorCalibracao;
    }

    public void setFatorCalibracao(BigDecimal fatorCalibracao) {
        this.fatorCalibracao = fatorCalibracao;
    }

    public BigDecimal getOffsetCalibracao() {
        return offsetCalibracao;
    }

    public void setOffsetCalibracao(BigDecimal offsetCalibracao) {
        this.offsetCalibracao = offsetCalibracao;
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
}