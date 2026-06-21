package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.LeituraCacheDTO;
import com.iotroom.iotroom.dto.LeituraEntradaDTO;
import com.iotroom.iotroom.model.LeituraSensor;
import com.iotroom.iotroom.model.Sensor;
import com.iotroom.iotroom.repository.LeituraSensorRepository;
import com.iotroom.iotroom.repository.SensorRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class LeituraService {

    private static final boolean DEBUG_LEITURAS = true;

    private final ProfessorAlertaService professorAlertaService;
    private final SensorRepository sensorRepository;
    private final LeituraSensorRepository leituraSensorRepository;
    private final LocalSqliteCacheService localSqliteCacheService;
    private final TransactionTemplate transactionTemplate;
    private final JdbcTemplate jdbcTemplate;

    public LeituraService(
            ProfessorAlertaService professorAlertaService,
            SensorRepository sensorRepository,
            LeituraSensorRepository leituraSensorRepository,
            LocalSqliteCacheService localSqliteCacheService,
            TransactionTemplate transactionTemplate,
            JdbcTemplate jdbcTemplate
    ) {
        this.professorAlertaService = professorAlertaService;
        this.sensorRepository = sensorRepository;
        this.leituraSensorRepository = leituraSensorRepository;
        this.localSqliteCacheService = localSqliteCacheService;
        this.transactionTemplate = transactionTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void registarLeitura(String deviceId, String tipoSensor, BigDecimal valor) {
        String tipoNormalizado = normalizarTipo(tipoSensor);
        String unidade = unidadePorTipo(tipoNormalizado);

        debug("[LEITURA ENTRADA RAW] deviceId=" + deviceId
                + " | tipoSensor=" + tipoSensor
                + " | tipoNormalizado=" + tipoNormalizado
                + " | valor=" + valor
                + " | unidade=" + unidade);

        LeituraEntradaDTO dto = new LeituraEntradaDTO(
                deviceId,
                tipoNormalizado,
                valor,
                unidade,
                LocalDateTime.now()
        );

        registarLeitura(dto);
    }

    public void registarLeitura(LeituraEntradaDTO dto) {
        debug("[LEITURA DTO RECEBIDO] " + dto);

        validar(dto);

        try {
            guardarNaBaseDados(dto);
            debug("[LEITURA OK] Gravada na BD principal: " + dto);

        } catch (IllegalArgumentException e) {
            localSqliteCacheService.guardar(dto);

            System.err.println("[LEITURA ERRO LOGICO] Leitura guardada em SQLite local para tentar sincronizar depois.");
            System.err.println("[LEITURA ERRO LOGICO] " + e.getMessage());
            System.err.println("[LEITURA ERRO LOGICO] dto=" + dto);

        } catch (Exception e) {
            localSqliteCacheService.guardar(dto);

            System.err.println("[LEITURA ERRO BD] Leitura guardada em SQLite local.");
            System.err.println("[LEITURA ERRO BD] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.err.println("[LEITURA ERRO BD] dto=" + dto);

            if (DEBUG_LEITURAS) {
                e.printStackTrace();
            }
        }
    }

    public void guardarNaBaseDados(LeituraEntradaDTO dto) {
        validar(dto);

        transactionTemplate.executeWithoutResult(status -> {
            String deviceId = dto.deviceId() != null ? dto.deviceId().trim() : "";
            String tipoSensor = normalizarTipo(dto.tipoSensor());

            debug("[LEITURA BD INICIO] deviceId=" + deviceId
                    + " | tipoSensor=" + tipoSensor
                    + " | valor=" + dto.valor()
                    + " | unidade=" + dto.unidade()
                    + " | data=" + dto.dataRegisto());

            Sensor sensor = sensorRepository
                    .findByEstacaoDeviceIdAndTipoAndAtivoTrueAndEstacaoAtivaTrue(
                            deviceId,
                            tipoSensor
                    )
                    .orElseThrow(() -> {
                        debugDiagnosticoSensor(deviceId, tipoSensor);

                        return new IllegalArgumentException(
                                "Sensor não encontrado ou inativo. deviceId="
                                        + deviceId
                                        + " tipoSensor="
                                        + tipoSensor
                        );
                    });

            debug("[LEITURA SENSOR OK] sensorId=" + sensor.getId()
                    + " | tipoSensor=" + tipoSensor
                    + " | deviceId=" + deviceId);

            Long experienciaId = obterExperienciaAtivaPorDeviceESensor(
                    deviceId,
                    tipoSensor
            );

            debug("[LEITURA EXPERIENCIA OK] experienciaId=" + experienciaId
                    + " | deviceId=" + deviceId
                    + " | tipoSensor=" + tipoSensor
                    + " | sensorId=" + sensor.getId());

            LeituraSensor leitura = new LeituraSensor();
            leitura.setExperienciaId(experienciaId);
            leitura.setSensor(sensor);
            leitura.setUnidade(dto.unidade());
            leitura.setValor(dto.valor());
            leitura.setRegistadoEm(dto.dataRegisto() != null ? dto.dataRegisto() : LocalDateTime.now());

            LeituraSensor leituraGuardada = leituraSensorRepository.saveAndFlush(leitura);

            debug("[LEITURA GUARDADA] leituraId=" + leituraGuardada.getId()
                    + " | experienciaId=" + experienciaId
                    + " | sensorId=" + sensor.getId()
                    + " | tipoSensor=" + tipoSensor
                    + " | valor=" + dto.valor());

            marcarExperienciaEmExecucao(experienciaId);

            professorAlertaService.processarLeitura(leituraGuardada.getId());

            debug("[LEITURA ALERTAS OK] leituraId=" + leituraGuardada.getId());
        });
    }

    @Scheduled(fixedDelayString = "${cache.flush.delay-ms:10000}")
    public void sincronizarCacheLocal() {
        for (LeituraCacheDTO pendente : localSqliteCacheService.listarPendentes(100)) {
            try {
                debug("[CACHE SYNC TENTAR] cacheId=" + pendente.id()
                        + " | leitura=" + pendente.leitura());

                guardarNaBaseDados(pendente.leitura());
                localSqliteCacheService.marcarComoSincronizada(pendente.id());

                debug("[CACHE SYNC OK] cacheId=" + pendente.id());

            } catch (Exception e) {
                System.err.println("[CACHE SYNC FALHOU] cacheId=" + pendente.id());
                System.err.println("[CACHE SYNC FALHOU] " + e.getClass().getSimpleName() + ": " + e.getMessage());
                System.err.println("[CACHE SYNC FALHOU] leitura=" + pendente.leitura());

                if (DEBUG_LEITURAS) {
                    e.printStackTrace();
                }

                break;
            }
        }
    }

    private Long obterExperienciaAtivaPorDeviceESensor(String deviceId, String tipoSensor) {
        try {
            debug("[EXPERIENCIA QUERY] Procurar experiência para deviceId="
                    + deviceId
                    + " | tipoSensor="
                    + tipoSensor
                    + " | estados=ATIVA,EM_EXECUCAO,CRIADA");

            Long experienciaId = jdbcTemplate.queryForObject(
                    """
                    SELECT exp.id
                    FROM experiencias exp
                    INNER JOIN experiencia_estacoes ee ON ee.experiencia_id = exp.id
                    INNER JOIN estacoes e ON e.id = ee.estacao_id
                    INNER JOIN sensores s ON s.estacao_id = e.id
                    WHERE e.device_id = ?
                      AND s.tipo = ?
                      AND e.ativa = TRUE
                      AND s.ativo = TRUE
                      AND exp.estado IN ('ATIVA', 'EM_EXECUCAO', 'CRIADA')
                    ORDER BY exp.data_inicio DESC, exp.id DESC
                    LIMIT 1
                    """,
                    Long.class,
                    deviceId,
                    tipoSensor
            );

            debug("[EXPERIENCIA ENCONTRADA] experienciaId=" + experienciaId
                    + " | deviceId=" + deviceId
                    + " | tipoSensor=" + tipoSensor);

            return experienciaId;

        } catch (EmptyResultDataAccessException e) {
            debugDiagnosticoExperiencia(deviceId, tipoSensor);

            throw new IllegalArgumentException(
                    "Não existe experiência ativa para esta estação/sensor. deviceId="
                            + deviceId
                            + " tipoSensor="
                            + tipoSensor
            );
        }
    }

    private void marcarExperienciaEmExecucao(Long experienciaId) {
        int updated = jdbcTemplate.update(
                """
                UPDATE experiencias
                SET estado = 'EM_EXECUCAO'
                WHERE id = ?
                  AND estado = 'CRIADA'
                """,
                experienciaId
        );

        debug("[EXPERIENCIA UPDATE] experienciaId=" + experienciaId
                + " | linhasAtualizadas=" + updated);
    }

    private void validar(LeituraEntradaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Leitura inválida.");
        }

        if (dto.deviceId() == null || dto.deviceId().isBlank()) {
            throw new IllegalArgumentException("Device ID inválido.");
        }

        if (dto.tipoSensor() == null || dto.tipoSensor().isBlank()) {
            throw new IllegalArgumentException("Tipo de sensor inválido.");
        }

        if (dto.valor() == null) {
            throw new IllegalArgumentException("Valor inválido: valor nulo.");
        }

        if (dto.valor().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor inválido: valor negativo recebido. valor=" + dto.valor());
        }
    }

    private String normalizarTipo(String tipoSensor) {
        if (tipoSensor == null) {
            return "";
        }

        String tipo = tipoSensor.trim().toUpperCase();

        return switch (tipo) {
            case "TEMP", "TEMPERATURE", "TEMPERATURA" -> "TEMPERATURA";
            case "TDS", "TDS_SENSOR" -> "TDS";
            case "PH", "PHSENSOR", "PH_SENSOR", "P_H" -> "PH";
            default -> tipo;
        };
    }

    private String unidadePorTipo(String tipoSensor) {
        String tipo = normalizarTipo(tipoSensor);

        return switch (tipo) {
            case "TEMPERATURA" -> "ºC";
            case "TDS" -> "ppm";
            case "PH" -> "pH";
            default -> "";
        };
    }

    private void debugDiagnosticoSensor(String deviceId, String tipoSensor) {
        try {
            System.err.println("[DEBUG SENSOR FALHOU] deviceId=" + deviceId + " | tipoSensor=" + tipoSensor);

            jdbcTemplate.query(
                    """
                    SELECT
                        e.id AS estacao_id,
                        e.nome AS estacao_nome,
                        e.device_id,
                        e.ativa AS estacao_ativa,
                        s.id AS sensor_id,
                        s.nome AS sensor_nome,
                        s.tipo,
                        s.ativo AS sensor_ativo,
                        s.remoto_ativo
                    FROM estacoes e
                    LEFT JOIN sensores s ON s.estacao_id = e.id
                    WHERE e.device_id = ?
                    ORDER BY s.tipo
                    """,
                    rs -> {
                        System.err.println("[DEBUG SENSOR BD] estacaoId=" + rs.getLong("estacao_id")
                                + " | estacao=" + rs.getString("estacao_nome")
                                + " | deviceId=" + rs.getString("device_id")
                                + " | estacaoAtiva=" + rs.getBoolean("estacao_ativa")
                                + " | sensorId=" + rs.getLong("sensor_id")
                                + " | sensor=" + rs.getString("sensor_nome")
                                + " | tipo=" + rs.getString("tipo")
                                + " | sensorAtivo=" + rs.getBoolean("sensor_ativo")
                                + " | remotoAtivo=" + rs.getBoolean("remoto_ativo"));
                    },
                    deviceId
            );

        } catch (Exception e) {
            System.err.println("[DEBUG SENSOR FALHOU TAMBEM] " + e.getMessage());
        }
    }

    private void debugDiagnosticoExperiencia(String deviceId, String tipoSensor) {
        try {
            System.err.println("[DEBUG EXPERIENCIA FALHOU] deviceId=" + deviceId + " | tipoSensor=" + tipoSensor);

            jdbcTemplate.query(
                    """
                    SELECT
                        exp.id AS experiencia_id,
                        exp.nome AS experiencia_nome,
                        exp.estado,
                        exp.data_inicio,
                        e.id AS estacao_id,
                        e.nome AS estacao_nome,
                        e.device_id,
                        e.ativa AS estacao_ativa,
                        s.id AS sensor_id,
                        s.nome AS sensor_nome,
                        s.tipo,
                        s.ativo AS sensor_ativo
                    FROM experiencias exp
                    INNER JOIN experiencia_estacoes ee ON ee.experiencia_id = exp.id
                    INNER JOIN estacoes e ON e.id = ee.estacao_id
                    INNER JOIN sensores s ON s.estacao_id = e.id
                    WHERE e.device_id = ?
                    ORDER BY exp.data_inicio DESC, exp.id DESC, s.tipo
                    """,
                    rs -> {
                        System.err.println("[DEBUG EXPERIENCIA BD] experienciaId=" + rs.getLong("experiencia_id")
                                + " | experiencia=" + rs.getString("experiencia_nome")
                                + " | estado=" + rs.getString("estado")
                                + " | dataInicio=" + rs.getTimestamp("data_inicio")
                                + " | estacaoId=" + rs.getLong("estacao_id")
                                + " | estacao=" + rs.getString("estacao_nome")
                                + " | deviceId=" + rs.getString("device_id")
                                + " | estacaoAtiva=" + rs.getBoolean("estacao_ativa")
                                + " | sensorId=" + rs.getLong("sensor_id")
                                + " | sensor=" + rs.getString("sensor_nome")
                                + " | tipo=" + rs.getString("tipo")
                                + " | sensorAtivo=" + rs.getBoolean("sensor_ativo"));
                    },
                    deviceId
            );

        } catch (Exception e) {
            System.err.println("[DEBUG EXPERIENCIA FALHOU TAMBEM] " + e.getMessage());
        }
    }

    private void debug(String mensagem) {
        if (DEBUG_LEITURAS) {
            System.out.println(mensagem);
        }
    }
}
