package com.iotroom.iotroom.service;

import com.iotroom.iotroom.model.LeituraSensor;
import com.iotroom.iotroom.model.Sensor;
import com.iotroom.iotroom.repository.LeituraSensorRepository;
import com.iotroom.iotroom.repository.SensorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class LeituraService {
    private final SensorRepository sensorRepository;
    private final LeituraSensorRepository leituraSensorRepository;

    public LeituraService(SensorRepository sensorRepository, LeituraSensorRepository leituraSensorRepository) {
        this.sensorRepository = sensorRepository;
        this.leituraSensorRepository = leituraSensorRepository;
    }

    @Transactional
    public void registarLeitura(String deviceId, String tipoSensor, BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor inválido.");
        }

        Sensor sensor = sensorRepository
                .findByEstacaoDeviceIdAndTipoAndAtivoTrueAndEstacaoAtivaTrue(deviceId, tipoSensor.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Sensor não encontrado ou inativo."));

        LeituraSensor leitura = new LeituraSensor();
        leitura.setSensor(sensor);
        leitura.setValor(valor);
        leitura.setRegistadoEm(LocalDateTime.now());

        leituraSensorRepository.save(leitura);
    }
}
