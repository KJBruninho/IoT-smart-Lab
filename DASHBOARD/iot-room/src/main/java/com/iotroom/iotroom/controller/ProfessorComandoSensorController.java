package com.iotroom.iotroom.controller;

import com.iotroom.iotroom.dto.SensorModoFormDTO;
import com.iotroom.iotroom.model.Sensor;
import com.iotroom.iotroom.security.AuthenticatedUser;
import com.iotroom.iotroom.service.ProfessorComandoSensorService;
import org.springframework.security.core.Authentication;
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
    public String index(Model model, Authentication authentication) {
        Long professorId = obterUtilizadorId(authentication);

        model.addAttribute("sensores", professorComandoSensorService.listarSensores(professorId));
        model.addAttribute("paginaAtual", "sensores");

        return "professor/sensores/index";
    }

    @GetMapping("/pedidos")
    public String pedidos(Model model, Authentication authentication) {
        Long professorId = obterUtilizadorId(authentication);

        model.addAttribute("pedidos", professorComandoSensorService.listarPedidosPendentes(professorId));
        model.addAttribute("paginaAtual", "sensores");

        return "professor/sensores/pedidos";
    }

    @GetMapping("/{sensorId}")
    public String ver(
            @PathVariable Long sensorId,
            Model model,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

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
            @ModelAttribute("modoForm") SensorModoFormDTO modoForm,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorComandoSensorService.guardarConfiguracaoModo(sensorId, professorId, modoForm);

        return "redirect:/professor/sensores/" + sensorId;
    }

    @PostMapping("/{sensorId}/calibracao")
    public String enviarFatorCalibracao(
            @PathVariable Long sensorId,
            @RequestParam BigDecimal fator,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorComandoSensorService.enviarFatorCalibracao(sensorId, professorId, fator);

        return "redirect:/professor/sensores/" + sensorId;
    }

    @PostMapping("/{sensorId}/ph-offset")
    public String enviarOffsetPh(
            @PathVariable Long sensorId,
            @RequestParam BigDecimal offset,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorComandoSensorService.enviarOffsetPh(sensorId, professorId, offset);

        return "redirect:/professor/sensores/" + sensorId;
    }

    @PostMapping("/{sensorId}/ligar")
    public String ligarSensor(
            @PathVariable Long sensorId,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorComandoSensorService.ligarSensor(sensorId, professorId);

        return "redirect:/professor/sensores/" + sensorId;
    }

    @PostMapping("/{sensorId}/desligar")
    public String desligarSensor(
            @PathVariable Long sensorId,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorComandoSensorService.desligarSensor(sensorId, professorId);

        return "redirect:/professor/sensores/" + sensorId;
    }

    @PostMapping("/pedidos/{pedidoId}/aprovar")
    public String aprovarPedido(
            @PathVariable Long pedidoId,
            @RequestParam(required = false) String respostaProfessor,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorComandoSensorService.aprovarPedido(pedidoId, professorId, respostaProfessor);

        return "redirect:/professor/sensores/pedidos";
    }

    @PostMapping("/pedidos/{pedidoId}/rejeitar")
    public String rejeitarPedido(
            @PathVariable Long pedidoId,
            @RequestParam(required = false) String respostaProfessor,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorComandoSensorService.rejeitarPedido(pedidoId, professorId, respostaProfessor);

        return "redirect:/professor/sensores/pedidos";
    }

    private Long obterUtilizadorId(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        return user.getId();
    }
}