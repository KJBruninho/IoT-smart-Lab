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
        validar(dto);

        try {
            guardarNaBaseDados(dto);
        } catch (Exception e) {
            localSqliteCacheService.guardar(dto);
            System.err.println("BD indisponível. Leitura guardada em SQLite local: " + e.getMessage());
        }
    }

    public void guardarNaBaseDados(LeituraEntradaDTO dto) {
        validar(dto);

        transactionTemplate.executeWithoutResult(status -> {
            String tipoSensor = normalizarTipo(dto.tipoSensor());

            Sensor sensor = sensorRepository
                    .findByEstacaoDeviceIdAndTipoAndAtivoTrueAndEstacaoAtivaTrue(
                            dto.deviceId(),
                            tipoSensor
                    )
                    .orElseThrow(() -> new IllegalArgumentException("Sensor não encontrado ou inativo."));

            Long experienciaId = obterExperienciaAtivaPorDeviceESensor(
                    dto.deviceId(),
                    tipoSensor
            );

            LeituraSensor leitura = new LeituraSensor();
            leitura.setExperienciaId(experienciaId);
            leitura.setSensor(sensor);
            leitura.setUnidade(dto.unidade());
            leitura.setValor(dto.valor());
            leitura.setRegistadoEm(dto.dataRegisto() != null ? dto.dataRegisto() : LocalDateTime.now());

            LeituraSensor leituraGuardada = leituraSensorRepository.saveAndFlush(leitura);

            marcarExperienciaEmExecucao(experienciaId);

            professorAlertaService.processarLeitura(leituraGuardada.getId());
        });
    }

    @Scheduled(fixedDelayString = "${cache.flush.delay-ms:10000}")
    public void sincronizarCacheLocal() {
        for (LeituraCacheDTO pendente : localSqliteCacheService.listarPendentes(100)) {
            try {
                guardarNaBaseDados(pendente.leitura());
                localSqliteCacheService.marcarComoSincronizada(pendente.id());
            } catch (Exception e) {
                System.err.println("Cache ainda não sincronizada: " + e.getMessage());
                break;
            }
        }
    }

    private Long obterExperienciaAtivaPorDeviceESensor(String deviceId, String tipoSensor) {
        try {
            return jdbcTemplate.queryForObject(
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
                      AND exp.estado IN ('CRIADA', 'EM_EXECUCAO')
                    ORDER BY exp.data_inicio DESC
                    LIMIT 1
                    """,
                    Long.class,
                    deviceId,
                    tipoSensor
            );
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException(
                    "Não existe experiência ativa para esta estação/sensor."
            );
        }
    }

    private void marcarExperienciaEmExecucao(Long experienciaId) {
        jdbcTemplate.update(
                """
                UPDATE experiencias
                SET estado = 'EM_EXECUCAO'
                WHERE id = ?
                  AND estado = 'CRIADA'
                """,
                experienciaId
        );
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

        if (dto.valor() == null || dto.valor().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor inválido.");
        }
    }

    private String normalizarTipo(String tipoSensor) {
        return tipoSensor.trim().toUpperCase();
    }

    private String unidadePorTipo(String tipoSensor) {
        String tipo = normalizarTipo(tipoSensor);

        return switch (tipo) {
            case "TEMPERATURA" -> "ºC";
            case "TDS" -> "ppm";
            default -> "";
        };
    }
}