package com.iotroom.iotroom.controller;

import com.iotroom.iotroom.dto.GrupoFormDTO;
import com.iotroom.iotroom.dto.GrupoMembroFormDTO;
import com.iotroom.iotroom.model.Grupo;
import com.iotroom.iotroom.security.AuthenticatedUser;
import com.iotroom.iotroom.service.PermissaoGrupoEstacaoService;
import com.iotroom.iotroom.service.ProfessorGrupoMembroService;
import com.iotroom.iotroom.service.ProfessorGrupoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/professor/grupos")
public class ProfessorGrupoController {

    private final ProfessorGrupoMembroService professorGrupoMembroService;
    private final ProfessorGrupoService professorGrupoService;
    private final PermissaoGrupoEstacaoService permissaoGrupoEstacaoService;

    public ProfessorGrupoController(
            ProfessorGrupoService professorGrupoService,
            ProfessorGrupoMembroService professorGrupoMembroService,
            PermissaoGrupoEstacaoService permissaoGrupoEstacaoService
    ) {
        this.professorGrupoMembroService = professorGrupoMembroService;
        this.professorGrupoService = professorGrupoService;
        this.permissaoGrupoEstacaoService = permissaoGrupoEstacaoService;
    }

    @GetMapping
    public String index(Model model, Authentication authentication) {
        Long professorId = obterUtilizadorId(authentication);

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
    public String criar(
            @ModelAttribute("grupoForm") GrupoFormDTO grupoForm,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        Grupo grupoCriado = professorGrupoService.criarGrupo(professorId, grupoForm);

        return "redirect:/professor/grupos/" + grupoCriado.getId();
    }

    @GetMapping("/{id}")
    public String ver(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

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
    public String editar(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        Grupo grupo = professorGrupoService.obterGrupoDoProfessor(id, professorId);

        model.addAttribute("grupoForm", professorGrupoService.criarFormAPartirDeGrupo(grupo));
        model.addAttribute("paginaAtual", "grupos");

        return "professor/grupos/editar";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(
            @PathVariable Long id,
            @ModelAttribute("grupoForm") GrupoFormDTO grupoForm,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorGrupoService.atualizarGrupo(id, professorId, grupoForm);

        return "redirect:/professor/grupos/" + id;
    }

    @PostMapping("/{id}/alternar-estado")
    public String alternarEstado(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorGrupoService.alternarEstado(id, professorId);

        return "redirect:/professor/grupos";
    }

    @GetMapping("/{id}/estacoes")
    public String gerirEstacoesDoGrupo(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        Grupo grupo = professorGrupoService.obterGrupoDoProfessor(id, professorId);

        model.addAttribute("grupo", grupo);
        model.addAttribute("estacoes", permissaoGrupoEstacaoService.listarEstacoesAtivas());
        model.addAttribute("estacoesComAcesso", permissaoGrupoEstacaoService.listarIdsEstacoesComAcesso(id));
        model.addAttribute("paginaAtual", "grupos");

        return "professor/grupos/estacoes";
    }

    @PostMapping("/{id}/estacoes")
    public String guardarEstacoesDoGrupo(
            @PathVariable Long id,
            @RequestParam(required = false) List<Long> estacaoIds,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorGrupoService.obterGrupoDoProfessor(id, professorId);

        permissaoGrupoEstacaoService.atualizarEstacoesDoGrupo(id, estacaoIds);

        return "redirect:/professor/grupos/" + id + "/estacoes";
    }

    @PostMapping("/{id}/membros/adicionar")
    public String adicionarMembro(
            @PathVariable Long id,
            @ModelAttribute("membroForm") GrupoMembroFormDTO membroForm,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorGrupoMembroService.adicionarMembro(id, professorId, membroForm);

        return "redirect:/professor/grupos/" + id;
    }

    @PostMapping("/{id}/membros/{utilizadorId}/alterar-role")
    public String alterarRoleMembro(
            @PathVariable Long id,
            @PathVariable Long utilizadorId,
            @RequestParam Long roleGrupoId,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorGrupoMembroService.alterarRole(id, utilizadorId, professorId, roleGrupoId);

        return "redirect:/professor/grupos/" + id;
    }

    @PostMapping("/{id}/membros/{utilizadorId}/remover")
    public String removerMembro(
            @PathVariable Long id,
            @PathVariable Long utilizadorId,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorGrupoMembroService.removerMembro(id, utilizadorId, professorId);

        return "redirect:/professor/grupos/" + id;
    }

    private Long obterUtilizadorId(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        return user.getId();
    }
}