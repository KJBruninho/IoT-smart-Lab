package com.iotroom.iotroom.dto;

public class AdminPedidoRespostaForm {

    private String resposta;

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta != null ? resposta.trim() : null;
    }
}