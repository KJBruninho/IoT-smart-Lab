package com.iotroom.iotroom.controller;

import com.iotroom.iotroom.dto.ProfessorDashboardResumoDTO;
import com.iotroom.iotroom.service.ProfessorDashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfessorController {

    private final ProfessorDashboardService professorDashboardService;

    public ProfessorController(ProfessorDashboardService professorDashboardService) {
        this.professorDashboardService = professorDashboardService;
    }

    @GetMapping("/professor")
    public String dashboardProfessor(Model model) {
        Long professorId = 2L;

        ProfessorDashboardResumoDTO dashboard =
                professorDashboardService.obterDashboard(professorId);

        model.addAttribute("dashboard", dashboard);
        model.addAttribute("paginaAtual", "dashboard");

        return "professor/index";
    }
}