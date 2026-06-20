package com.iotroom.iotroom.controller;

import com.iotroom.iotroom.dto.AvisoFormDTO;
import com.iotroom.iotroom.model.Aviso;
import com.iotroom.iotroom.service.ProfessorAvisoService;
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
    public String index(Model model) {
        Long professorId = obterProfessorIdTemporario();

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
    public String criar(@ModelAttribute("avisoForm") AvisoFormDTO avisoForm) {
        Long professorId = obterProfessorIdTemporario();

        Aviso avisoCriado = professorAvisoService.criarAviso(professorId, avisoForm);

        return "redirect:/professor/avisos/" + avisoCriado.getId();
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model) {
        Long professorId = obterProfessorIdTemporario();

        Aviso aviso = professorAvisoService.obterAvisoDoProfessor(id, professorId);

        model.addAttribute("aviso", aviso);
        model.addAttribute("paginaAtual", "avisos");

        return "professor/avisos/ver";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Long professorId = obterProfessorIdTemporario();

        Aviso aviso = professorAvisoService.obterAvisoDoProfessor(id, professorId);

        model.addAttribute("avisoForm", professorAvisoService.criarFormAPartirDeAviso(aviso));
        model.addAttribute("paginaAtual", "avisos");

        return "professor/avisos/editar";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(
            @PathVariable Long id,
            @ModelAttribute("avisoForm") AvisoFormDTO avisoForm
    ) {
        Long professorId = obterProfessorIdTemporario();

        professorAvisoService.atualizarAviso(id, professorId, avisoForm);

        return "redirect:/professor/avisos/" + id;
    }

    @PostMapping("/{id}/alternar-estado")
    public String alternarEstado(@PathVariable Long id) {
        Long professorId = obterProfessorIdTemporario();

        professorAvisoService.alternarEstado(id, professorId);

        return "redirect:/professor/avisos";
    }

    private Long obterProfessorIdTemporario() {
        return 1L;
    }
}