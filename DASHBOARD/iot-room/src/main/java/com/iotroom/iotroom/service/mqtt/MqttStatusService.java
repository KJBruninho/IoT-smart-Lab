package com.iotroom.iotroom.service.mqtt;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class MqttStatusService {
    private volatile LocalDateTime ultimaMensagemRecebida;
    private volatile boolean clienteLigado;

    public void marcarClienteLigado() {
        this.clienteLigado = true;
    }

    public void marcarClienteDesligado() {
        this.clienteLigado = false;
    }

    public void atualizarUltimaMensagem() {
        this.ultimaMensagemRecebida = LocalDateTime.now();
        this.clienteLigado = true;
    }

    public boolean isOnline() {
        if (!clienteLigado || ultimaMensagemRecebida == null) {
            return false;
        }

        return Duration.between(ultimaMensagemRecebida, LocalDateTime.now()).getSeconds() <= 30;
    }

    public LocalDateTime getUltimaMensagemRecebida() {
        return ultimaMensagemRecebida;
    }
}
