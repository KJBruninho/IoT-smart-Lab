package com.iotroom.assistant.service;

import com.iotroom.assistant.dto.AssistantChatRequest;
import com.iotroom.assistant.dto.AssistantChatResponse;
import com.iotroom.assistant.dto.AssistantFeedbackRequest;
import com.iotroom.assistant.dto.AuthUserDTO;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.*;

@Service
public class AssistantService {

    private final JdbcTemplate jdbcTemplate;
    private final DiagnosticService diagnosticService;

    public AssistantService(
            JdbcTemplate jdbcTemplate,
            DiagnosticService diagnosticService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.diagnosticService = diagnosticService;
    }

    public AssistantChatResponse chat(AssistantChatRequest request, Authentication authentication) {
        AuthUserDTO user = obterUser(authentication);

        String email = user != null ? user.email() : null;
        Long utilizadorId = user != null ? user.id() : diagnosticService.obterUtilizadorIdPorEmail(email);

        String role = user != null && user.role() != null && !user.role().isBlank()
                ? user.role().trim().toUpperCase()
                : obterRole(authentication, request.role());

        String paginaAtual = safe(request.paginaAtual());

        Long conversaId = criarConversa(utilizadorId, role, paginaAtual);
        guardarMensagem(conversaId, "USER", request.mensagem());

        Map<String, Object> diagnostico = diagnosticService.run(request, authentication);

        diagnostico.put("authUserId", utilizadorId);
        diagnostico.put("authEmail", email);
        diagnostico.put("authRole", role);

        RespostaGerada respostaGerada = gerarResposta(request, role, diagnostico);

        Long mensagemId = guardarMensagem(conversaId, "ASSISTENTE", respostaGerada.resposta());

        return new AssistantChatResponse(
                conversaId,
                mensagemId,
                respostaGerada.resposta(),
                respostaGerada.passos(),
                respostaGerada.gravidade(),
                respostaGerada.sugerirContactoProfessor(),
                diagnostico
        );
    }

    public void feedback(AssistantFeedbackRequest request) {
        jdbcTemplate.update("""
                INSERT INTO assistente_feedback (mensagem_id, util, comentario)
                VALUES (?, ?, ?)
                """,
                request.mensagemId(),
                request.util(),
                request.comentario()
        );
    }

    private AuthUserDTO obterUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthUserDTO user) {
            return user;
        }

        return null;
    }

    private RespostaGerada gerarResposta(
            AssistantChatRequest request,
            String role,
            Map<String, Object> diagnostico
    ) {
        String mensagem = normalizar(request.mensagem());

        if (contemAlguma(mensagem, "login", "entrar", "password", "senha", "token", "sessao", "sessão")) {
            return respostaLogin(role);
        }

        if (contemAlguma(mensagem, "dados", "leituras", "grafico", "gráfico", "sensor", "tds", "ph", "temperatura", "valores")) {
            return respostaDadosSensores(role, diagnostico);
        }

        if (contemAlguma(mensagem, "mqtt", "esp32", "offline", "online", "broker", "publicar", "topico", "tópico")) {
            return respostaMqtt();
        }

        if (contemAlguma(mensagem, "experiencia", "experiência", "ativa", "iniciar", "finalizar")) {
            return respostaExperiencia(role);
        }

        if (contemAlguma(mensagem, "grupo", "permissao", "permissão", "aluno", "professor", "acesso")) {
            return respostaPermissoes(role);
        }

        if (contemAlguma(mensagem, "calibracao", "calibração", "calibrar", "fator", "factor", "offset")) {
            return respostaCalibracao(role);
        }

        return new RespostaGerada(
                "Ainda não consegui identificar uma causa específica. Começa por confirmar permissões, experiência ativa, sensores e comunicação MQTT.",
                List.of(
                        "Confirma se estás autenticado com a conta correta.",
                        "Verifica se existe uma experiência ativa associada à estação.",
                        "Confirma se a estação e o sensor estão ativos.",
                        "Verifica se existem leituras recentes na base de dados.",
                        "Consulta os logs do Spring Boot e do broker MQTT se o problema continuar."
                ),
                "MEDIA",
                role.equals("ALUNO")
        );
    }

    private RespostaGerada respostaLogin(String role) {
        return new RespostaGerada(
                "O problema parece estar relacionado com autenticação. As causas mais prováveis são credenciais incorretas, conta inativa, token expirado ou endpoint errado.",
                List.of(
                        "Confirma se o email e a password estão corretos.",
                        "Verifica se o utilizador está ativo na tabela utilizadores.",
                        "Na app Android, confirma se estás a chamar /api/auth/login no Auth API.",
                        "Confirma se estás a enviar Authorization: Bearer <token> nas chamadas autenticadas.",
                        "Se o erro for 401, o problema está no token ou nas credenciais."
                ),
                "MEDIA",
                role.equals("ALUNO")
        );
    }

    private RespostaGerada respostaDadosSensores(String role, Map<String, Object> diagnostico) {
        List<String> passos = new ArrayList<>();

        passos.add("Confirma se a estação está ativa.");
        passos.add("Confirma se o sensor está ativo.");
        passos.add("Verifica se existe uma experiência ATIVA para a estação/grupo.");
        passos.add("Confirma se existem leituras recentes na tabela leituras_sensor.");
        passos.add("Se o valor for -9999, o sensor está desligado ou indisponível.");

        StringBuilder resposta = new StringBuilder();
        resposta.append("O problema parece estar relacionado com leituras de sensores. ");

        Object ultimaLeituraEncontrada = diagnostico.get("ultimaLeituraEncontrada");
        Object ultimaLeituraValor = diagnostico.get("ultimaLeituraValor");
        Object ultimaLeituraData = diagnostico.get("ultimaLeituraData");
        Object minutosDesdeUltimaLeitura = diagnostico.get("minutosDesdeUltimaLeitura");

        if (Boolean.TRUE.equals(ultimaLeituraEncontrada)) {
            resposta.append("Foi encontrada uma última leitura com valor ")
                    .append(ultimaLeituraValor)
                    .append(" em ")
                    .append(ultimaLeituraData)
                    .append(". ");

            if (minutosDesdeUltimaLeitura instanceof Long minutos && minutos > 10) {
                resposta.append("A leitura já tem mais de 10 minutos, por isso pode existir falha no ESP32, MQTT ou persistência na BD. ");
            }
        } else if (Boolean.FALSE.equals(ultimaLeituraEncontrada)) {
            resposta.append("Não foi encontrada nenhuma leitura com o contexto indicado. ");
        }

        if (Boolean.FALSE.equals(diagnostico.get("estacaoAtiva"))) {
            resposta.append("A estação indicada não parece estar ativa. ");
        }

        if (Boolean.FALSE.equals(diagnostico.get("sensorAtivo"))) {
            resposta.append("O sensor indicado não parece estar ativo. ");
        }

        if (Boolean.FALSE.equals(diagnostico.get("experienciaAtiva"))) {
            resposta.append("Não foi encontrada experiência ativa associada à estação. ");
        }

        return new RespostaGerada(
                resposta.toString(),
                passos,
                "MEDIA",
                role.equals("ALUNO")
        );
    }

    private RespostaGerada respostaMqtt() {
        return new RespostaGerada(
                "O problema parece estar relacionado com comunicação MQTT entre ESP32, broker e backend.",
                List.of(
                        "Testa no servidor: mosquitto_sub -h 127.0.0.1 -t 'esp32/#' -v",
                        "Confirma se o ESP32 está ligado ao Wi-Fi correto.",
                        "Confirma se o Mosquitto está ativo.",
                        "Confirma se o backend está subscrito ao broker correto.",
                        "Valida os tópicos: esp32/temperatura, esp32/tds e esp32/ph.",
                        "Se existirem leituras antigas mas nenhuma recente, o problema está no envio MQTT ou na subscrição."
                ),
                "ALTA",
                false
        );
    }

    private RespostaGerada respostaExperiencia(String role) {
        return new RespostaGerada(
                "O problema parece estar relacionado com experiências. No teu sistema, as leituras podem depender de uma experiência ativa associada ao grupo e à estação.",
                List.of(
                        "Verifica se a experiência está no estado ATIVA.",
                        "Confirma se a experiência pertence ao grupo correto.",
                        "Confirma se o grupo tem permissão para usar a estação.",
                        "Verifica se não existe conflito com outra experiência ativa.",
                        "Se as leituras forem recusadas, confirma a regra de experiência ativa no backend."
                ),
                "MEDIA",
                role.equals("ALUNO")
        );
    }

    private RespostaGerada respostaPermissoes(String role) {
        return new RespostaGerada(
                "O problema parece estar relacionado com permissões ou grupos. Um utilizador pode não ver dados se não estiver no grupo correto ou se o grupo não tiver acesso à estação.",
                List.of(
                        "Confirma se o utilizador pertence ao grupo correto.",
                        "Confirma se o grupo tem permissão ativa para a estação.",
                        "Se for aluno, confirma se o professor associou o grupo à experiência.",
                        "Se for professor, confirma se está a consultar o grupo correto.",
                        "Se for admin, confirma se a estação está ativa."
                ),
                "MEDIA",
                role.equals("ALUNO")
        );
    }

    private RespostaGerada respostaCalibracao(String role) {
        return new RespostaGerada(
                "O problema parece estar relacionado com calibração. Para TDS usa fator de calibração. Para pH podes precisar de fator e offset.",
                List.of(
                        "Confirma se o sensor está ativo.",
                        "Confirma se o comando foi criado na tabela comandos_sensor.",
                        "Confirma se o ESP32 recebeu o comando MQTT correto.",
                        "Confirma se o comando passou de PENDENTE para ENVIADO ou CONFIRMADO.",
                        "Se o valor continuar igual, valida o firmware do ESP32."
                ),
                "MEDIA",
                role.equals("ALUNO")
        );
    }

    private Long criarConversa(Long utilizadorId, String role, String paginaAtual) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement("""
                        INSERT INTO assistente_conversas (utilizador_id, role, pagina_atual)
                        VALUES (?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);

                if (utilizadorId == null) {
                    ps.setObject(1, null);
                } else {
                    ps.setLong(1, utilizadorId);
                }

                ps.setString(2, role);
                ps.setString(3, paginaAtual);

                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            return key != null ? key.longValue() : null;

        } catch (Exception e) {
            return null;
        }
    }

    private Long guardarMensagem(Long conversaId, String origem, String mensagem) {
        if (conversaId == null) {
            return null;
        }

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement("""
                        INSERT INTO assistente_mensagens (conversa_id, origem, mensagem)
                        VALUES (?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);

                ps.setLong(1, conversaId);
                ps.setString(2, origem);
                ps.setString(3, mensagem);

                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            return key != null ? key.longValue() : null;

        } catch (Exception e) {
            return null;
        }
    }

    private String obterRole(Authentication authentication, String fallback) {
        if (authentication != null && authentication.getAuthorities() != null) {
            Optional<String> role = authentication.getAuthorities()
                    .stream()
                    .map(Object::toString)
                    .filter(value -> value.startsWith("ROLE_"))
                    .map(value -> value.substring(5))
                    .findFirst();

            if (role.isPresent()) {
                return role.get().toUpperCase();
            }
        }

        if (fallback == null || fallback.isBlank()) {
            return "USER";
        }

        return fallback.trim().toUpperCase();
    }

    private boolean contemAlguma(String texto, String... termos) {
        for (String termo : termos) {
            if (texto.contains(normalizar(termo))) {
                return true;
            }
        }

        return false;
    }

    private String normalizar(String input) {
        if (input == null) {
            return "";
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        return normalized
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private record RespostaGerada(
            String resposta,
            List<String> passos,
            String gravidade,
            boolean sugerirContactoProfessor
    ) {
    }
}