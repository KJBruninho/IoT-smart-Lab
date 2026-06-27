package com.iotroom.iotroom.service.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iotroom.iotroom.dto.mqtt.MqttComandoAckDTO;

@Service
public class MqttComandoAckService {

    private static final Logger logger = LoggerFactory.getLogger(MqttComandoAckService.class);

    private final JdbcTemplate jdbcTemplate;

    public MqttComandoAckService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void processarAck(MqttComandoAckDTO ack) {
        if (ack == null || ack.commandId() == null) {
            logger.warn("[ACK] Ignorado: ACK nulo ou sem commandId.");
            return;
        }

        String status = ack.status() != null ? ack.status().trim().toUpperCase() : "CONFIRMADO";
        String mensagem = ack.message() != null ? ack.message().trim() : null;

        if ("OK".equals(status)) {
            status = "CONFIRMADO";
        }

        logger.info(
                "[ACK] Processar commandId={} status={} mensagem={}",
                ack.commandId(),
                status,
                mensagem
        );

        if ("CONFIRMADO".equals(status)) {
            confirmarComando(ack.commandId(), mensagem);
            marcarPedidoComoAplicado(ack.commandId());

            logger.info("[ACK OK] Comando {} confirmado.", ack.commandId());
            return;
        }

        if ("ERRO".equals(status) || "ERROR".equals(status)) {
            marcarComandoComoErro(ack.commandId(), mensagem);
            marcarPedidoComoErro(ack.commandId());

            logger.warn("[ACK ERRO] Comando {} marcado como erro. Mensagem={}", ack.commandId(), mensagem);
            return;
        }

        logger.warn("[ACK] Status desconhecido recebido: commandId={} status={}", ack.commandId(), status);
    }

    private void confirmarComando(Long comandoId, String mensagem) {
        int updated = jdbcTemplate.update("""
                UPDATE comandos_sensor
                SET estado = 'CONFIRMADO',
                    resposta = ?,
                    confirmado_em = CURRENT_TIMESTAMP,
                    ultimo_erro = NULL
                WHERE id = ?
                """,
                mensagem,
                comandoId
        );

        if (updated == 0) {
            logger.warn("[ACK] Nenhum comando atualizado para commandId={}", comandoId);
        }
    }

    private void marcarComandoComoErro(Long comandoId, String mensagem) {
        int updated = jdbcTemplate.update("""
                UPDATE comandos_sensor
                SET estado = 'ERRO',
                    resposta = ?,
                    ultimo_erro = ?,
                    confirmado_em = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                mensagem,
                mensagem,
                comandoId
        );

        if (updated == 0) {
            logger.warn("[ACK] Nenhum comando em erro atualizado para commandId={}", comandoId);
        }
    }

    private void marcarPedidoComoAplicado(Long comandoId) {
        int updated = jdbcTemplate.update("""
                UPDATE pedidos_configuracao_sensor
                SET estado = 'APLICADO',
                    aplicado_em = CURRENT_TIMESTAMP
                WHERE comando_id = ?
                AND estado = 'APROVADO'
                """,
                comandoId
        );

        if (updated > 0) {
            logger.info("[ACK] Pedido associado ao comando {} marcado como APLICADO.", comandoId);
        }
    }

    private void marcarPedidoComoErro(Long comandoId) {
        int updated = jdbcTemplate.update("""
                UPDATE pedidos_configuracao_sensor
                SET estado = 'ERRO'
                WHERE comando_id = ?
                AND estado IN ('PENDENTE', 'APROVADO')
                """,
                comandoId
        );

        if (updated > 0) {
            logger.warn("[ACK] Pedido associado ao comando {} marcado como ERRO.", comandoId);
        }
    }
}