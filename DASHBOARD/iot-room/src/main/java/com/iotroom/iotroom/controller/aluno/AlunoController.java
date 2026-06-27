package com.iotroom.iotroom.controller.aluno;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iotroom.iotroom.dto.aluno.AlunoDadosFiltroForm;
import com.iotroom.iotroom.dto.aluno.AlunoGraficoDTO;
import com.iotroom.iotroom.dto.aluno.AlunoLeituraDTO;
import com.iotroom.iotroom.dto.aluno.AlunoPedidoModoForm;
import com.iotroom.iotroom.security.AuthenticatedUser;
import com.iotroom.iotroom.service.aluno.AlunoService;
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
    private final ObjectMapper objectMapper;

    public AlunoController(AlunoService alunoService, ObjectMapper objectMapper) {
        this.alunoService = alunoService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String dashboard(Model model, Authentication authentication) {
        AcessoAluno acesso = obterAcessoAluno(authentication);

        model.addAttribute(
                "dashboard",
                alunoService.obterDashboard(acesso.id(), acesso.admin(), acesso.professor())
        );
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
        AcessoAluno acesso = obterAcessoAluno(authentication);

        AlunoDadosFiltroForm filtro = new AlunoDadosFiltroForm();
        filtro.setGrupoId(grupoId);
        filtro.setExperienciaId(experienciaId);
        filtro.setEstacaoId(estacaoId);
        filtro.setTipoSensor(tipoSensor);
        filtro.setDataInicio(dataInicio);
        filtro.setDataFim(dataFim);
        filtro.setLimite(limite);

        List<AlunoLeituraDTO> leituras = alunoService.listarLeituras(
                acesso.id(),
                acesso.admin(),
                acesso.professor(),
                filtro
        );

        List<AlunoGraficoDTO> graficos = alunoService.listarGraficos(
                acesso.id(),
                acesso.admin(),
                acesso.professor(),
                filtro
        );

        model.addAttribute("filtro", filtro);
        model.addAttribute("dataInicioValue", formatarDataInput(dataInicio));
        model.addAttribute("dataFimValue", formatarDataInput(dataFim));
        model.addAttribute("leituras", leituras);
        model.addAttribute("graficos", graficos);
        model.addAttribute("graficosJson", toJson(graficos));
        model.addAttribute("grupos", alunoService.listarGrupos(acesso.id(), acesso.admin(), acesso.professor()));
        model.addAttribute("experiencias", alunoService.listarExperiencias(acesso.id(), acesso.admin(), acesso.professor()));
        model.addAttribute("estacoes", alunoService.listarEstacoes(acesso.id(), acesso.admin(), acesso.professor()));
        model.addAttribute("tiposSensor", List.of("TEMPERATURA", "TDS", "PH"));
        model.addAttribute("chartLabels", chartLabels(leituras));
        model.addAttribute("chartValores", chartValores(leituras));
        model.addAttribute("paginaAtual", "dados");

        return "aluno/dados";
    }

    @GetMapping("/pedidos")
    public String pedidos(Model model, Authentication authentication) {
        AcessoAluno acesso = obterAcessoAluno(authentication);
        prepararModeloPedidos(model, acesso, new AlunoPedidoModoForm());
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
        AcessoAluno acesso = obterAcessoAluno(authentication);

        AlunoPedidoModoForm form = new AlunoPedidoModoForm();
        form.setSensorId(sensorId);
        form.setIntervaloRapidoMs(intervaloRapidoMs);
        form.setIntervaloEstavelMs(intervaloEstavelMs);
        form.setDuracaoModoRapidoMs(duracaoModoRapidoMs);
        form.setDeltaSignificativo(deltaSignificativo);
        form.setMotivo(motivo);

        try {
            alunoService.criarPedidoModo(acesso.id(), acesso.admin(), acesso.professor(), form);
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

    private void prepararModeloPedidos(Model model, AcessoAluno acesso, AlunoPedidoModoForm form) {
        model.addAttribute("form", form);
        model.addAttribute("sensores", alunoService.listarSensores(acesso.id(), acesso.admin(), acesso.professor()));
        model.addAttribute("pedidos", alunoService.listarPedidos(acesso.id()));
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

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private AcessoAluno obterAcessoAluno(Authentication authentication) {
        AuthenticatedUser user = obterUtilizador(authentication);

        boolean admin = temAuthority(authentication, "ADMIN")
                || "ADMIN".equalsIgnoreCase(user.getRole());

        boolean professor = temAuthority(authentication, "PROFESSOR")
                || "PROFESSOR".equalsIgnoreCase(user.getRole());

        return new AcessoAluno(user.getId(), admin, professor);
    }

    private boolean temAuthority(Authentication authentication, String role) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> {
                    String valor = authority.getAuthority();
                    return valor.equals(role) || valor.equals("ROLE_" + role);
                });
    }

    private AuthenticatedUser obterUtilizador(Authentication authentication) {
        return (AuthenticatedUser) authentication.getPrincipal();
    }

    private record AcessoAluno(Long id, boolean admin, boolean professor) {
    }
}
