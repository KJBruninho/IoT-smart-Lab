package com.iotroom.iotroom.controller;

import com.iotroom.iotroom.dto.RegraAlertaSensorFormDTO;
import com.iotroom.iotroom.model.RegraAlertaSensor;
import com.iotroom.iotroom.security.AuthenticatedUser;
import com.iotroom.iotroom.service.ProfessorAlertaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/professor/alertas")
public class ProfessorAlertaController {

    private final ProfessorAlertaService professorAlertaService;

    public ProfessorAlertaController(ProfessorAlertaService professorAlertaService) {
        this.professorAlertaService = professorAlertaService;
    }

    @GetMapping
    public String index(Model model, Authentication authentication) {
        Long professorId = obterUtilizadorId(authentication);

        model.addAttribute("alertas", professorAlertaService.listarAlertas(professorId));
        model.addAttribute("paginaAtual", "alertas");

        return "professor/alertas/index";
    }

    @GetMapping("/regras")
    public String regras(Model model, Authentication authentication) {
        Long professorId = obterUtilizadorId(authentication);

        model.addAttribute("regras", professorAlertaService.listarRegras(professorId));
        model.addAttribute("paginaAtual", "alertas");

        return "professor/alertas/regras";
    }

    @GetMapping("/regras/nova")
    public String novaRegra(Model model, Authentication authentication) {
        Long professorId = obterUtilizadorId(authentication);

        prepararFormulario(model, professorId);
        model.addAttribute("regraForm", professorAlertaService.criarFormVazio());

        return "professor/alertas/nova-regra";
    }

    @PostMapping("/regras/nova")
    public String criarRegra(
            @ModelAttribute("regraForm") RegraAlertaSensorFormDTO regraForm,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorAlertaService.criarRegra(professorId, regraForm);

        return "redirect:/professor/alertas/regras";
    }

    @GetMapping("/regras/{id}/editar")
    public String editarRegra(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        RegraAlertaSensor regra = professorAlertaService.obterRegra(id, professorId);

        prepararFormulario(model, professorId);
        model.addAttribute("regraForm", professorAlertaService.criarFormAPartirDeRegra(regra));

        return "professor/alertas/editar-regra";
    }

    @PostMapping("/regras/{id}/editar")
    public String atualizarRegra(
            @PathVariable Long id,
            @ModelAttribute("regraForm") RegraAlertaSensorFormDTO regraForm,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorAlertaService.atualizarRegra(id, professorId, regraForm);

        return "redirect:/professor/alertas/regras";
    }

    @PostMapping("/regras/{id}/alternar-estado")
    public String alternarEstadoRegra(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorAlertaService.alternarEstadoRegra(id, professorId);

        return "redirect:/professor/alertas/regras";
    }

    @PostMapping("/{id}/marcar-lido")
    public String marcarLido(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorAlertaService.marcarLido(id, professorId);

        return "redirect:/professor/alertas";
    }

    @PostMapping("/{id}/resolver")
    public String resolver(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorAlertaService.resolver(id, professorId);

        return "redirect:/professor/alertas";
    }

    @PostMapping("/{id}/ignorar")
    public String ignorar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        professorAlertaService.ignorar(id, professorId);

        return "redirect:/professor/alertas";
    }

    private void prepararFormulario(Model model, Long professorId) {
        model.addAttribute("grupos", professorAlertaService.listarGrupos(professorId));
        model.addAttribute("experiencias", professorAlertaService.listarExperiencias(professorId));
        model.addAttribute("estacoes", professorAlertaService.listarEstacoes(professorId));
        model.addAttribute("paginaAtual", "alertas");
    }

    private Long obterUtilizadorId(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        return user.getId();
    }
}