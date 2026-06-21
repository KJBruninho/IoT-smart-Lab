package com.iotroom.iotroom.controller;

import com.iotroom.iotroom.dto.SensorModoFormDTO;
import com.iotroom.iotroom.model.Sensor;
import com.iotroom.iotroom.service.ProfessorComandoSensorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/professor/sensores")
public class ProfessorComandoSensorController {

    private final ProfessorComandoSensorService professorComandoSensorService;

    public ProfessorComandoSensorController(ProfessorComandoSensorService professorComandoSensorService) {
        this.professorComandoSensorService = professorComandoSensorService;
    }

    @GetMapping
    public String index(Model model) {
        Long professorId = obterProfessorIdTemporario();

        model.addAttribute("sensores", professorComandoSensorService.listarSensores(professorId));
        model.addAttribute("paginaAtual", "sensores");

        return "professor/sensores/index";
    }

    @GetMapping("/{sensorId}")
    public String ver(@PathVariable Long sensorId, Model model) {
        Long professorId = obterProfessorIdTemporario();

        Sensor sensor = professorComandoSensorService.obterSensor(sensorId, professorId);

        model.addAttribute("sensor", sensor);
        model.addAttribute("configuracao", professorComandoSensorService.obterOuCriarConfiguracao(sensorId, professorId));
        model.addAttribute("modoForm", professorComandoSensorService.criarFormModo(sensorId, professorId));
        model.addAttribute("comandosRecentes", professorComandoSensorService.listarComandosRecentes(sensorId, professorId));
        model.addAttribute("pedidosSensor", professorComandoSensorService.listarPedidosDoSensor(sensorId, professorId));
        model.addAttribute("paginaAtual", "sensores");

        return "professor/sensores/ver";
    }

    @PostMapping("/{sensorId}/configuracao")
    public String guardarConfiguracaoModo(
            @PathVariable Long sensorId,
            @ModelAttribute("modoForm") SensorModoFormDTO modoForm
    ) {
        Long professorId = obterProfessorIdTemporario();

        professorComandoSensorService.guardarConfiguracaoModo(sensorId, professorId, modoForm);

        return "redirect:/professor/sensores/" + sensorId;
    }

    @PostMapping("/{sensorId}/calibracao")
    public String enviarFatorCalibracao(
            @PathVariable Long sensorId,
            @RequestParam BigDecimal fator
    ) {
        Long professorId = obterProfessorIdTemporario();

        professorComandoSensorService.enviarFatorCalibracao(sensorId, professorId, fator);

        return "redirect:/professor/sensores/" + sensorId;
    }

    @PostMapping("/{sensorId}/ph-offset")
    public String enviarOffsetPh(
            @PathVariable Long sensorId,
            @RequestParam BigDecimal offset
    ) {
        Long professorId = obterProfessorIdTemporario();

        professorComandoSensorService.enviarOffsetPh(sensorId, professorId, offset);

        return "redirect:/professor/sensores/" + sensorId;
    }

    @PostMapping("/{sensorId}/ligar")
    public String ligarSensor(@PathVariable Long sensorId) {
        Long professorId = obterProfessorIdTemporario();

        professorComandoSensorService.ligarSensor(sensorId, professorId);

        return "redirect:/professor/sensores/" + sensorId;
    }

    @PostMapping("/{sensorId}/desligar")
    public String desligarSensor(@PathVariable Long sensorId) {
        Long professorId = obterProfessorIdTemporario();

        professorComandoSensorService.desligarSensor(sensorId, professorId);

        return "redirect:/professor/sensores/" + sensorId;
    }

    @GetMapping("/pedidos")
    public String pedidos(Model model) {
        Long professorId = obterProfessorIdTemporario();

        model.addAttribute("pedidos", professorComandoSensorService.listarPedidosPendentes(professorId));
        model.addAttribute("paginaAtual", "sensores");

        return "professor/sensores/pedidos";
    }

    @PostMapping("/pedidos/{pedidoId}/aprovar")
    public String aprovarPedido(
            @PathVariable Long pedidoId,
            @RequestParam(required = false) String respostaProfessor
    ) {
        Long professorId = obterProfessorIdTemporario();

        professorComandoSensorService.aprovarPedido(pedidoId, professorId, respostaProfessor);

        return "redirect:/professor/sensores/pedidos";
    }

    @PostMapping("/pedidos/{pedidoId}/rejeitar")
    public String rejeitarPedido(
            @PathVariable Long pedidoId,
            @RequestParam(required = false) String respostaProfessor
    ) {
        Long professorId = obterProfessorIdTemporario();

        professorComandoSensorService.rejeitarPedido(pedidoId, professorId, respostaProfessor);

        return "redirect:/professor/sensores/pedidos";
    }

    private Long obterProfessorIdTemporario() {
        return 2L;
    }
}