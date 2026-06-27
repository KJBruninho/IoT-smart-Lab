package com.iotroom.iotroom.controller.api;

import com.iotroom.iotroom.dto.dashboard.DashboardEstadoDTO;
import com.iotroom.iotroom.dto.leitura.GraficoLeituraDTO;
import com.iotroom.iotroom.dto.leitura.UltimaLeituraDTO;
import com.iotroom.iotroom.model.LeituraSensor;
import com.iotroom.iotroom.repository.leitura.LeituraSensorRepository;
import com.iotroom.iotroom.service.leitura.LocalSqliteCacheService;
import com.iotroom.iotroom.service.mqtt.MqttStatusService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final LeituraSensorRepository leituraSensorRepository;
    private final MqttStatusService mqttStatusService;
    private final LocalSqliteCacheService localSqliteCacheService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ApiController(
            LeituraSensorRepository leituraSensorRepository,
            MqttStatusService mqttStatusService,
            LocalSqliteCacheService localSqliteCacheService
    ) {
        this.leituraSensorRepository = leituraSensorRepository;
        this.mqttStatusService = mqttStatusService;
        this.localSqliteCacheService = localSqliteCacheService;
    }

    @GetMapping("/temperatura")
    public List<GraficoLeituraDTO> temperatura() {
        return dadosPorTipo("TEMPERATURA");
    }

    @GetMapping("/tds")
    public List<GraficoLeituraDTO> tds() {
        return dadosPorTipo("TDS");
    }

    @GetMapping("/dashboard/estado")
    public DashboardEstadoDTO estadoDashboard() {
        List<UltimaLeituraDTO> ultimas = new ArrayList<>();
        String fonte = "mysql";

        try {
            leituraSensorRepository.findTopBySensorTipoOrderByRegistadoEmDesc("TEMPERATURA")
                    .ifPresent(l -> ultimas.add(toUltimaLeituraDTO(l)));

            leituraSensorRepository.findTopBySensorTipoOrderByRegistadoEmDesc("TDS")
                    .ifPresent(l -> ultimas.add(toUltimaLeituraDTO(l)));
        } catch (Exception e) {
            fonte = "sqlite-local";

            localSqliteCacheService.ultimaPorTipo("TEMPERATURA")
                    .ifPresent(ultimas::add);

            localSqliteCacheService.ultimaPorTipo("TDS")
                    .ifPresent(ultimas::add);
        }

        return new DashboardEstadoDTO(
                mqttStatusService.isOnline(),
                fonte,
                localSqliteCacheService.contarPendentes(),
                ultimas
        );
    }

    private List<GraficoLeituraDTO> dadosPorTipo(String tipo) {
        try {
            List<LeituraSensor> leituras = leituraSensorRepository.findTop30BySensorTipoOrderByRegistadoEmDesc(tipo);
            Collections.reverse(leituras);

            return leituras.stream()
                    .map(l -> new GraficoLeituraDTO(l.getRegistadoEm().format(formatter), l.getValor()))
                    .toList();
        } catch (Exception e) {
            return localSqliteCacheService.graficoPorTipo(tipo, 30);
        }
    }

    private UltimaLeituraDTO toUltimaLeituraDTO(LeituraSensor leitura) {
        return new UltimaLeituraDTO(
                leitura.getSensor().getTipo(),
                leitura.getValor(),
                leitura.getSensor().getUnidade(),
                leitura.getRegistadoEm()
        );
    }
}
