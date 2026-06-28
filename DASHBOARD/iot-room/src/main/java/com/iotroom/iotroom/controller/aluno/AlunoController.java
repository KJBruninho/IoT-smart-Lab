package com.iotroom.iotroom.controller.aluno;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iotroom.iotroom.dto.aluno.AlunoDadosFiltroForm;
import com.iotroom.iotroom.dto.aluno.AlunoGraficoDTO;
import com.iotroom.iotroom.dto.aluno.AlunoLeituraDTO;
import com.iotroom.iotroom.dto.aluno.AlunoPedidoModoForm;
import com.iotroom.iotroom.security.AuthenticatedUser;
import com.iotroom.iotroom.service.aluno.AlunoExcelExportService;
import com.iotroom.iotroom.service.aluno.AlunoForumService;
import com.iotroom.iotroom.service.aluno.AlunoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/aluno")
public class AlunoController {

    private static final DateTimeFormatter INPUT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final AlunoService alunoService;
    private final AlunoExcelExportService alunoExcelExportService;
    private final ObjectMapper objectMapper;
    private final AlunoForumService alunoForumService;
    
    public AlunoController(
            AlunoService alunoService,
            ObjectMapper objectMapper,
            AlunoExcelExportService alunoExcelExportService,
            AlunoForumService alunoForumService
    ) {
        this.alunoService = alunoService;
        this.objectMapper = objectMapper;
        this.alunoForumService = alunoForumService;
        this.alunoExcelExportService = alunoExcelExportService;
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

    @GetMapping("/pessoal")
    public String pessoal(HttpSession session, Model model) {
        model.addAttribute("paginaAtual", "pessoal");

        model.addAttribute("nomeUtilizador", valorSessao(session, "nomeUtilizador", "Utilizador"));
        model.addAttribute("emailUtilizador", valorSessao(session, "emailUtilizador", "Sem email"));
        model.addAttribute("roleUtilizador", valorSessao(session, "roleUtilizador", "ALUNO"));

        return "aluno/pessoal";
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
            @RequestParam(required = false) Integer limite,
            @RequestParam(required = false) Integer limiteDados,
            @RequestParam(required = false) Integer limiteSeries,
            Model model,
            Authentication authentication
    ) {
        AcessoAluno acesso = obterAcessoAluno(authentication);

        int limiteDadosNormalizado = alunoService.normalizarLimitePublico(
                limiteDados != null ? limiteDados : limite
        );

        int limiteSeriesNormalizado = alunoService.normalizarLimiteSeriesPublico(limiteSeries);

        AlunoDadosFiltroForm filtro = new AlunoDadosFiltroForm();
        filtro.setGrupoId(grupoId);
        filtro.setExperienciaId(experienciaId);
        filtro.setEstacaoId(estacaoId);
        filtro.setTipoSensor(tipoSensor);
        filtro.setDataInicio(dataInicio);
        filtro.setDataFim(dataFim);
        filtro.setLimite(limiteDadosNormalizado);

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
                filtro,
                limiteSeriesNormalizado
        );

        model.addAttribute("filtro", filtro);
        model.addAttribute("limiteDados", limiteDadosNormalizado);
        model.addAttribute("limiteSeries", limiteSeriesNormalizado);
        model.addAttribute("dataInicioValue", formatarDataInput(dataInicio));
        model.addAttribute("dataFimValue", formatarDataInput(dataFim));
        model.addAttribute("leituras", leituras);
        model.addAttribute("graficos", graficos);
        model.addAttribute("graficosJson", toJson(graficos));
        model.addAttribute("grupos", alunoService.listarGrupos(acesso.id(), acesso.admin(), acesso.professor()));
        model.addAttribute("experiencias", alunoService.listarExperiencias(acesso.id(), acesso.admin(), acesso.professor()));
        model.addAttribute("estacoes", alunoService.listarEstacoes(acesso.id(), acesso.admin(), acesso.professor()));
        model.addAttribute("tiposSensor", List.of("TEMPERATURA", "TDS", "PH"));
        model.addAttribute("paginaAtual", "dados");

        return "aluno/dados";
    }

    @GetMapping("/dados/exportar/excel")
    public ResponseEntity<byte[]> exportarDadosExcelFiltrado(
            @RequestParam(required = false) Long grupoId,
            @RequestParam(required = false) Long experienciaId,
            @RequestParam(required = false) Long estacaoId,
            @RequestParam(required = false) String tipoSensor,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
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

        List<AlunoLeituraDTO> leituras = alunoService.listarLeiturasSemLimite(
                acesso.id(),
                acesso.admin(),
                acesso.professor(),
                filtro
        );

        return respostaExcel(
                alunoExcelExportService.gerarExcel(leituras),
                "leituras-filtradas.xlsx"
        );
    }

    @GetMapping("/dados/exportar/excel/todos")
    public ResponseEntity<byte[]> exportarTodosDadosExcel(Authentication authentication) {
        AcessoAluno acesso = obterAcessoAluno(authentication);

        List<AlunoLeituraDTO> leituras = alunoService.listarLeiturasSemLimite(
                acesso.id(),
                acesso.admin(),
                acesso.professor(),
                new AlunoDadosFiltroForm()
        );

        return respostaExcel(
                alunoExcelExportService.gerarExcel(leituras),
                "todas-as-leituras.xlsx"
        );
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

    private void prepararModeloPedidos(Model model, AcessoAluno acesso, AlunoPedidoModoForm form) {
        model.addAttribute("form", form);
        model.addAttribute("sensores", alunoService.listarSensores(acesso.id(), acesso.admin(), acesso.professor()));
        model.addAttribute("pedidos", alunoService.listarPedidos(acesso.id()));
        model.addAttribute("paginaAtual", "pedidos");
    }

    private ResponseEntity<byte[]> respostaExcel(byte[] conteudo, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        ));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(conteudo);
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
    
    private String valorSessao(HttpSession session, String chave, String fallback) {
        Object valor = session.getAttribute(chave);

        if (valor == null) {
            return fallback;
        }

        String texto = valor.toString();

        if (texto.isBlank()) {
            return fallback;
        }

        return texto;
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
    
    @GetMapping("/forum")
    public String forum(Model model, Authentication authentication) {
        AcessoAluno acesso = obterAcessoAluno(authentication);

        model.addAttribute("modo", "lista");
        model.addAttribute("topicos", alunoForumService.listarTopicos(acesso.id(), acesso.admin(), acesso.professor()));
        model.addAttribute("paginaAtual", "forum");

        return "aluno/forum";
    }

    @GetMapping("/forum/novo")
    public String forumNovo(Model model, Authentication authentication) {
        AcessoAluno acesso = obterAcessoAluno(authentication);

        if (!model.containsAttribute("formTitulo")) {
            model.addAttribute("formTitulo", "");
        }

        if (!model.containsAttribute("formMensagem")) {
            model.addAttribute("formMensagem", "");
        }

        if (!model.containsAttribute("formGrupoId")) {
            model.addAttribute("formGrupoId", null);
        }

        if (!model.containsAttribute("formExperienciaId")) {
            model.addAttribute("formExperienciaId", null);
        }

        model.addAttribute("modo", "novo");
        model.addAttribute("grupos", alunoForumService.listarGrupos(acesso.id(), acesso.admin(), acesso.professor()));
        model.addAttribute("experiencias", alunoForumService.listarExperiencias(acesso.id(), acesso.admin(), acesso.professor()));
        model.addAttribute("paginaAtual", "forum");

        return "aluno/forum";
    }

    @PostMapping("/forum/novo")
    public String forumCriar(
            @RequestParam String titulo,
            @RequestParam String mensagem,
            @RequestParam(required = false) Long grupoId,
            @RequestParam(required = false) Long experienciaId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        AcessoAluno acesso = obterAcessoAluno(authentication);

        try {
            Long topicoId = alunoForumService.criarTopico(
                    acesso.id(),
                    acesso.admin(),
                    acesso.professor(),
                    titulo,
                    mensagem,
                    grupoId,
                    experienciaId
            );

            redirectAttributes.addFlashAttribute("sucesso", "Tópico criado com sucesso.");
            return "redirect:/aluno/forum/" + topicoId;
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            redirectAttributes.addFlashAttribute("formTitulo", titulo);
            redirectAttributes.addFlashAttribute("formMensagem", mensagem);
            redirectAttributes.addFlashAttribute("formGrupoId", grupoId);
            redirectAttributes.addFlashAttribute("formExperienciaId", experienciaId);

            return "redirect:/aluno/forum/novo";
        }
    }

    @GetMapping("/forum/{id}")
    public String forumVer(
            @PathVariable Long id,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        AcessoAluno acesso = obterAcessoAluno(authentication);

        try {
            model.addAttribute("modo", "ver");
            model.addAttribute("topico", alunoForumService.obterTopico(id, acesso.id(), acesso.admin(), acesso.professor()));
            model.addAttribute("respostas", alunoForumService.listarRespostas(id, acesso.id(), acesso.admin(), acesso.professor()));
            model.addAttribute("paginaAtual", "forum");

            return "aluno/forum";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/aluno/forum";
        }
    }

    @PostMapping("/forum/{id}/responder")
    public String forumResponder(
            @PathVariable Long id,
            @RequestParam String mensagem,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        AcessoAluno acesso = obterAcessoAluno(authentication);

        try {
            alunoForumService.responder(id, acesso.id(), acesso.admin(), acesso.professor(), mensagem);
            redirectAttributes.addFlashAttribute("sucesso", "Resposta enviada.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/aluno/forum/" + id;
    }

    private AuthenticatedUser obterUtilizador(Authentication authentication) {
        return (AuthenticatedUser) authentication.getPrincipal();
    }

    private record AcessoAluno(Long id, boolean admin, boolean professor) {
    }
}
