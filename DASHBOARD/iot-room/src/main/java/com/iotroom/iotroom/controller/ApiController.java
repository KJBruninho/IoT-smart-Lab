package com.iotroom.iotroom.controller;

import com.iotroom.iotroom.dto.GraficoLeituraDTO;
import com.iotroom.iotroom.model.LeituraSensor;
import com.iotroom.iotroom.repository.LeituraSensorRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final LeituraSensorRepository leituraSensorRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ApiController(LeituraSensorRepository leituraSensorRepository) {
        this.leituraSensorRepository = leituraSensorRepository;
    }

    @GetMapping("/temperatura")
    public List<GraficoLeituraDTO> temperatura() {
        return dadosPorTipo("TEMPERATURA");
    }

    @GetMapping("/tds")
    public List<GraficoLeituraDTO> tds() {
        return dadosPorTipo("TDS");
    }

    private List<GraficoLeituraDTO> dadosPorTipo(String tipo) {
        List<LeituraSensor> leituras = leituraSensorRepository.findTop30BySensorTipoOrderByRegistadoEmDesc(tipo);
        Collections.reverse(leituras);
        return leituras.stream()
                .map(l -> new GraficoLeituraDTO(l.getRegistadoEm().format(formatter), l.getValor()))
                .toList();
    }
}
