package com.iotroom.iotroom.controller;

import com.iotroom.iotroom.dto.AlunoDadosFiltroForm;
import com.iotroom.iotroom.dto.AlunoLeituraDTO;
import com.iotroom.iotroom.dto.AlunoPedidoModoForm;
import com.iotroom.iotroom.security.AuthenticatedUser;
import com.iotroom.iotroom.service.AlunoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/aluno")
public class AlunoController {

    private static final DateTimeFormatter INPUT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter CHART_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @GetMapping
    public String dashboard(Model model, Authentication authentication) {
        Long alunoId = obterUtilizadorId(authentication);
        model.addAttribute("dashboard", alunoService.obterDashboard(alunoId));
        model.addAttribute("paginaAtual", "dashboard");
        return "aluno/index";
    }

    @GetMapping("/dados")
    public String dados(
            @RequestParam(required = false) Long grupoId,
            @RequestParam(required = false) Long experienciaId,
            @RequestParam(required = false) Long estacaoId,
            @RequestParam(required = false) String tipoSensor,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false, defaultValue = "50") Integer limite,
            Model model,
            Authentication authentication
    ) {
        Long alunoId = obterUtilizadorId(authentication);

        AlunoDadosFiltroForm filtro = new AlunoDadosFiltroForm();
        filtro.setGrupoId(grupoId);
        filtro.setExperienciaId(experienciaId);
        filtro.setEstacaoId(estacaoId);
        filtro.setTipoSensor(tipoSensor);
        filtro.setDataInicio(dataInicio);
        filtro.setDataFim(dataFim);
        filtro.setLimite(limite);

        List<AlunoLeituraDTO> leituras = alunoService.listarLeituras(alunoId, filtro);

        model.addAttribute("filtro", filtro);
        model.addAttribute("dataInicioValue", formatarDataInput(dataInicio));
        model.addAttribute("dataFimValue", formatarDataInput(dataFim));
        model.addAttribute("leituras", leituras);
        model.addAttribute("grupos", alunoService.listarGrupos(alunoId));
        model.addAttribute("experiencias", alunoService.listarExperiencias(alunoId));
        model.addAttribute("estacoes", alunoService.listarEstacoes(alunoId));
        model.addAttribute("tiposSensor", List.of("TEMPERATURA", "TDS", "PH"));
        model.addAttribute("chartLabels", chartLabels(leituras));
        model.addAttribute("chartValores", chartValores(leituras));
        model.addAttribute("paginaAtual", "dados");
        return "aluno/dados";
    }

    @GetMapping("/pedidos")
    public String pedidos(Model model, Authentication authentication) {
        Long alunoId = obterUtilizadorId(authentication);
        prepararModeloPedidos(model, alunoId, new AlunoPedidoModoForm());
        return "aluno/pedidos";
    }

    @PostMapping("/pedidos")
    public String criarPedido(
            @RequestParam Long sensorId,
            @RequestParam Integer intervaloRapidoMs,
            @RequestParam Integer intervaloEstavelMs,
            @RequestParam Integer duracaoModoRapidoMs,
            @RequestParam BigDecimal deltaSignificativo,
            @RequestParam(required = false) String motivo,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        Long alunoId = obterUtilizadorId(authentication);

        AlunoPedidoModoForm form = new AlunoPedidoModoForm();
        form.setSensorId(sensorId);
        form.setIntervaloRapidoMs(intervaloRapidoMs);
        form.setIntervaloEstavelMs(intervaloEstavelMs);
        form.setDuracaoModoRapidoMs(duracaoModoRapidoMs);
        form.setDeltaSignificativo(deltaSignificativo);
        form.setMotivo(motivo);

        try {
            alunoService.criarPedidoModo(alunoId, form);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido enviado para análise.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/aluno/pedidos";
    }

    @GetMapping("/pessoal")
    public String pessoal(Model model, Authentication authentication) {
        AuthenticatedUser user = obterUtilizador(authentication);
        model.addAttribute("perfil", alunoService.obterPerfil(user));
        model.addAttribute("paginaAtual", "pessoal");
        return "aluno/pessoal";
    }

    private void prepararModeloPedidos(Model model, Long alunoId, AlunoPedidoModoForm form) {
        model.addAttribute("form", form);
        model.addAttribute("sensores", alunoService.listarSensores(alunoId));
        model.addAttribute("pedidos", alunoService.listarPedidos(alunoId));
        model.addAttribute("paginaAtual", "pedidos");
    }

    private List<String> chartLabels(List<AlunoLeituraDTO> leituras) {
        List<String> labels = new ArrayList<>();
        List<AlunoLeituraDTO> ordenadas = new ArrayList<>(leituras);
        Collections.reverse(ordenadas);
        for (AlunoLeituraDTO leitura : ordenadas) {
            labels.add(leitura.dataRegisto() != null ? leitura.dataRegisto().format(CHART_DATETIME_FORMATTER) : "-");
        }
        return labels;
    }

    private List<BigDecimal> chartValores(List<AlunoLeituraDTO> leituras) {
        List<BigDecimal> valores = new ArrayList<>();
        List<AlunoLeituraDTO> ordenadas = new ArrayList<>(leituras);
        Collections.reverse(ordenadas);
        for (AlunoLeituraDTO leitura : ordenadas) {
            valores.add(leitura.valor());
        }
        return valores;
    }

    private String formatarDataInput(LocalDateTime data) {
        return data != null ? data.format(INPUT_DATETIME_FORMATTER) : "";
    }

    private Long obterUtilizadorId(Authentication authentication) {
        return obterUtilizador(authentication).getId();
    }

    private AuthenticatedUser obterUtilizador(Authentication authentication) {
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
