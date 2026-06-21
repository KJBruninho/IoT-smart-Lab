package com.iotroom.iotroom.controller;

import com.iotroom.iotroom.dto.ForumRespostaFormDTO;
import com.iotroom.iotroom.dto.ForumTopicoFormDTO;
import com.iotroom.iotroom.model.ForumTopico;
import com.iotroom.iotroom.security.AuthenticatedUser;
import com.iotroom.iotroom.service.ProfessorForumService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/professor/forum")
public class ProfessorForumController {

    private final ProfessorForumService professorForumService;

    public ProfessorForumController(ProfessorForumService professorForumService) {
        this.professorForumService = professorForumService;
    }

    @GetMapping
    public String index(Model model, Authentication authentication) {
        Long professorId = obterUtilizadorId(authentication);

        model.addAttribute("topicos", professorForumService.listarTopicos(professorId));
        model.addAttribute("nomesGrupos", professorForumService.obterNomesGrupos(professorId));
        model.addAttribute("nomesExperiencias", professorForumService.obterNomesExperiencias(professorId));
        model.addAttribute("paginaAtual", "forum");

        return "professor/forum/index";
    }

    @GetMapping("/novo")
    public String novo(Model model, Authentication authentication) {
        Long professorId = obterUtilizadorId(authentication);

        model.addAttribute("topicoForm", professorForumService.criarFormVazio());
        model.addAttribute("grupos", professorForumService.listarGrupos(professorId));
        model.addAttribute("experiencias", professorForumService.listarExperiencias(professorId));
        model.addAttribute("paginaAtual", "forum");

        return "professor/forum/novo";
    }

    @PostMapping("/novo")
    public String criar(
            @ModelAttribute("topicoForm") ForumTopicoFormDTO topicoForm,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        ForumTopico topico = professorForumService.criarTopico(professorId, topicoForm);

        return "redirect:/professor/forum/" + topico.getId();
    }

    @GetMapping("/{id}")
    public String ver(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        ForumTopico topico = professorForumService.obterTopico(id, professorId);

        model.addAttribute("topico", topico);
        model.addAttribute("respostas", professorForumService.listarRespostas(id));
        model.addAttribute("respostaForm", new ForumRespostaFormDTO());
        model.addAttribute("nomesGrupos", professorForumService.obterNomesGrupos(professorId));
        model.addAttribute("nomesExperiencias", professorForumService.obterNomesExperiencias(professorId));
        model.addAttribute("paginaAtual", "forum");

        return "professor/forum/ver";
    }

    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        ForumTopico topico = professorForumService.obterTopico(id, professorId);

        model.addAttribute("topicoForm", professorForumService.criarFormAPartirDeTopico(topico));
        model.addAttribute("grupos", professorForumService.listarGrupos(professorId));
        model.addAttribute("experiencias", professorForumService.listarExperiencias(professorId));
        model.addAttribute("paginaAtual", "forum");

        return "professor/forum/editar";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(
            @PathVariable Long id,
            @ModelAttribute("topicoForm") ForumTopicoFormDTO topicoForm,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorForumService.atualizarTopico(id, professorId, topicoForm);

        return "redirect:/professor/forum/" + id;
    }

    @PostMapping("/{id}/responder")
    public String responder(
            @PathVariable Long id,
            @ModelAttribute("respostaForm") ForumRespostaFormDTO respostaForm,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorForumService.responder(id, professorId, respostaForm);

        return "redirect:/professor/forum/" + id;
    }

    @PostMapping("/{id}/fechar")
    public String fechar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorForumService.fecharTopico(id, professorId);

        return "redirect:/professor/forum/" + id;
    }

    @PostMapping("/{id}/reabrir")
    public String reabrir(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorForumService.reabrirTopico(id, professorId);

        return "redirect:/professor/forum/" + id;
    }

    private Long obterUtilizadorId(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        return user.getId();
    }
}