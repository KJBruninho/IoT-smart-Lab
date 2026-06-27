package com.iotroom.iotroom.controller.admin;

import com.iotroom.iotroom.dto.admin.AdminConfiguracoesSistemaForm;
import com.iotroom.iotroom.dto.admin.AdminEstacaoForm;
import com.iotroom.iotroom.dto.admin.AdminSensorForm;
import com.iotroom.iotroom.dto.admin.AdminUtilizadorForm;
import com.iotroom.iotroom.service.admin.AdminComandoSensorService;
import com.iotroom.iotroom.service.admin.AdminConfiguracoesSistemaService;
import com.iotroom.iotroom.service.admin.AdminDashboardService;
import com.iotroom.iotroom.service.admin.AdminEstacaoService;
import com.iotroom.iotroom.service.admin.AdminLogService;
import com.iotroom.iotroom.service.admin.AdminSensorService;
import com.iotroom.iotroom.service.admin.AdminUtilizadorService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

	private final AdminComandoSensorService adminComandoSensorService;
	private final AdminConfiguracoesSistemaService adminConfiguracoesSistemaService;
	private final AdminLogService adminLogService;
	private final AdminSensorService adminSensorService;
	private final AdminEstacaoService adminEstacaoService;
    private final AdminDashboardService adminDashboardService;
    private final AdminUtilizadorService adminUtilizadorService;

    public AdminController(
    		AdminComandoSensorService adminComandoSensorService,
            AdminDashboardService adminDashboardService,
            AdminUtilizadorService adminUtilizadorService,
            AdminEstacaoService adminEstacaoService,
            AdminSensorService adminSensorService,
            AdminLogService adminLogService,
            AdminConfiguracoesSistemaService adminConfiguracoesSistemaService
    ) {
        this.adminDashboardService = adminDashboardService;
        this.adminUtilizadorService = adminUtilizadorService;
        this.adminEstacaoService = adminEstacaoService;
        this.adminSensorService = adminSensorService;
        this.adminLogService = adminLogService;
        this.adminConfiguracoesSistemaService = adminConfiguracoesSistemaService;
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
