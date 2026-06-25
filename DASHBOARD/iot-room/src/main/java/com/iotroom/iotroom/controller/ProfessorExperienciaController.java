package com.iotroom.iotroom.controller;

import com.iotroom.iotroom.dto.ExperienciaEstacaoFormDTO;
import com.iotroom.iotroom.dto.ExperienciaFormDTO;
import com.iotroom.iotroom.model.Experiencia;
import com.iotroom.iotroom.security.AuthenticatedUser;
import com.iotroom.iotroom.service.ProfessorExperienciaService;
import org.springframework.security.core.Authentication;
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
    public String index(Model model, Authentication authentication) {
        Long professorId = obterUtilizadorId(authentication);

        List<Experiencia> experiencias = professorExperienciaService.listarExperienciasDoProfessor(professorId);

        model.addAttribute("experiencias", experiencias);
        model.addAttribute("nomesGrupos", professorExperienciaService.obterNomesDosGrupos(professorId));
        model.addAttribute("paginaAtual", "experiencias");

        return "professor/experiencias/index";
    }

    @GetMapping("/nova")
    public String nova(Model model, Authentication authentication) {
        Long professorId = obterUtilizadorId(authentication);

        model.addAttribute("experienciaForm", professorExperienciaService.criarFormVazio());
        model.addAttribute("grupos", professorExperienciaService.listarGruposDisponiveis(professorId));
        model.addAttribute("paginaAtual", "experiencias");

        return "professor/experiencias/nova";
    }

    @PostMapping("/nova")
    public String criar(
            @ModelAttribute("experienciaForm") ExperienciaFormDTO experienciaForm,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        Experiencia experienciaCriada =
                professorExperienciaService.criarExperiencia(professorId, experienciaForm);

        return "redirect:/professor/experiencias/" + experienciaCriada.getId();
    }

    @GetMapping("/{id}")
    public String ver(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        Experiencia experiencia = professorExperienciaService.obterExperienciaDoProfessor(id, professorId);

        model.addAttribute("experiencia", experiencia);
        model.addAttribute("nomesGrupos", professorExperienciaService.obterNomesDosGrupos(professorId));
        model.addAttribute("paginaAtual", "experiencias");
        model.addAttribute("estacoesAssociadas", professorExperienciaService.listarEstacoesDaExperiencia(id, professorId));
        model.addAttribute("estacoesDisponiveis", professorExperienciaService.listarEstacoesDisponiveisParaExperiencia(id, professorId));
        model.addAttribute("estacoesMap", professorExperienciaService.obterMapaEstacoes(id, professorId));
        model.addAttribute("estacaoForm", new ExperienciaEstacaoFormDTO());

        return "professor/experiencias/ver";
    }

    @PostMapping("/{id}/estacoes/adicionar")
    public String adicionarEstacao(
            @PathVariable Long id,
            @ModelAttribute("estacaoForm") ExperienciaEstacaoFormDTO estacaoForm,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorExperienciaService.associarEstacao(id, professorId, estacaoForm);

        return "redirect:/professor/experiencias/" + id;
    }

    @PostMapping("/{id}/estacoes/{estacaoId}/remover")
    public String removerEstacao(
            @PathVariable Long id,
            @PathVariable Long estacaoId,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorExperienciaService.removerEstacao(id, estacaoId, professorId);

        return "redirect:/professor/experiencias/" + id;
    }

    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        Experiencia experiencia = professorExperienciaService.obterExperienciaDoProfessor(id, professorId);

        model.addAttribute("experienciaForm", professorExperienciaService.criarFormAPartirDeExperiencia(experiencia));
        model.addAttribute("grupos", professorExperienciaService.listarGruposDisponiveis(professorId));
        model.addAttribute("paginaAtual", "experiencias");

        return "professor/experiencias/editar";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(
            @PathVariable Long id,
            @ModelAttribute("experienciaForm") ExperienciaFormDTO experienciaForm,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorExperienciaService.atualizarExperiencia(id, professorId, experienciaForm);

        return "redirect:/professor/experiencias/" + id;
    }

    @PostMapping("/{id}/iniciar")
    public String iniciar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorExperienciaService.iniciarExperiencia(id, professorId);

        return "redirect:/professor/experiencias/" + id;
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorExperienciaService.finalizarExperiencia(id, professorId);

        return "redirect:/professor/experiencias/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorExperienciaService.cancelarExperiencia(id, professorId);

        return "redirect:/professor/experiencias/" + id;
    }

    private Long obterUtilizadorId(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        return user.getId();
    }
}