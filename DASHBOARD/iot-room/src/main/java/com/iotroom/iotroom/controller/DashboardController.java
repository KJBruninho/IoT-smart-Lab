package com.iotroom.iotroom.controller;

import com.iotroom.iotroom.repository.EstacaoRepository;
import com.iotroom.iotroom.repository.LeituraSensorRepository;
import com.iotroom.iotroom.repository.SensorRepository;
import com.iotroom.iotroom.security.AuthenticatedUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final EstacaoRepository estacaoRepository;
    private final SensorRepository sensorRepository;
    private final LeituraSensorRepository leituraSensorRepository;

    public DashboardController(
            EstacaoRepository estacaoRepository,
            SensorRepository sensorRepository,
            LeituraSensorRepository leituraSensorRepository
    ) {
        this.estacaoRepository = estacaoRepository;
        this.sensorRepository = sensorRepository;
        this.leituraSensorRepository = leituraSensorRepository;
    }

    @GetMapping("/")
    public String home(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/admin";
        }

        return "redirect:/professor";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalEstacoes", estacaoRepository.count());
        model.addAttribute("totalSensores", sensorRepository.count());
        model.addAttribute("totalLeituras", leituraSensorRepository.count());

        model.addAttribute(
                "leiturasRecentes",
                leituraSensorRepository.findRecentesComSensorEEstacao(PageRequest.of(0, 10))
        );

        return "dashboard";
    }

    @GetMapping("/estacoes")
    public String estacoes(Model model) {
        model.addAttribute("estacoes", estacaoRepository.findAll());
        return "estacoes";
    }

    @GetMapping("/sensores")
    public String sensores(Model model) {
        model.addAttribute("sensores", sensorRepository.findAllComEstacao());
        return "sensores";
    }

    @GetMapping("/leituras")
    public String leituras(Model model) {
        model.addAttribute(
                "leituras",
                leituraSensorRepository.findRecentesComSensorEEstacao(PageRequest.of(0, 30))
        );

        return "leituras";
    }
}