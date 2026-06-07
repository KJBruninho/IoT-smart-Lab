package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.LeituraCacheDTO;
import com.iotroom.iotroom.dto.LeituraEntradaDTO;
import com.iotroom.iotroom.model.LeituraSensor;
import com.iotroom.iotroom.model.Sensor;
import com.iotroom.iotroom.repository.LeituraSensorRepository;
import com.iotroom.iotroom.repository.SensorRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class LeituraService {
    private final SensorRepository sensorRepository;
    private final LeituraSensorRepository leituraSensorRepository;
    private final LocalSqliteCacheService localSqliteCacheService;
    private final TransactionTemplate transactionTemplate;

    public LeituraService(
            SensorRepository sensorRepository,
            LeituraSensorRepository leituraSensorRepository,
            LocalSqliteCacheService localSqliteCacheService,
            TransactionTemplate transactionTemplate
    ) {
        this.sensorRepository = sensorRepository;
        this.leituraSensorRepository = leituraSensorRepository;
        this.localSqliteCacheService = localSqliteCacheService;
        this.transactionTemplate = transactionTemplate;
    }

    public void registarLeitura(String deviceId, String tipoSensor, BigDecimal valor) {
        String unidade = unidadePorTipo(tipoSensor);

        LeituraEntradaDTO dto = new LeituraEntradaDTO(
                deviceId,
                normalizarTipo(tipoSensor),
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
            Sensor sensor = sensorRepository
                    .findByEstacaoDeviceIdAndTipoAndAtivoTrueAndEstacaoAtivaTrue(
                            dto.deviceId(),
                            normalizarTipo(dto.tipoSensor())
                    )
                    .orElseThrow(() -> new IllegalArgumentException("Sensor não encontrado ou inativo."));

            LeituraSensor leitura = new LeituraSensor();
            leitura.setSensor(sensor);
            leitura.setValor(dto.valor());
            leitura.setRegistadoEm(dto.dataRegisto() != null ? dto.dataRegisto() : LocalDateTime.now());

            leituraSensorRepository.save(leitura);
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
