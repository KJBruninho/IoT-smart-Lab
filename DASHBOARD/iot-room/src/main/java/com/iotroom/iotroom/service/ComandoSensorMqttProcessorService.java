package com.iotroom.iotroom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iotroom.iotroom.dto.MqttComandoPayloadDTO;
import com.iotroom.iotroom.dto.MqttComandoPendenteDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComandoSensorMqttProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(ComandoSensorMqttProcessorService.class);

    private static final int MAX_TENTATIVAS = 5;

    private final JdbcTemplate jdbcTemplate;
    private final MqttClientService mqttClientService;
    private final ObjectMapper objectMapper;

    public ComandoSensorMqttProcessorService(
            JdbcTemplate jdbcTemplate,
            MqttClientService mqttClientService,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.mqttClientService = mqttClientService;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void processarComandosPendentes() {
        List<MqttComandoPendenteDTO> comandos = buscarComandosPendentes();

        if (!comandos.isEmpty()) {
            logger.info("[COMANDOS] {} comando(s) pendente(s) encontrado(s).", comandos.size());
        }

        for (MqttComandoPendenteDTO comando : comandos) {
            processarComando(comando);
        }
    }

    private List<MqttComandoPendenteDTO> buscarComandosPendentes() {
        return jdbcTemplate.query("""
                SELECT
                    id,
                    sensor_id,
                    device_id,
                    tipo_sensor,
                    comando,
                    tentativas_envio,
                    criado_em
                FROM comandos_sensor
                WHERE estado = 'ENVIADO'
                AND confirmado_em IS NULL
                AND tentativas_envio < ?
                AND (
                    publicado_em IS NULL
                    OR publicado_em < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 30 SECOND)
                )
                ORDER BY criado_em ASC
                LIMIT 20
                """,
                (rs, rowNum) -> new MqttComandoPendenteDTO(
                        rs.getLong("id"),
                        rs.getLong("sensor_id"),
                        rs.getString("device_id"),
                        rs.getString("tipo_sensor"),
                        rs.getString("comando"),
                        rs.getInt("tentativas_envio"),
                        toLocalDateTime(rs.getTimestamp("criado_em"))
                ),
                MAX_TENTATIVAS
        );
    }

    private void processarComando(MqttComandoPendenteDTO comando) {
        try {
            logger.info(
                    "[COMANDO PROCESSAR] id={} sensorId={} deviceId={} tipo={} tentativaAtual={} comando={}",
                    comando.id(),
                    comando.sensorId(),
                    comando.deviceId(),
                    comando.tipoSensor(),
                    comando.tentativasEnvio(),
                    comando.comando()
            );

            MqttComandoPayloadDTO payload = new MqttComandoPayloadDTO(
                    comando.id(),
                    comando.sensorId(),
                    comando.deviceId(),
                    comando.tipoSensor(),
                    comando.comando()
            );

            String payloadJson = objectMapper.writeValueAsString(payload);

            mqttClientService.publicarComando(comando.deviceId(), payloadJson);

            marcarPublicado(comando.id());

            logger.info("[COMANDO PUBLICADO] id={} deviceId={}", comando.id(), comando.deviceId());

        } catch (Exception e) {
            logger.error("[COMANDO ERRO] id={} erro={}", comando.id(), e.getMessage(), e);
            marcarFalha(comando, e.getMessage());
        }
    }

    private void marcarPublicado(Long comandoId) {
        jdbcTemplate.update("""
                UPDATE comandos_sensor
                SET publicado_em = CURRENT_TIMESTAMP,
                    tentativas_envio = tentativas_envio + 1,
                    ultimo_erro = NULL
                WHERE id = ?
                """,
                comandoId
        );
    }

    private void marcarFalha(MqttComandoPendenteDTO comando, String erro) {
        int novaTentativa = comando.tentativasEnvio() + 1;

        if (novaTentativa >= MAX_TENTATIVAS) {
            jdbcTemplate.update("""
                    UPDATE comandos_sensor
                    SET estado = 'ERRO',
                        tentativas_envio = tentativas_envio + 1,
                        ultimo_erro = ?,
                        resposta = ?
                    WHERE id = ?
                    """,
                    limitarTexto(erro),
                    limitarTexto(erro),
                    comando.id()
            );

            marcarPedidoComoErro(comando.id());

            logger.error(
                    "[COMANDO ERRO FINAL] id={} atingiu limite de tentativas ({})",
                    comando.id(),
                    MAX_TENTATIVAS
            );

        } else {
            jdbcTemplate.update("""
                    UPDATE comandos_sensor
                    SET tentativas_envio = tentativas_envio + 1,
                        ultimo_erro = ?
                    WHERE id = ?
                    """,
                    limitarTexto(erro),
                    comando.id()
            );

            logger.warn(
                    "[COMANDO RETENTAR] id={} tentativa={}/{} erro={}",
                    comando.id(),
                    novaTentativa,
                    MAX_TENTATIVAS,
                    erro
            );
        }
    }

    private void marcarPedidoComoErro(Long comandoId) {
        jdbcTemplate.update("""
                UPDATE pedidos_configuracao_sensor
                SET estado = 'ERRO'
                WHERE comando_id = ?
                AND estado IN ('PENDENTE', 'APROVADO')
                """,
                comandoId
        );
    }

    private String limitarTexto(String texto) {
        if (texto == null) {
            return null;
        }

        if (texto.length() <= 1000) {
            return texto;
        }

        return texto.substring(0, 1000);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}