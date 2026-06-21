package com.iotroom.auth.dto;

import java.time.LocalDateTime;

public class ErrorResponseDTO {

    private LocalDateTime timestamp;
    private int status;
    private String erro;
    private String detalhe;

    public ErrorResponseDTO() {
    }

    public ErrorResponseDTO(int status, String erro, String detalhe) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.erro = erro;
        this.detalhe = detalhe;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getErro() {
        return erro;
    }

    public String getDetalhe() {
        return detalhe;
    }
}