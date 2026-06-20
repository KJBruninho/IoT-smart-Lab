package com.iotroom.iotroom.controller;

import com.iotroom.iotroom.dto.ExperienciaEstacaoFormDTO;
import com.iotroom.iotroom.dto.ExperienciaFormDTO;
import com.iotroom.iotroom.model.Experiencia;
import com.iotroom.iotroom.service.ProfessorExperienciaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/professor/experiencias")
public class ProfessorExperienciaController {

    private final ProfessorExperienciaService professorExperienciaService;

    public ProfessorExperienciaController(ProfessorExperienciaService professorExperienciaService) {
        this.professorExperienciaService = professorExperienciaService;
    }

    @GetMapping
    public String index(Model model) {
        Long professorId = obterProfessorIdTemporario();

        List<Experiencia> experiencias = professorExperienciaService.listarExperienciasDoProfessor(professorId);

        model.addAttribute("experiencias", experiencias);
        model.addAttribute("nomesGrupos", professorExperienciaService.obterNomesDosGrupos(professorId));
        model.addAttribute("paginaAtual", "experiencias");

        return "professor/experiencias/index";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        Long professorId = obterProfessorIdTemporario();

        model.addAttribute("experienciaForm", professorExperienciaService.criarFormVazio());
        model.addAttribute("grupos", professorExperienciaService.listarGruposDisponiveis(professorId));
        model.addAttribute("paginaAtual", "experiencias");

        return "professor/experiencias/nova";
    }

    @PostMapping("/nova")
    public String criar(@ModelAttribute("experienciaForm") ExperienciaFormDTO experienciaForm) {
        Long professorId = obterProfessorIdTemporario();

        Experiencia experienciaCriada = professorExperienciaService.criarExperiencia(
                professorId,
                experienciaForm
        );

        return "redirect:/professor/experiencias/" + experienciaCriada.getId();
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model) {
        Long professorId = obterProfessorIdTemporario();

        Experiencia experiencia = professorExperienciaService.obterExperienciaDoProfessor(id, professorId);

        model.addAttribute("experiencia", experiencia);
        model.addAttribute("nomesGrupos", professorExperienciaService.obterNomesDosGrupos(professorId));
        model.addAttribute("paginaAtual", "experiencias");
        model.addAttribute(
                "estacoesAssociadas",
                professorExperienciaService.listarEstacoesDaExperiencia(id, professorId)
        );

        model.addAttribute(
                "estacoesDisponiveis",
                professorExperienciaService.listarEstacoesDisponiveisParaExperiencia(id, professorId)
        );

        model.addAttribute(
                "estacoesMap",
                professorExperienciaService.obterMapaEstacoes(id, professorId)
        );

        model.addAttribute("estacaoForm", new ExperienciaEstacaoFormDTO());

        return "professor/experiencias/ver";
    }
    
    @PostMapping("/{id}/estacoes/adicionar")
    public String adicionarEstacao(
            @PathVariable Long id,
            @ModelAttribute("estacaoForm") ExperienciaEstacaoFormDTO estacaoForm
    ) {
        Long professorId = obterProfessorIdTemporario();

        professorExperienciaService.associarEstacao(id, professorId, estacaoForm);

        return "redirect:/professor/experiencias/" + id;
    }

    @PostMapping("/{id}/estacoes/{estacaoId}/remover")
    public String removerEstacao(
            @PathVariable Long id,
            @PathVariable Long estacaoId
    ) {
        Long professorId = obterProfessorIdTemporario();

        professorExperienciaService.removerEstacao(id, estacaoId, professorId);

        return "redirect:/professor/experiencias/" + id;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Long professorId = obterProfessorIdTemporario();

        Experiencia experiencia = professorExperienciaService.obterExperienciaDoProfessor(id, professorId);

        model.addAttribute("experienciaForm", professorExperienciaService.criarFormAPartirDeExperiencia(experiencia));
        model.addAttribute("grupos", professorExperienciaService.listarGruposDisponiveis(professorId));
        model.addAttribute("paginaAtual", "experiencias");

        return "professor/experiencias/editar";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(
            @PathVariable Long id,
            @ModelAttribute("experienciaForm") ExperienciaFormDTO experienciaForm
    ) {
        Long professorId = obterProfessorIdTemporario();

        professorExperienciaService.atualizarExperiencia(id, professorId, experienciaForm);

        return "redirect:/professor/experiencias/" + id;
    }

    @PostMapping("/{id}/iniciar")
    public String iniciar(@PathVariable Long id) {
        Long professorId = obterProfessorIdTemporario();

        professorExperienciaService.iniciarExperiencia(id, professorId);

        return "redirect:/professor/experiencias/" + id;
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable Long id) {
        Long professorId = obterProfessorIdTemporario();

        professorExperienciaService.finalizarExperiencia(id, professorId);

        return "redirect:/professor/experiencias/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id) {
        Long professorId = obterProfessorIdTemporario();

        professorExperienciaService.cancelarExperiencia(id, professorId);

        return "redirect:/professor/experiencias/" + id;
    }

    private Long obterProfessorIdTemporario() {
        return 1L;
    }
}