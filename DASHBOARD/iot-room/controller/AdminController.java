package com.iotroom.iotroom.controller;

import java.util.List;
import com.iotroom.iotroom.dto.AdminExperienciaForm;
import com.iotroom.iotroom.service.AdminComandoSensorService;
import com.iotroom.iotroom.service.AdminExperienciaService;
import com.iotroom.iotroom.dto.AdminConfiguracoesSistemaForm;
import com.iotroom.iotroom.service.AdminConfiguracoesSistemaService;
import com.iotroom.iotroom.dto.AdminPedidoRespostaForm;
import com.iotroom.iotroom.service.AdminPedidoSensorService;
import com.iotroom.iotroom.dto.AdminSensorForm;
import com.iotroom.iotroom.service.AdminSensorService;
import com.iotroom.iotroom.dto.AdminEstacaoForm;
import com.iotroom.iotroom.service.AdminEstacaoService;
import com.iotroom.iotroom.service.AdminGrupoPermissaoService;
import com.iotroom.iotroom.dto.AdminGrupoForm;
import com.iotroom.iotroom.service.AdminGrupoService;
import com.iotroom.iotroom.dto.AdminUtilizadorForm;
import com.iotroom.iotroom.service.AdminDashboardService;
import com.iotroom.iotroom.service.AdminUtilizadorService;
import org.springframework.stereotype.Controller;
import com.iotroom.iotroom.service.AdminLogService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

	private final AdminComandoSensorService adminComandoSensorService;
	private final AdminConfiguracoesSistemaService adminConfiguracoesSistemaService;
	private final AdminLogService adminLogService;
	private final AdminPedidoSensorService adminPedidoSensorService;
	private final AdminSensorService adminSensorService;
	private final AdminEstacaoService adminEstacaoService;
	private final AdminGrupoPermissaoService adminGrupoPermissaoService;
	private final AdminGrupoService adminGrupoService;
    private final AdminDashboardService adminDashboardService;
    private final AdminUtilizadorService adminUtilizadorService;
    private final AdminExperienciaService adminExperienciaService;

    public AdminController(
    		AdminComandoSensorService adminComandoSensorService,
            AdminDashboardService adminDashboardService,
            AdminUtilizadorService adminUtilizadorService,
            AdminGrupoService adminGrupoService,
            AdminGrupoPermissaoService adminGrupoPermissaoService,
            AdminEstacaoService adminEstacaoService,
            AdminSensorService adminSensorService,
            AdminPedidoSensorService adminPedidoSensorService,
            AdminLogService adminLogService,
            AdminConfiguracoesSistemaService adminConfiguracoesSistemaService,
            AdminExperienciaService adminExperienciaService
    ) {
        this.adminDashboardService = adminDashboardService;
        this.adminUtilizadorService = adminUtilizadorService;
        this.adminGrupoService = adminGrupoService;
        this.adminGrupoPermissaoService = adminGrupoPermissaoService;
        this.adminEstacaoService = adminEstacaoService;
        this.adminSensorService = adminSensorService;
        this.adminPedidoSensorService = adminPedidoSensorService;
        this.adminLogService = adminLogService;
        this.adminConfiguracoesSistemaService = adminConfiguracoesSistemaService;
        this.adminExperienciaService = adminExperienciaService;
        this.adminComandoSensorService = adminComandoSensorService;
    }
    
    @GetMapping("/comandos")
    public String listarComandos(
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "tipoSensor", required = false) String tipoSensor,
            @RequestParam(value = "deviceId", required = false) String deviceId,
            @RequestParam(value = "termo", required = false) String termo,
            @RequestParam(value = "sucesso", required = false) String sucesso,
            @RequestParam(value = "erro", required = false) String erro,
            Model model
    ) {
        model.addAttribute("comandos", adminComandoSensorService.listar(estado, tipoSensor, deviceId, termo));

        model.addAttribute("estado", estado);
        model.addAttribute("tipoSensor", tipoSensor);
        model.addAttribute("deviceId", deviceId);
        model.addAttribute("termo", termo);
        model.addAttribute("paginaAtiva", "comandos");

        if (sucesso != null) {
            model.addAttribute("mensagemSucesso", sucesso);
        }

        if (erro != null) {
            model.addAttribute("mensagemErro", erro);
        }

        return "admin/comandos";
    }

    @PostMapping("/comandos/{id}/reenviar")
    public String reenviarComando(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminComandoSensorService.reenviar(id);
            redirectAttributes.addAttribute("sucesso", "Comando colocado novamente na fila de envio.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", e.getMessage());
        }

        return "redirect:/admin/comandos";
    }
    
    @GetMapping("/experiencias")
    public String listarExperiencias(
            @RequestParam(value = "termo", required = false) String termo,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "grupoId", required = false) Long grupoId,
            @RequestParam(value = "sucesso", required = false) String sucesso,
            @RequestParam(value = "erro", required = false) String erro,
            Model model
    ) {
        model.addAttribute("experiencias", adminExperienciaService.listar(termo, estado, grupoId));
        model.addAttribute("grupos", adminExperienciaService.listarGruposParaSelect());

        model.addAttribute("termo", termo);
        model.addAttribute("estado", estado);
        model.addAttribute("grupoId", grupoId);
        model.addAttribute("paginaAtiva", "experiencias");

        if (sucesso != null) {
            model.addAttribute("mensagemSucesso", sucesso);
        }

        if (erro != null) {
            model.addAttribute("mensagemErro", erro);
        }

        return "admin/experiencias";
    }

    @GetMapping("/experiencias/nova")
    public String novaExperiencia(Model model) {
        model.addAttribute("form", new AdminExperienciaForm());
        model.addAttribute("grupos", adminExperienciaService.listarGruposParaSelect());
        model.addAttribute("estacoes", adminExperienciaService.listarEstacoesParaSelect());
        model.addAttribute("modo", "criar");
        model.addAttribute("paginaAtiva", "experiencias");

        return "admin/experiencia-form";
    }

    @PostMapping("/experiencias")
    public String criarExperiencia(
            @ModelAttribute("form") AdminExperienciaForm form,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminExperienciaService.criar(form);
            redirectAttributes.addAttribute("sucesso", "Experiência criada com sucesso.");
            return "redirect:/admin/experiencias";
        } catch (Exception e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("form", form);
            model.addAttribute("grupos", adminExperienciaService.listarGruposParaSelect());
            model.addAttribute("estacoes", adminExperienciaService.listarEstacoesParaSelect());
            model.addAttribute("modo", "criar");
            model.addAttribute("paginaAtiva", "experiencias");
            return "admin/experiencia-form";
        }
    }

    @GetMapping("/experiencias/{id}/editar")
    public String editarExperiencia(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            model.addAttribute(
                    "form",
                    AdminExperienciaForm.from(
                            adminExperienciaService.obterPorId(id),
                            adminExperienciaService.obterEstacaoIdsDaExperiencia(id)
                    )
            );

            model.addAttribute("grupos", adminExperienciaService.listarGruposParaSelect());
            model.addAttribute("estacoes", adminExperienciaService.listarEstacoesParaSelect());
            model.addAttribute("modo", "editar");
            model.addAttribute("paginaAtiva", "experiencias");

            return "admin/experiencia-form";
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", e.getMessage());
            return "redirect:/admin/experiencias";
        }
    }

    @PostMapping("/experiencias/{id}")
    public String atualizarExperiencia(
            @PathVariable Long id,
            @ModelAttribute("form") AdminExperienciaForm form,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminExperienciaService.atualizar(id, form);
            redirectAttributes.addAttribute("sucesso", "Experiência atualizada com sucesso.");
            return "redirect:/admin/experiencias";
        } catch (Exception e) {
            form.setId(id);
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("form", form);
            model.addAttribute("grupos", adminExperienciaService.listarGruposParaSelect());
            model.addAttribute("estacoes", adminExperienciaService.listarEstacoesParaSelect());
            model.addAttribute("modo", "editar");
            model.addAttribute("paginaAtiva", "experiencias");
            return "admin/experiencia-form";
        }
    }

    @PostMapping("/experiencias/{id}/iniciar")
    public String iniciarExperiencia(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminExperienciaService.iniciar(id);
            redirectAttributes.addAttribute("sucesso", "Experiência iniciada com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", e.getMessage());
        }

        return "redirect:/admin/experiencias";
    }

    @PostMapping("/experiencias/{id}/finalizar")
    public String finalizarExperiencia(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminExperienciaService.finalizar(id);
            redirectAttributes.addAttribute("sucesso", "Experiência finalizada com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", e.getMessage());
        }

        return "redirect:/admin/experiencias";
    }

    @PostMapping("/experiencias/{id}/cancelar")
    public String cancelarExperiencia(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminExperienciaService.cancelar(id);
            redirectAttributes.addAttribute("sucesso", "Experiência cancelada com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", e.getMessage());
        }

        return "redirect:/admin/experiencias";
    }
    
    @GetMapping("/configuracoes")
    public String configuracoes(
            @RequestParam(value = "sucesso", required = false) String sucesso,
            @RequestParam(value = "erro", required = false) String erro,
            Model model
    ) {
        model.addAttribute("form", adminConfiguracoesSistemaService.obterConfiguracoes());
        model.addAttribute("paginaAtiva", "configuracoes");

        if (sucesso != null) {
            model.addAttribute("mensagemSucesso", sucesso);
        }

        if (erro != null) {
            model.addAttribute("mensagemErro", erro);
        }

        return "admin/configuracoes";
    }

    @PostMapping("/configuracoes")
    public String guardarConfiguracoes(
            @ModelAttribute("form") AdminConfiguracoesSistemaForm form,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminConfiguracoesSistemaService.guardar(form);
            redirectAttributes.addAttribute("sucesso", "Configurações guardadas com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", e.getMessage());
        }

        return "redirect:/admin/configuracoes";
    }
    
    @GetMapping
    public String dashboard(
            @RequestParam(value = "sucesso", required = false) String sucesso,
            @RequestParam(value = "erro", required = false) String erro,
            Model model
    ) {
        model.addAttribute("stats", adminDashboardService.obterResumoDashboard());
        model.addAttribute("logsRecentes", adminDashboardService.obterLogsRecentes());
        model.addAttribute("paginaAtiva", "dashboard");

        if (sucesso != null) {
            model.addAttribute("mensagemSucesso", sucesso);
        }

        if (erro != null) {
            model.addAttribute("mensagemErro", erro);
        }

        return "admin/dashboard";
    }

    @GetMapping("/utilizadores")
    public String listarUtilizadores(
            @RequestParam(value = "termo", required = false) String termo,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "sucesso", required = false) String sucesso,
            @RequestParam(value = "erro", required = false) String erro,
            Model model
    ) {
        model.addAttribute("utilizadores", adminUtilizadorService.listar(termo, role, estado));
        model.addAttribute("termo", termo);
        model.addAttribute("role", role);
        model.addAttribute("estado", estado);
        model.addAttribute("paginaAtiva", "utilizadores");

        if (sucesso != null) {
            model.addAttribute("mensagemSucesso", sucesso);
        }

        if (erro != null) {
            model.addAttribute("mensagemErro", erro);
        }

        return "admin/utilizadores";
    }

    @GetMapping("/utilizadores/novo")
    public String novoUtilizador(Model model) {
        model.addAttribute("form", new AdminUtilizadorForm());
        model.addAttribute("modo", "criar");
        model.addAttribute("paginaAtiva", "utilizadores");
        return "admin/utilizador-form";
    }

    @PostMapping("/utilizadores")
    public String criarUtilizador(
            @ModelAttribute("form") AdminUtilizadorForm form,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminUtilizadorService.criar(form);
            redirectAttributes.addAttribute("sucesso", "Utilizador criado com sucesso.");
            return "redirect:/admin/utilizadores";
        } catch (Exception e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("form", form);
            model.addAttribute("modo", "criar");
            model.addAttribute("paginaAtiva", "utilizadores");
            return "admin/utilizador-form";
        }
    }
    
    @GetMapping("/logs")
    public String listarLogs(
            @RequestParam(value = "termo", required = false) String termo,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "nivel", required = false) String nivel,
            @RequestParam(value = "dataInicio", required = false) String dataInicio,
            @RequestParam(value = "dataFim", required = false) String dataFim,
            @RequestParam(value = "sucesso", required = false) String sucesso,
            @RequestParam(value = "erro", required = false) String erro,
            Model model
    ) {
        model.addAttribute("logs", adminLogService.listar(termo, tipo, nivel, dataInicio, dataFim));
        model.addAttribute("tiposLog", adminLogService.listarTiposLog());
        model.addAttribute("niveisLog", adminLogService.listarNiveis());

        model.addAttribute("termo", termo);
        model.addAttribute("tipo", tipo);
        model.addAttribute("nivel", nivel);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);

        model.addAttribute("paginaAtiva", "logs");

        if (sucesso != null) {
            model.addAttribute("mensagemSucesso", sucesso);
        }

        if (erro != null) {
            model.addAttribute("mensagemErro", erro);
        }

        return "admin/logs";
    }

    @GetMapping("/logs/{id}")
    public String detalheLog(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            model.addAttribute("log", adminLogService.obterPorId(id));
            model.addAttribute("paginaAtiva", "logs");
            return "admin/log-detalhe";
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", e.getMessage());
            return "redirect:/admin/logs";
        }
    }

    @GetMapping("/utilizadores/{id}/editar")
    public String editarUtilizador(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            model.addAttribute("form", AdminUtilizadorForm.from(adminUtilizadorService.obterPorId(id)));
            model.addAttribute("modo", "editar");
            model.addAttribute("paginaAtiva", "utilizadores");
            return "admin/utilizador-form";
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", e.getMessage());
            return "redirect:/admin/utilizadores";
        }
    }

    @PostMapping("/utilizadores/{id}")
    public String atualizarUtilizador(
            @PathVariable Long id,
            @ModelAttribute("form") AdminUtilizadorForm form,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminUtilizadorService.atualizar(id, form);
            redirectAttributes.addAttribute("sucesso", "Utilizador atualizado com sucesso.");
            return "redirect:/admin/utilizadores";
        } catch (Exception e) {
            form.setId(id);
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("form", form);
            model.addAttribute("modo", "editar");
            model.addAttribute("paginaAtiva", "utilizadores");
            return "admin/utilizador-form";
        }
    }

    @PostMapping("/utilizadores/{id}/estado")
    public String alternarEstadoUtilizador(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminUtilizadorService.alternarEstado(id);
            redirectAttributes.addAttribute("sucesso", "Estado do utilizador alterado com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", "Não foi possível alterar o estado do utilizador.");
        }

        return "redirect:/admin/utilizadores";
    }
    
    @GetMapping("/grupos")
    public String listarGrupos(
            @RequestParam(value = "termo", required = false) String termo,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "sucesso", required = false) String sucesso,
            @RequestParam(value = "erro", required = false) String erro,
            Model model
    ) {
        model.addAttribute("grupos", adminGrupoService.listar(termo, estado));
        model.addAttribute("termo", termo);
        model.addAttribute("estado", estado);
        model.addAttribute("paginaAtiva", "grupos");

        if (sucesso != null) {
            model.addAttribute("mensagemSucesso", sucesso);
        }

        if (erro != null) {
            model.addAttribute("mensagemErro", erro);
        }

        return "admin/grupos";
    }
    
    @GetMapping("/pedidos")
    public String listarPedidos(
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "origem", required = false) String origem,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "sucesso", required = false) String sucesso,
            @RequestParam(value = "erro", required = false) String erro,
            Model model
    ) {
        model.addAttribute("pedidos", adminPedidoSensorService.listar(estado, origem, tipo));
        model.addAttribute("estado", estado);
        model.addAttribute("origem", origem);
        model.addAttribute("tipo", tipo);
        model.addAttribute("respostaForm", new AdminPedidoRespostaForm());
        model.addAttribute("paginaAtiva", "pedidos");

        if (sucesso != null) {
            model.addAttribute("mensagemSucesso", sucesso);
        }

        if (erro != null) {
            model.addAttribute("mensagemErro", erro);
        }

        return "admin/pedidos";
    }

    @PostMapping("/pedidos/{id}/aprovar")
    public String aprovarPedido(
            @PathVariable Long id,
            @ModelAttribute AdminPedidoRespostaForm respostaForm,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminPedidoSensorService.aprovar(id, respostaForm);
            redirectAttributes.addAttribute("sucesso", "Pedido aprovado. Configuração atualizada e comando criado para o sensor.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", e.getMessage());
        }

        return "redirect:/admin/pedidos";
    }

    @PostMapping("/pedidos/{id}/rejeitar")
    public String rejeitarPedido(
            @PathVariable Long id,
            @ModelAttribute AdminPedidoRespostaForm respostaForm,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminPedidoSensorService.rejeitar(id, respostaForm);
            redirectAttributes.addAttribute("sucesso", "Pedido rejeitado com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", e.getMessage());
        }

        return "redirect:/admin/pedidos";
    }

    @GetMapping("/grupos/novo")
    public String novoGrupo(Model model) {
        model.addAttribute("form", new AdminGrupoForm());
        model.addAttribute("modo", "criar");
        model.addAttribute("paginaAtiva", "grupos");
        return "admin/grupo-form";
    }

    @PostMapping("/grupos")
    public String criarGrupo(
            @ModelAttribute("form") AdminGrupoForm form,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminGrupoService.criar(form);
            redirectAttributes.addAttribute("sucesso", "Grupo criado com sucesso.");
            return "redirect:/admin/grupos";
        } catch (Exception e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("form", form);
            model.addAttribute("modo", "criar");
            model.addAttribute("paginaAtiva", "grupos");
            return "admin/grupo-form";
        }
    }

    @GetMapping("/grupos/{id}/editar")
    public String editarGrupo(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            model.addAttribute("form", AdminGrupoForm.from(adminGrupoService.obterPorId(id)));
            model.addAttribute("modo", "editar");
            model.addAttribute("paginaAtiva", "grupos");
            return "admin/grupo-form";
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", e.getMessage());
            return "redirect:/admin/grupos";
        }
    }

    @PostMapping("/grupos/{id}")
    public String atualizarGrupo(
            @PathVariable Long id,
            @ModelAttribute("form") AdminGrupoForm form,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminGrupoService.atualizar(id, form);
            redirectAttributes.addAttribute("sucesso", "Grupo atualizado com sucesso.");
            return "redirect:/admin/grupos";
        } catch (Exception e) {
            form.setId(id);
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("form", form);
            model.addAttribute("modo", "editar");
            model.addAttribute("paginaAtiva", "grupos");
            return "admin/grupo-form";
        }
    }
    
    @GetMapping("/grupos/{id}/estacoes")
    public String gerirEstacoesDoGrupo(
            @PathVariable Long id,
            @RequestParam(value = "sucesso", required = false) String sucesso,
            @RequestParam(value = "erro", required = false) String erro,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            model.addAttribute("grupo", adminGrupoService.obterPorId(id));
            model.addAttribute("estacoes", adminGrupoPermissaoService.listarEstacoesDoGrupo(id));
            model.addAttribute("paginaAtiva", "grupos");

            if (sucesso != null) {
                model.addAttribute("mensagemSucesso", sucesso);
            }

            if (erro != null) {
                model.addAttribute("mensagemErro", erro);
            }

            return "admin/grupo-estacoes";
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", e.getMessage());
            return "redirect:/admin/grupos";
        }
    }

    @PostMapping("/grupos/{id}/estacoes")
    public String atualizarEstacoesDoGrupo(
            @PathVariable Long id,
            @RequestParam(value = "estacaoIds", required = false) List<Long> estacaoIds,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminGrupoPermissaoService.atualizarEstacoesDoGrupo(id, estacaoIds);
            redirectAttributes.addAttribute("sucesso", "Permissões de estações atualizadas com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", e.getMessage());
        }

        return "redirect:/admin/grupos/" + id + "/estacoes";
    }

    @PostMapping("/grupos/{id}/estado")
    public String alternarEstadoGrupo(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminGrupoService.alternarEstado(id);
            redirectAttributes.addAttribute("sucesso", "Estado do grupo alterado com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", e.getMessage());
        }

        return "redirect:/admin/grupos";
    }
    
    @GetMapping("/estacoes")
    public String listarEstacoes(
            @RequestParam(value = "termo", required = false) String termo,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "sucesso", required = false) String sucesso,
            @RequestParam(value = "erro", required = false) String erro,
            Model model
    ) {
        model.addAttribute("estacoes", adminEstacaoService.listar(termo, estado));
        model.addAttribute("termo", termo);
        model.addAttribute("estado", estado);
        model.addAttribute("paginaAtiva", "estacoes");

        if (sucesso != null) {
            model.addAttribute("mensagemSucesso", sucesso);
        }

        if (erro != null) {
            model.addAttribute("mensagemErro", erro);
        }

        return "admin/estacoes";
    }

    @GetMapping("/estacoes/novo")
    public String novaEstacao(Model model) {
        model.addAttribute("form", new AdminEstacaoForm());
        model.addAttribute("modo", "criar");
        model.addAttribute("paginaAtiva", "estacoes");
        return "admin/estacao-form";
    }

    @PostMapping("/estacoes")
    public String criarEstacao(
            @ModelAttribute("form") AdminEstacaoForm form,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminEstacaoService.criar(form);
            redirectAttributes.addAttribute("sucesso", "Estação criada com sucesso.");
            return "redirect:/admin/estacoes";
        } catch (Exception e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("form", form);
            model.addAttribute("modo", "criar");
            model.addAttribute("paginaAtiva", "estacoes");
            return "admin/estacao-form";
        }
    }

    @GetMapping("/estacoes/{id}/editar")
    public String editarEstacao(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            model.addAttribute("form", AdminEstacaoForm.from(adminEstacaoService.obterPorId(id)));
            model.addAttribute("modo", "editar");
            model.addAttribute("paginaAtiva", "estacoes");
            return "admin/estacao-form";
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", e.getMessage());
            return "redirect:/admin/estacoes";
        }
    }

    @PostMapping("/estacoes/{id}")
    public String atualizarEstacao(
            @PathVariable Long id,
            @ModelAttribute("form") AdminEstacaoForm form,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminEstacaoService.atualizar(id, form);
            redirectAttributes.addAttribute("sucesso", "Estação atualizada com sucesso.");
            return "redirect:/admin/estacoes";
        } catch (Exception e) {
            form.setId(id);
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("form", form);
            model.addAttribute("modo", "editar");
            model.addAttribute("paginaAtiva", "estacoes");
            return "admin/estacao-form";
        }
    }
    
    @GetMapping("/sensores")
    public String listarSensores(
            @RequestParam(value = "estacaoId", required = false) Long estacaoId,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "sucesso", required = false) String sucesso,
            @RequestParam(value = "erro", required = false) String erro,
            Model model
    ) {
        model.addAttribute("sensores", adminSensorService.listar(estacaoId, tipo, estado));
        model.addAttribute("estacoes", adminSensorService.listarEstacoesParaSelect());

        model.addAttribute("estacaoId", estacaoId);
        model.addAttribute("tipo", tipo);
        model.addAttribute("estado", estado);
        model.addAttribute("paginaAtiva", "sensores");

        if (sucesso != null) {
            model.addAttribute("mensagemSucesso", sucesso);
        }

        if (erro != null) {
            model.addAttribute("mensagemErro", erro);
        }

        return "admin/sensores";
    }

    @GetMapping("/sensores/novo")
    public String novoSensor(
            @RequestParam(value = "estacaoId", required = false) Long estacaoId,
            Model model
    ) {
        AdminSensorForm form = new AdminSensorForm();

        if (estacaoId != null) {
            form.setEstacaoId(estacaoId);
        }

        model.addAttribute("form", form);
        model.addAttribute("estacoes", adminSensorService.listarEstacoesParaSelect());
        model.addAttribute("modo", "criar");
        model.addAttribute("paginaAtiva", "sensores");

        return "admin/sensor-form";
    }

    @PostMapping("/sensores")
    public String criarSensor(
            @ModelAttribute("form") AdminSensorForm form,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminSensorService.criar(form);
            redirectAttributes.addAttribute("sucesso", "Sensor criado com sucesso.");
            return "redirect:/admin/sensores";
        } catch (Exception e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("form", form);
            model.addAttribute("estacoes", adminSensorService.listarEstacoesParaSelect());
            model.addAttribute("modo", "criar");
            model.addAttribute("paginaAtiva", "sensores");
            return "admin/sensor-form";
        }
    }

    @GetMapping("/sensores/{id}/editar")
    public String editarSensor(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            model.addAttribute("form", AdminSensorForm.from(adminSensorService.obterPorId(id)));
            model.addAttribute("estacoes", adminSensorService.listarEstacoesParaSelect());
            model.addAttribute("modo", "editar");
            model.addAttribute("paginaAtiva", "sensores");
            return "admin/sensor-form";
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", e.getMessage());
            return "redirect:/admin/sensores";
        }
    }

    @PostMapping("/sensores/{id}")
    public String atualizarSensor(
            @PathVariable Long id,
            @ModelAttribute("form") AdminSensorForm form,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminSensorService.atualizar(id, form);
            redirectAttributes.addAttribute("sucesso", "Sensor atualizado com sucesso.");
            return "redirect:/admin/sensores";
        } catch (Exception e) {
            form.setId(id);
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("form", form);
            model.addAttribute("estacoes", adminSensorService.listarEstacoesParaSelect());
            model.addAttribute("modo", "editar");
            model.addAttribute("paginaAtiva", "sensores");
            return "admin/sensor-form";
        }
    }

    @PostMapping("/sensores/{id}/estado")
    public String alternarEstadoSensor(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminSensorService.alternarEstado(id);
            redirectAttributes.addAttribute("sucesso", "Estado local do sensor alterado com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", "Não foi possível alterar o estado do sensor.");
        }

        return "redirect:/admin/sensores";
    }

    @PostMapping("/sensores/{id}/remoto")
    public String alternarEstadoRemotoSensor(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminSensorService.alternarEstadoRemoto(id);
            redirectAttributes.addAttribute("sucesso", "Comando remoto registado com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", "Não foi possível registar o comando remoto.");
        }

        return "redirect:/admin/sensores";
    }

    @PostMapping("/estacoes/{id}/estado")
    public String alternarEstadoEstacao(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminEstacaoService.alternarEstado(id);
            redirectAttributes.addAttribute("sucesso", "Estado da estação alterado com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("erro", "Não foi possível alterar o estado da estação.");
        }

        return "redirect:/admin/estacoes";
    }
}