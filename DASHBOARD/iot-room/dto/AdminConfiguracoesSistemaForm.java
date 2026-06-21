package com.iotroom.iotroom.dto;

public class AdminConfiguracoesSistemaForm {

    private boolean modoManutencao;
    private boolean permitirCalibracaoProfessor;
    private boolean permitirControloRemotoProfessor;
    private boolean permitirPedidosIntervalo;

    private Integer intervaloMinimoMs;
    private Integer intervaloMaximoMs;
    private Integer intervaloRapidoPadraoMs;
    private Integer intervaloEstavelPadraoMs;
    private Integer duracaoModoRapidoPadraoMs;

    private Integer retencaoLeiturasDias;
    private Integer retencaoLogsDias;

    private String mqttHost;
    private Integer mqttPorta;
    private String mqttTopicoBase;

    private Integer timeoutSemComunicacaoSegundos;

    public boolean isModoManutencao() {
        return modoManutencao;
    }

    public void setModoManutencao(boolean modoManutencao) {
        this.modoManutencao = modoManutencao;
    }

    public boolean isPermitirCalibracaoProfessor() {
        return permitirCalibracaoProfessor;
    }

    public void setPermitirCalibracaoProfessor(boolean permitirCalibracaoProfessor) {
        this.permitirCalibracaoProfessor = permitirCalibracaoProfessor;
    }

    public boolean isPermitirControloRemotoProfessor() {
        return permitirControloRemotoProfessor;
    }

    public void setPermitirControloRemotoProfessor(boolean permitirControloRemotoProfessor) {
        this.permitirControloRemotoProfessor = permitirControloRemotoProfessor;
    }

    public boolean isPermitirPedidosIntervalo() {
        return permitirPedidosIntervalo;
    }

    public void setPermitirPedidosIntervalo(boolean permitirPedidosIntervalo) {
        this.permitirPedidosIntervalo = permitirPedidosIntervalo;
    }

    public Integer getIntervaloMinimoMs() {
        return intervaloMinimoMs;
    }

    public void setIntervaloMinimoMs(Integer intervaloMinimoMs) {
        this.intervaloMinimoMs = intervaloMinimoMs;
    }

    public Integer getIntervaloMaximoMs() {
        return intervaloMaximoMs;
    }

    public void setIntervaloMaximoMs(Integer intervaloMaximoMs) {
        this.intervaloMaximoMs = intervaloMaximoMs;
    }

    public Integer getIntervaloRapidoPadraoMs() {
        return intervaloRapidoPadraoMs;
    }

    public void setIntervaloRapidoPadraoMs(Integer intervaloRapidoPadraoMs) {
        this.intervaloRapidoPadraoMs = intervaloRapidoPadraoMs;
    }

    public Integer getIntervaloEstavelPadraoMs() {
        return intervaloEstavelPadraoMs;
    }

    public void setIntervaloEstavelPadraoMs(Integer intervaloEstavelPadraoMs) {
        this.intervaloEstavelPadraoMs = intervaloEstavelPadraoMs;
    }

    public Integer getDuracaoModoRapidoPadraoMs() {
        return duracaoModoRapidoPadraoMs;
    }

    public void setDuracaoModoRapidoPadraoMs(Integer duracaoModoRapidoPadraoMs) {
        this.duracaoModoRapidoPadraoMs = duracaoModoRapidoPadraoMs;
    }

    public Integer getRetencaoLeiturasDias() {
        return retencaoLeiturasDias;
    }

    public void setRetencaoLeiturasDias(Integer retencaoLeiturasDias) {
        this.retencaoLeiturasDias = retencaoLeiturasDias;
    }

    public Integer getRetencaoLogsDias() {
        return retencaoLogsDias;
    }

    public void setRetencaoLogsDias(Integer retencaoLogsDias) {
        this.retencaoLogsDias = retencaoLogsDias;
    }

    public String getMqttHost() {
        return mqttHost;
    }

    public void setMqttHost(String mqttHost) {
        this.mqttHost = mqttHost;
    }

    public Integer getMqttPorta() {
        return mqttPorta;
    }

    public void setMqttPorta(Integer mqttPorta) {
        this.mqttPorta = mqttPorta;
    }

    public String getMqttTopicoBase() {
        return mqttTopicoBase;
    }

    public void setMqttTopicoBase(String mqttTopicoBase) {
        this.mqttTopicoBase = mqttTopicoBase;
    }

    public Integer getTimeoutSemComunicacaoSegundos() {
        return timeoutSemComunicacaoSegundos;
    }

    public void setTimeoutSemComunicacaoSegundos(Integer timeoutSemComunicacaoSegundos) {
        this.timeoutSemComunicacaoSegundos = timeoutSemComunicacaoSegundos;
    }
}