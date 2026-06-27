package com.iotroom.iotroom.controller.professor;

import com.iotroom.iotroom.dto.professor.AvisoFormDTO;
import com.iotroom.iotroom.dto.professor.ExperienciaEstacaoFormDTO;
import com.iotroom.iotroom.dto.professor.ExperienciaFormDTO;
import com.iotroom.iotroom.dto.professor.ForumRespostaFormDTO;
import com.iotroom.iotroom.dto.professor.ForumTopicoFormDTO;
import com.iotroom.iotroom.dto.professor.GrupoFormDTO;
import com.iotroom.iotroom.dto.professor.GrupoMembroFormDTO;
import com.iotroom.iotroom.dto.professor.ProfessorDashboardResumoDTO;
import com.iotroom.iotroom.dto.professor.RegraAlertaSensorFormDTO;
import com.iotroom.iotroom.dto.professor.SensorModoFormDTO;
import com.iotroom.iotroom.model.Aviso;
import com.iotroom.iotroom.model.Experiencia;
import com.iotroom.iotroom.model.ForumTopico;
import com.iotroom.iotroom.model.Grupo;
import com.iotroom.iotroom.model.RegraAlertaSensor;
import com.iotroom.iotroom.model.Sensor;
import com.iotroom.iotroom.security.AuthenticatedUser;
import org.springframework.security.access.AccessDeniedException;

import com.iotroom.iotroom.service.professor.ProfessorAlertaService;
import com.iotroom.iotroom.service.professor.ProfessorAvisoService;
import com.iotroom.iotroom.service.professor.ProfessorComandoSensorService;
import com.iotroom.iotroom.service.professor.ProfessorDashboardService;
import com.iotroom.iotroom.service.professor.ProfessorExperienciaService;
import com.iotroom.iotroom.service.professor.ProfessorForumService;
import com.iotroom.iotroom.service.professor.ProfessorGrupoMembroService;
import com.iotroom.iotroom.service.professor.ProfessorGrupoService;
import com.iotroom.iotroom.service.sensor.PermissaoGrupoEstacaoService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class ProfessorController {

    private final ProfessorDashboardService professorDashboardService;
    private final ProfessorAlertaService professorAlertaService;
    private final ProfessorAvisoService professorAvisoService;
    private final ProfessorComandoSensorService professorComandoSensorService;
    private final ProfessorExperienciaService professorExperienciaService;
    private final ProfessorForumService professorForumService;
    private final ProfessorGrupoMembroService professorGrupoMembroService;
    private final ProfessorGrupoService professorGrupoService;
    private final PermissaoGrupoEstacaoService permissaoGrupoEstacaoService;

    public ProfessorController(
            ProfessorDashboardService professorDashboardService,
            ProfessorAlertaService professorAlertaService,
            ProfessorAvisoService professorAvisoService,
            ProfessorComandoSensorService professorComandoSensorService,
            ProfessorExperienciaService professorExperienciaService,
            ProfessorForumService professorForumService,
            ProfessorGrupoMembroService professorGrupoMembroService,
            ProfessorGrupoService professorGrupoService,
            PermissaoGrupoEstacaoService permissaoGrupoEstacaoService
    ) {
        this.professorDashboardService = professorDashboardService;
        this.professorAlertaService = professorAlertaService;
        this.professorAvisoService = professorAvisoService;
        this.professorComandoSensorService = professorComandoSensorService;
        this.professorExperienciaService = professorExperienciaService;
        this.professorForumService = professorForumService;
        this.professorGrupoMembroService = professorGrupoMembroService;
        this.professorGrupoService = professorGrupoService;
        this.permissaoGrupoEstacaoService = permissaoGrupoEstacaoService;
    }

    @GetMapping("/professor")
    public String dashboardProfessor(Model model, Authentication authentication) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        ProfessorDashboardResumoDTO dashboard =
                professorDashboardService.obterDashboard(acesso.id(), acesso.admin());

        model.addAttribute("dashboard", dashboard);
        model.addAttribute("paginaAtual", "dashboard");

        return "professor/index";
    }

    @GetMapping("/professor/alertas")
    public String alertasIndex(Model model, Authentication authentication) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        model.addAttribute("alertas", professorAlertaService.listarAlertas(acesso.id(), acesso.admin()));
        model.addAttribute("paginaAtual", "alertas");

        return "professor/alertas/index";
    }

    @GetMapping("/professor/alertas/regras")
    public String alertasRegras(Model model, Authentication authentication) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        model.addAttribute("regras", professorAlertaService.listarRegras(acesso.id(), acesso.admin()));
        model.addAttribute("paginaAtual", "alertas");

        return "professor/alertas/regras";
    }

    @GetMapping("/professor/alertas/regras/nova")
    public String alertasNovaRegra(Model model, Authentication authentication) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        prepararFormularioAlertas(model, acesso);
        model.addAttribute("regraForm", professorAlertaService.criarFormVazio());

        return "professor/alertas/nova-regra";
    }

    @PostMapping("/professor/alertas/regras/nova")
    public String alertasCriarRegra(
            @ModelAttribute("regraForm") RegraAlertaSensorFormDTO regraForm,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorAlertaService.criarRegra(acesso.id(), acesso.admin(), regraForm);

        return "redirect:/professor/alertas/regras";
    }

    @GetMapping("/professor/alertas/regras/{id}/editar")
    public String alertasEditarRegra(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        RegraAlertaSensor regra = professorAlertaService.obterRegra(id, acesso.id(), acesso.admin());

        prepararFormularioAlertas(model, acesso);
        model.addAttribute("regraForm", professorAlertaService.criarFormAPartirDeRegra(regra));

        return "professor/alertas/editar-regra";
    }

    @PostMapping("/professor/alertas/regras/{id}/editar")
    public String alertasAtualizarRegra(
            @PathVariable Long id,
            @ModelAttribute("regraForm") RegraAlertaSensorFormDTO regraForm,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorAlertaService.atualizarRegra(id, acesso.id(), acesso.admin(), regraForm);

        return "redirect:/professor/alertas/regras";
    }

    @PostMapping("/professor/alertas/regras/{id}/alternar-estado")
    public String alertasAlternarEstadoRegra(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorAlertaService.alternarEstadoRegra(id, acesso.id(), acesso.admin());

        return "redirect:/professor/alertas/regras";
    }

    @PostMapping("/professor/alertas/{id}/marcar-lido")
    public String alertasMarcarLido(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorAlertaService.marcarLido(id, acesso.id(), acesso.admin());

        return "redirect:/professor/alertas";
    }

    @PostMapping("/professor/alertas/{id}/resolver")
    public String alertasResolver(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorAlertaService.resolver(id, acesso.id(), acesso.admin());

        return "redirect:/professor/alertas";
    }

    @PostMapping("/professor/alertas/{id}/ignorar")
    public String alertasIgnorar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorAlertaService.ignorar(id, acesso.id(), acesso.admin());

        return "redirect:/professor/alertas";
    }

    @GetMapping("/professor/avisos")
    public String avisosIndex(Model model, Authentication authentication) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        List<Aviso> avisos = professorAvisoService.listarAvisosDoProfessor(acesso.id(), acesso.admin());

        model.addAttribute("avisos", avisos);
        model.addAttribute("paginaAtual", "avisos");

        return "professor/avisos/index";
    }

    @GetMapping("/professor/avisos/novo")
    public String avisosNovo(Model model, Authentication authentication) {
        obterAcessoProfessor(authentication);

        model.addAttribute("avisoForm", professorAvisoService.criarFormVazio());
        model.addAttribute("paginaAtual", "avisos");

        return "professor/avisos/novo";
    }

    @PostMapping("/professor/avisos/novo")
    public String avisosCriar(
            @ModelAttribute("avisoForm") AvisoFormDTO avisoForm,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        Aviso avisoCriado = professorAvisoService.criarAviso(acesso.id(), acesso.admin(), avisoForm);

        return "redirect:/professor/avisos/" + avisoCriado.getId();
    }

    @GetMapping("/professor/avisos/{id}")
    public String avisosVer(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        Aviso aviso = professorAvisoService.obterAvisoDoProfessor(id, acesso.id(), acesso.admin());

        model.addAttribute("aviso", aviso);
        model.addAttribute("paginaAtual", "avisos");

        return "professor/avisos/ver";
    }

    @GetMapping("/professor/avisos/{id}/editar")
    public String avisosEditar(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        Aviso aviso = professorAvisoService.obterAvisoDoProfessor(id, acesso.id(), acesso.admin());

        model.addAttribute("avisoForm", professorAvisoService.criarFormAPartirDeAviso(aviso));
        model.addAttribute("paginaAtual", "avisos");

        return "professor/avisos/editar";
    }

    @PostMapping("/professor/avisos/{id}/editar")
    public String avisosAtualizar(
            @PathVariable Long id,
            @ModelAttribute("avisoForm") AvisoFormDTO avisoForm,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorAvisoService.atualizarAviso(id, acesso.id(), acesso.admin(), avisoForm);

        return "redirect:/professor/avisos/" + id;
    }

    @PostMapping("/professor/avisos/{id}/alternar-estado")
    public String avisosAlternarEstado(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorAvisoService.alternarEstado(id, acesso.id(), acesso.admin());

        return "redirect:/professor/avisos";
    }

    @GetMapping("/professor/sensores")
    public String sensoresIndex(Model model, Authentication authentication) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        model.addAttribute("sensores", professorComandoSensorService.listarSensores(acesso.id(), acesso.admin()));
        model.addAttribute("paginaAtual", "sensores");

        return "professor/sensores/index";
    }

    @GetMapping("/professor/sensores/pedidos")
    public String sensoresPedidos(Model model, Authentication authentication) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        model.addAttribute("pedidos", professorComandoSensorService.listarPedidosPendentes(acesso.id(), acesso.admin()));
        model.addAttribute("paginaAtual", "sensores");

        return "professor/sensores/pedidos";
    }

    @GetMapping("/professor/sensores/{sensorId}")
    public String sensoresVer(
            @PathVariable Long sensorId,
            Model model,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        Sensor sensor = professorComandoSensorService.obterSensor(sensorId, acesso.id(), acesso.admin());

        model.addAttribute("sensor", sensor);
        model.addAttribute("configuracao", professorComandoSensorService.obterOuCriarConfiguracao(sensorId, acesso.id(), acesso.admin()));
        model.addAttribute("modoForm", professorComandoSensorService.criarFormModo(sensorId, acesso.id(), acesso.admin()));
        model.addAttribute("comandosRecentes", professorComandoSensorService.listarComandosRecentes(sensorId, acesso.id(), acesso.admin()));
        model.addAttribute("pedidosSensor", professorComandoSensorService.listarPedidosDoSensor(sensorId, acesso.id(), acesso.admin()));
        model.addAttribute("paginaAtual", "sensores");

        return "professor/sensores/ver";
    }

    @PostMapping("/professor/sensores/{sensorId}/configuracao")
    public String sensoresGuardarConfiguracaoModo(
            @PathVariable Long sensorId,
            @ModelAttribute("modoForm") SensorModoFormDTO modoForm,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorComandoSensorService.guardarConfiguracaoModo(sensorId, acesso.id(), acesso.admin(), modoForm);

        return "redirect:/professor/sensores/" + sensorId;
    }

    @PostMapping("/professor/sensores/{sensorId}/calibracao")
    public String sensoresEnviarFatorCalibracao(
            @PathVariable Long sensorId,
            @RequestParam BigDecimal fator,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorComandoSensorService.enviarFatorCalibracao(sensorId, acesso.id(), acesso.admin(), fator);

        return "redirect:/professor/sensores/" + sensorId;
    }

    @PostMapping("/professor/sensores/{sensorId}/ph-offset")
    public String sensoresEnviarOffsetPh(
            @PathVariable Long sensorId,
            @RequestParam BigDecimal offset,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorComandoSensorService.enviarOffsetPh(sensorId, acesso.id(), acesso.admin(), offset);

        return "redirect:/professor/sensores/" + sensorId;
    }

    @PostMapping("/professor/sensores/{sensorId}/ligar")
    public String sensoresLigarSensor(
            @PathVariable Long sensorId,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorComandoSensorService.ligarSensor(sensorId, acesso.id(), acesso.admin());

        return "redirect:/professor/sensores/" + sensorId;
    }

    @PostMapping("/professor/sensores/{sensorId}/desligar")
    public String sensoresDesligarSensor(
            @PathVariable Long sensorId,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorComandoSensorService.desligarSensor(sensorId, acesso.id(), acesso.admin());

        return "redirect:/professor/sensores/" + sensorId;
    }

    @PostMapping("/professor/sensores/pedidos/{pedidoId}/aprovar")
    public String sensoresAprovarPedido(
            @PathVariable Long pedidoId,
            @RequestParam(required = false) String respostaProfessor,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorComandoSensorService.aprovarPedido(pedidoId, acesso.id(), acesso.admin(), respostaProfessor);

        return "redirect:/professor/sensores/pedidos";
    }

    @PostMapping("/professor/sensores/pedidos/{pedidoId}/rejeitar")
    public String sensoresRejeitarPedido(
            @PathVariable Long pedidoId,
            @RequestParam(required = false) String respostaProfessor,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorComandoSensorService.rejeitarPedido(pedidoId, acesso.id(), acesso.admin(), respostaProfessor);

        return "redirect:/professor/sensores/pedidos";
    }

    @GetMapping("/professor/experiencias")
    public String experienciasIndex(Model model, Authentication authentication) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        List<Experiencia> experiencias = professorExperienciaService.listarExperienciasDoProfessor(acesso.id(), acesso.admin());

        model.addAttribute("experiencias", experiencias);
        model.addAttribute("nomesGrupos", professorExperienciaService.obterNomesDosGrupos(acesso.id(), acesso.admin()));
        model.addAttribute("paginaAtual", "experiencias");

        return "professor/experiencias/index";
    }

    @GetMapping("/professor/experiencias/nova")
    public String experienciasNova(Model model, Authentication authentication) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        model.addAttribute("experienciaForm", professorExperienciaService.criarFormVazio());
        model.addAttribute("grupos", professorExperienciaService.listarGruposDisponiveis(acesso.id(), acesso.admin()));
        model.addAttribute("paginaAtual", "experiencias");

        return "professor/experiencias/nova";
    }

    @PostMapping("/professor/experiencias/nova")
    public String experienciasCriar(
            @ModelAttribute("experienciaForm") ExperienciaFormDTO experienciaForm,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        Experiencia experienciaCriada = professorExperienciaService.criarExperiencia(acesso.id(), acesso.admin(), experienciaForm);

        return "redirect:/professor/experiencias/" + experienciaCriada.getId();
    }

    @GetMapping("/professor/experiencias/{id}")
    public String experienciasVer(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        Experiencia experiencia = professorExperienciaService.obterExperienciaDoProfessor(id, acesso.id(), acesso.admin());

        model.addAttribute("experiencia", experiencia);
        model.addAttribute("nomesGrupos", professorExperienciaService.obterNomesDosGrupos(acesso.id(), acesso.admin()));
        model.addAttribute("paginaAtual", "experiencias");
        model.addAttribute("estacoesAssociadas", professorExperienciaService.listarEstacoesDaExperiencia(id, acesso.id(), acesso.admin()));
        model.addAttribute("estacoesDisponiveis", professorExperienciaService.listarEstacoesDisponiveisParaExperiencia(id, acesso.id(), acesso.admin()));
        model.addAttribute("estacoesMap", professorExperienciaService.obterMapaEstacoes(id, acesso.id(), acesso.admin()));
        model.addAttribute("estacaoForm", new ExperienciaEstacaoFormDTO());

        return "professor/experiencias/ver";
    }

    @PostMapping("/professor/experiencias/{id}/estacoes/adicionar")
    public String experienciasAdicionarEstacao(
            @PathVariable Long id,
            @ModelAttribute("estacaoForm") ExperienciaEstacaoFormDTO estacaoForm,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorExperienciaService.associarEstacao(id, acesso.id(), acesso.admin(), estacaoForm);

        return "redirect:/professor/experiencias/" + id;
    }

    @PostMapping("/professor/experiencias/{id}/estacoes/{estacaoId}/remover")
    public String experienciasRemoverEstacao(
            @PathVariable Long id,
            @PathVariable Long estacaoId,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorExperienciaService.removerEstacao(id, estacaoId, acesso.id(), acesso.admin());

        return "redirect:/professor/experiencias/" + id;
    }

    @GetMapping("/professor/experiencias/{id}/editar")
    public String experienciasEditar(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        Experiencia experiencia = professorExperienciaService.obterExperienciaDoProfessor(id, acesso.id(), acesso.admin());

        model.addAttribute("experienciaForm", professorExperienciaService.criarFormAPartirDeExperiencia(experiencia));
        model.addAttribute("grupos", professorExperienciaService.listarGruposDisponiveis(acesso.id(), acesso.admin()));
        model.addAttribute("paginaAtual", "experiencias");

        return "professor/experiencias/editar";
    }

    @PostMapping("/professor/experiencias/{id}/editar")
    public String experienciasAtualizar(
            @PathVariable Long id,
            @ModelAttribute("experienciaForm") ExperienciaFormDTO experienciaForm,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorExperienciaService.atualizarExperiencia(id, acesso.id(), acesso.admin(), experienciaForm);

        return "redirect:/professor/experiencias/" + id;
    }

    @PostMapping("/professor/experiencias/{id}/iniciar")
    public String experienciasIniciar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorExperienciaService.iniciarExperiencia(id, acesso.id(), acesso.admin());

        return "redirect:/professor/experiencias/" + id;
    }

    @PostMapping("/professor/experiencias/{id}/finalizar")
    public String experienciasFinalizar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorExperienciaService.finalizarExperiencia(id, acesso.id(), acesso.admin());

        return "redirect:/professor/experiencias/" + id;
    }

    @PostMapping("/professor/experiencias/{id}/cancelar")
    public String experienciasCancelar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorExperienciaService.cancelarExperiencia(id, acesso.id(), acesso.admin());

        return "redirect:/professor/experiencias/" + id;
    }

    @GetMapping("/professor/forum")
    public String forumIndex(Model model, Authentication authentication) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        model.addAttribute("topicos", professorForumService.listarTopicos(acesso.id(), acesso.admin()));
        model.addAttribute("nomesGrupos", professorForumService.obterNomesGrupos(acesso.id(), acesso.admin()));
        model.addAttribute("nomesExperiencias", professorForumService.obterNomesExperiencias(acesso.id(), acesso.admin()));
        model.addAttribute("paginaAtual", "forum");

        return "professor/forum/index";
    }

    @GetMapping("/professor/forum/novo")
    public String forumNovo(Model model, Authentication authentication) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        model.addAttribute("topicoForm", professorForumService.criarFormVazio());
        model.addAttribute("grupos", professorForumService.listarGrupos(acesso.id(), acesso.admin()));
        model.addAttribute("experiencias", professorForumService.listarExperiencias(acesso.id(), acesso.admin()));
        model.addAttribute("paginaAtual", "forum");

        return "professor/forum/novo";
    }

    @PostMapping("/professor/forum/novo")
    public String forumCriar(
            @ModelAttribute("topicoForm") ForumTopicoFormDTO topicoForm,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        ForumTopico topico = professorForumService.criarTopico(acesso.id(), acesso.admin(), topicoForm);

        return "redirect:/professor/forum/" + topico.getId();
    }

    @GetMapping("/professor/forum/{id}")
    public String forumVer(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        ForumTopico topico = professorForumService.obterTopico(id, acesso.id(), acesso.admin());

        model.addAttribute("topico", topico);
        model.addAttribute("respostas", professorForumService.listarRespostas(id));
        model.addAttribute("respostaForm", new ForumRespostaFormDTO());
        model.addAttribute("nomesGrupos", professorForumService.obterNomesGrupos(acesso.id(), acesso.admin()));
        model.addAttribute("nomesExperiencias", professorForumService.obterNomesExperiencias(acesso.id(), acesso.admin()));
        model.addAttribute("paginaAtual", "forum");

        return "professor/forum/ver";
    }

    @GetMapping("/professor/forum/{id}/editar")
    public String forumEditar(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        ForumTopico topico = professorForumService.obterTopico(id, acesso.id(), acesso.admin());

        model.addAttribute("topicoForm", professorForumService.criarFormAPartirDeTopico(topico));
        model.addAttribute("grupos", professorForumService.listarGrupos(acesso.id(), acesso.admin()));
        model.addAttribute("experiencias", professorForumService.listarExperiencias(acesso.id(), acesso.admin()));
        model.addAttribute("paginaAtual", "forum");

        return "professor/forum/editar";
    }

    @PostMapping("/professor/forum/{id}/editar")
    public String forumAtualizar(
            @PathVariable Long id,
            @ModelAttribute("topicoForm") ForumTopicoFormDTO topicoForm,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorForumService.atualizarTopico(id, acesso.id(), acesso.admin(), topicoForm);

        return "redirect:/professor/forum/" + id;
    }

    @PostMapping("/professor/forum/{id}/responder")
    public String forumResponder(
            @PathVariable Long id,
            @ModelAttribute("respostaForm") ForumRespostaFormDTO respostaForm,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorForumService.responder(id, acesso.id(), acesso.admin(), respostaForm);

        return "redirect:/professor/forum/" + id;
    }

    @PostMapping("/professor/forum/{id}/fechar")
    public String forumFechar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorForumService.fecharTopico(id, acesso.id(), acesso.admin());

        return "redirect:/professor/forum/" + id;
    }

    @PostMapping("/professor/forum/{id}/reabrir")
    public String forumReabrir(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorForumService.reabrirTopico(id, acesso.id(), acesso.admin());

        return "redirect:/professor/forum/" + id;
    }

    @GetMapping("/professor/grupos")
    public String gruposIndex(Model model, Authentication authentication) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        List<Grupo> grupos = professorGrupoService.listarGruposDoProfessor(acesso.id(), acesso.admin());

        model.addAttribute("grupos", grupos);
        model.addAttribute("paginaAtual", "grupos");

        return "professor/grupos/index";
    }

    @GetMapping("/professor/grupos/novo")
    public String gruposNovo(Model model, Authentication authentication) {
        obterAcessoProfessor(authentication);

        model.addAttribute("grupoForm", professorGrupoService.criarFormVazio());
        model.addAttribute("paginaAtual", "grupos");

        return "professor/grupos/novo";
    }

    @PostMapping("/professor/grupos/novo")
    public String gruposCriar(
            @ModelAttribute("grupoForm") GrupoFormDTO grupoForm,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        Grupo grupoCriado = professorGrupoService.criarGrupo(acesso.id(), acesso.admin(), grupoForm);

        return "redirect:/professor/grupos/" + grupoCriado.getId();
    }

    @GetMapping("/professor/grupos/{id}")
    public String gruposVer(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        Grupo grupo = professorGrupoService.obterGrupoDoProfessor(id, acesso.id(), acesso.admin());

        model.addAttribute("grupo", grupo);
        model.addAttribute("paginaAtual", "grupos");
        model.addAttribute("membros", professorGrupoMembroService.listarMembros(id, acesso.id(), acesso.admin()));
        model.addAttribute("utilizadoresDisponiveis", professorGrupoMembroService.listarUtilizadoresDisponiveis(id, acesso.id(), acesso.admin()));
        model.addAttribute("rolesGrupo", professorGrupoMembroService.listarRoles());
        model.addAttribute("membroForm", professorGrupoMembroService.criarFormVazio());

        return "professor/grupos/ver";
    }

    @GetMapping("/professor/grupos/{id}/editar")
    public String gruposEditar(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        Grupo grupo = professorGrupoService.obterGrupoDoProfessor(id, acesso.id(), acesso.admin());

        model.addAttribute("grupoForm", professorGrupoService.criarFormAPartirDeGrupo(grupo));
        model.addAttribute("paginaAtual", "grupos");

        return "professor/grupos/editar";
    }

    @PostMapping("/professor/grupos/{id}/editar")
    public String gruposAtualizar(
            @PathVariable Long id,
            @ModelAttribute("grupoForm") GrupoFormDTO grupoForm,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorGrupoService.atualizarGrupo(id, acesso.id(), acesso.admin(), grupoForm);

        return "redirect:/professor/grupos/" + id;
    }

    @PostMapping("/professor/grupos/{id}/alternar-estado")
    public String gruposAlternarEstado(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorGrupoService.alternarEstado(id, acesso.id(), acesso.admin());

        return "redirect:/professor/grupos";
    }

    @GetMapping("/professor/grupos/{id}/estacoes")
    public String gruposGerirEstacoes(
            @PathVariable Long id,
            Model model,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        Grupo grupo = professorGrupoService.obterGrupoDoProfessor(id, acesso.id(), acesso.admin());

        model.addAttribute("grupo", grupo);
        model.addAttribute("estacoes", permissaoGrupoEstacaoService.listarEstacoesAtivas());
        model.addAttribute("estacoesComAcesso", permissaoGrupoEstacaoService.listarIdsEstacoesComAcesso(id));
        model.addAttribute("paginaAtual", "grupos");

        return "professor/grupos/estacoes";
    }

    @PostMapping("/professor/grupos/{id}/estacoes")
    public String gruposGuardarEstacoes(
            @PathVariable Long id,
            @RequestParam(required = false) List<Long> estacaoIds,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorGrupoService.obterGrupoDoProfessor(id, acesso.id(), acesso.admin());
        permissaoGrupoEstacaoService.atualizarEstacoesDoGrupo(id, estacaoIds);

        return "redirect:/professor/grupos/" + id + "/estacoes";
    }

    @PostMapping("/professor/grupos/{id}/membros/adicionar")
    public String gruposAdicionarMembro(
            @PathVariable Long id,
            @ModelAttribute("membroForm") GrupoMembroFormDTO membroForm,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorGrupoMembroService.adicionarMembro(id, acesso.id(), acesso.admin(), membroForm);

        return "redirect:/professor/grupos/" + id;
    }

    @PostMapping("/professor/grupos/{id}/membros/{utilizadorId}/alterar-role")
    public String gruposAlterarRoleMembro(
            @PathVariable Long id,
            @PathVariable Long utilizadorId,
            @RequestParam Long roleGrupoId,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorGrupoMembroService.alterarRole(id, utilizadorId, acesso.id(), acesso.admin(), roleGrupoId);

        return "redirect:/professor/grupos/" + id;
    }

    @PostMapping("/professor/grupos/{id}/membros/{utilizadorId}/remover")
    public String gruposRemoverMembro(
            @PathVariable Long id,
            @PathVariable Long utilizadorId,
            Authentication authentication
    ) {
        AcessoProfessor acesso = obterAcessoProfessor(authentication);

        professorGrupoMembroService.removerMembro(id, utilizadorId, acesso.id(), acesso.admin());

        return "redirect:/professor/grupos/" + id;
    }

    private void prepararFormularioAlertas(Model model, AcessoProfessor acesso) {
        model.addAttribute("grupos", professorAlertaService.listarGrupos(acesso.id(), acesso.admin()));
        model.addAttribute("experiencias", professorAlertaService.listarExperiencias(acesso.id(), acesso.admin()));
        model.addAttribute("estacoes", professorAlertaService.listarEstacoes(acesso.id(), acesso.admin()));
        model.addAttribute("paginaAtual", "alertas");
    }

    private AcessoProfessor obterAcessoProfessor(Authentication authentication) {
        AuthenticatedUser user = obterUtilizador(authentication);

        boolean admin = temAuthority(authentication, "ADMIN");
        boolean professor = temAuthority(authentication, "PROFESSOR");

        if (!admin && !professor) {
            throw new AccessDeniedException("Acesso reservado a professores ou administradores.");
        }

        return new AcessoProfessor(user.getId(), admin, professor);
    }

    private AuthenticatedUser obterUtilizador(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("Utilizador não autenticado.");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof AuthenticatedUser user)) {
            throw new IllegalStateException(
                    "Principal inválido. Esperado AuthenticatedUser, recebido: "
                            + principal.getClass().getName()
            );
        }

        return user;
    }

    private boolean temAuthority(Authentication authentication, String role) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> {
                    String valor = authority.getAuthority();
                    return valor.equals(role) || valor.equals("ROLE_" + role);
                });
    }

    private record AcessoProfessor(Long id, boolean admin, boolean professor) {
    }
}
