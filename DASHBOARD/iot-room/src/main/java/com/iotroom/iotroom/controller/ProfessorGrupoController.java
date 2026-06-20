package com.iotroom.iotroom.controller;

import com.iotroom.iotroom.dto.GrupoMembroFormDTO;
import com.iotroom.iotroom.dto.GrupoFormDTO;
import com.iotroom.iotroom.model.Grupo;
import com.iotroom.iotroom.service.ProfessorGrupoMembroService;
import com.iotroom.iotroom.service.ProfessorGrupoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/professor/grupos")
public class ProfessorGrupoController {

	private final ProfessorGrupoMembroService professorGrupoMembroService;
    private final ProfessorGrupoService professorGrupoService;

    public ProfessorGrupoController(ProfessorGrupoService professorGrupoService, ProfessorGrupoMembroService professorGrupoMembroService) {
        this.professorGrupoMembroService = professorGrupoMembroService;
		this.professorGrupoService = professorGrupoService;
    }

    @GetMapping
    public String index(Model model) {
        Long professorId = obterProfessorIdTemporario();

        List<Grupo> grupos = professorGrupoService.listarGruposDoProfessor(professorId);

        model.addAttribute("grupos", grupos);
        model.addAttribute("paginaAtual", "grupos");

        return "professor/grupos/index";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("grupoForm", professorGrupoService.criarFormVazio());
        model.addAttribute("paginaAtual", "grupos");

        return "professor/grupos/novo";
    }

    @PostMapping("/novo")
    public String criar(@ModelAttribute("grupoForm") GrupoFormDTO grupoForm) {
        Long professorId = obterProfessorIdTemporario();

        Grupo grupoCriado = professorGrupoService.criarGrupo(professorId, grupoForm);

        return "redirect:/professor/grupos/" + grupoCriado.getId();
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model) {
        Long professorId = obterProfessorIdTemporario();

        Grupo grupo = professorGrupoService.obterGrupoDoProfessor(id, professorId);

        model.addAttribute("grupo", grupo);
        model.addAttribute("paginaAtual", "grupos");
        model.addAttribute("membros", professorGrupoMembroService.listarMembros(id, professorId));
        model.addAttribute("utilizadoresDisponiveis", professorGrupoMembroService.listarUtilizadoresDisponiveis(id, professorId));
        model.addAttribute("rolesGrupo", professorGrupoMembroService.listarRoles());
        model.addAttribute("membroForm", professorGrupoMembroService.criarFormVazio());

        return "professor/grupos/ver";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Long professorId = obterProfessorIdTemporario();

        Grupo grupo = professorGrupoService.obterGrupoDoProfessor(id, professorId);

        model.addAttribute("grupoForm", professorGrupoService.criarFormAPartirDeGrupo(grupo));
        model.addAttribute("paginaAtual", "grupos");

        return "professor/grupos/editar";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(
            @PathVariable Long id,
            @ModelAttribute("grupoForm") GrupoFormDTO grupoForm
    ) {
        Long professorId = obterProfessorIdTemporario();

        professorGrupoService.atualizarGrupo(id, professorId, grupoForm);

        return "redirect:/professor/grupos/" + id;
    }

    @PostMapping("/{id}/alternar-estado")
    public String alternarEstado(@PathVariable Long id) {
        Long professorId = obterProfessorIdTemporario();

        professorGrupoService.alternarEstado(id, professorId);

        return "redirect:/professor/grupos";
    }
    
    @PostMapping("/{id}/membros/adicionar")
    public String adicionarMembro(
            @PathVariable Long id,
            @ModelAttribute("membroForm") GrupoMembroFormDTO membroForm
    ) {
        Long professorId = obterProfessorIdTemporario();

        professorGrupoMembroService.adicionarMembro(id, professorId, membroForm);

        return "redirect:/professor/grupos/" + id;
    }

    @PostMapping("/{id}/membros/{utilizadorId}/alterar-role")
    public String alterarRoleMembro(
            @PathVariable Long id,
            @PathVariable Long utilizadorId,
            @RequestParam Long roleGrupoId
    ) {
        Long professorId = obterProfessorIdTemporario();

        professorGrupoMembroService.alterarRole(id, utilizadorId, professorId, roleGrupoId);

        return "redirect:/professor/grupos/" + id;
    }

    @PostMapping("/{id}/membros/{utilizadorId}/remover")
    public String removerMembro(
            @PathVariable Long id,
            @PathVariable Long utilizadorId
    ) {
        Long professorId = obterProfessorIdTemporario();

        professorGrupoMembroService.removerMembro(id, utilizadorId, professorId);

        return "redirect:/professor/grupos/" + id;
    }

    private Long obterProfessorIdTemporario() {
        /*
         * Temporário.
         * Depois substituímos pelo utilizador autenticado.
         */
        return 1L;
    }
}