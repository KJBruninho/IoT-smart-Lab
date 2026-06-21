package com.iotroom.iotroom.controller;

import com.iotroom.iotroom.dto.AvisoFormDTO;
import com.iotroom.iotroom.model.Aviso;
import com.iotroom.iotroom.security.AuthenticatedUser;
import com.iotroom.iotroom.service.ProfessorAvisoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/professor/avisos")
public class ProfessorAvisoController {

    private final ProfessorAvisoService professorAvisoService;

    public ProfessorAvisoController(ProfessorAvisoService professorAvisoService) {
        this.professorAvisoService = professorAvisoService;
    }

    @GetMapping
    public String index(Model model, Authentication authentication) {
        Long professorId = obterUtilizadorId(authentication);

        List<Aviso> avisos = professorAvisoService.listarAvisosDoProfessor(professorId);

        model.addAttribute("avisos", avisos);
        model.addAttribute("paginaAtual", "avisos");

        return "professor/avisos/index";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("avisoForm", professorAvisoService.criarFormVazio());
        model.addAttribute("paginaAtual", "avisos");

        return "professor/avisos/novo";
    }

    @PostMapping("/novo")
    public String criar(
            @ModelAttribute("avisoForm") AvisoFormDTO avisoForm,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        Aviso avisoCriado = professorAvisoService.criarAviso(professorId, avisoForm);

        return "redirect:/professor/avisos/" + avisoCriado.getId();
    }

    @GetMapping("/{id}")
    public String ver(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        Aviso aviso = professorAvisoService.obterAvisoDoProfessor(id, professorId);

        model.addAttribute("aviso", aviso);
        model.addAttribute("paginaAtual", "avisos");

        return "professor/avisos/ver";
    }

    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        Aviso aviso = professorAvisoService.obterAvisoDoProfessor(id, professorId);

        model.addAttribute("avisoForm", professorAvisoService.criarFormAPartirDeAviso(aviso));
        model.addAttribute("paginaAtual", "avisos");

        return "professor/avisos/editar";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(
            @PathVariable Long id,
            @ModelAttribute("avisoForm") AvisoFormDTO avisoForm,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorAvisoService.atualizarAviso(id, professorId, avisoForm);

        return "redirect:/professor/avisos/" + id;
    }

    @PostMapping("/{id}/alternar-estado")
    public String alternarEstado(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorAvisoService.alternarEstado(id, professorId);

        return "redirect:/professor/avisos";
    }

    private Long obterUtilizadorId(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        return user.getId();
    }
}