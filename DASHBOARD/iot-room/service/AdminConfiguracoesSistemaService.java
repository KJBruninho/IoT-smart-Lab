package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.AdminConfiguracoesSistemaForm;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminConfiguracoesSistemaService {

    private final JdbcTemplate jdbcTemplate;

    public AdminConfiguracoesSistemaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AdminConfiguracoesSistemaForm obterConfiguracoes() {
        garantirConfiguracoesPadrao();

        AdminConfiguracoesSistemaForm form = new AdminConfiguracoesSistemaForm();

        form.setModoManutencao(getBoolean("modo_manutencao", false));
        form.setPermitirCalibracaoProfessor(getBoolean("permitir_calibracao_professor", true));
        form.setPermitirControloRemotoProfessor(getBoolean("permitir_controlo_remoto_professor", true));
        form.setPermitirPedidosIntervalo(getBoolean("permitir_pedidos_intervalo", true));

        form.setIntervaloMinimoMs(getInteger("intervalo_minimo_ms", 1000));
        form.setIntervaloMaximoMs(getInteger("intervalo_maximo_ms", 300000));
        form.setIntervaloRapidoPadraoMs(getInteger("intervalo_rapido_padrao_ms", 1000));
        form.setIntervaloEstavelPadraoMs(getInteger("intervalo_estavel_padrao_ms", 30000));
        form.setDuracaoModoRapidoPadraoMs(getInteger("duracao_modo_rapido_padrao_ms", 120000));

        form.setRetencaoLeiturasDias(getInteger("retencao_leituras_dias", 365));
        form.setRetencaoLogsDias(getInteger("retencao_logs_dias", 180));

        form.setMqttHost(getString("mqtt_host", "192.168.4.1"));
        form.setMqttPorta(getInteger("mqtt_porta", 1883));
        form.setMqttTopicoBase(getString("mqtt_topico_base", "esp32"));

        form.setTimeoutSemComunicacaoSegundos(getInteger("timeout_sem_comunicacao_segundos", 120));

        return form;
    }

    @Transactional
    public void guardar(AdminConfiguracoesSistemaForm form) {
        validar(form);

        upsert("modo_manutencao", bool(form.isModoManutencao()));
        upsert("permitir_calibracao_professor", bool(form.isPermitirCalibracaoProfessor()));
        upsert("permitir_controlo_remoto_professor", bool(form.isPermitirControloRemotoProfessor()));
        upsert("permitir_pedidos_intervalo", bool(form.isPermitirPedidosIntervalo()));

        upsert("intervalo_minimo_ms", form.getIntervaloMinimoMs());
        upsert("intervalo_maximo_ms", form.getIntervaloMaximoMs());
        upsert("intervalo_rapido_padrao_ms", form.getIntervaloRapidoPadraoMs());
        upsert("intervalo_estavel_padrao_ms", form.getIntervaloEstavelPadraoMs());
        upsert("duracao_modo_rapido_padrao_ms", form.getDuracaoModoRapidoPadraoMs());

        upsert("retencao_leituras_dias", form.getRetencaoLeiturasDias());
        upsert("retencao_logs_dias", form.getRetencaoLogsDias());

        upsert("mqtt_host", form.getMqttHost());
        upsert("mqtt_porta", form.getMqttPorta());
        upsert("mqtt_topico_base", form.getMqttTopicoBase());

        upsert("timeout_sem_comunicacao_segundos", form.getTimeoutSemComunicacaoSegundos());
    }

    private void validar(AdminConfiguracoesSistemaForm form) {
        if (form.getIntervaloMinimoMs() == null || form.getIntervaloMinimoMs() < 500) {
            throw new IllegalArgumentException("O intervalo mínimo deve ser pelo menos 500 ms.");
        }

        if (form.getIntervaloMaximoMs() == null || form.getIntervaloMaximoMs() < form.getIntervaloMinimoMs()) {
            throw new IllegalArgumentException("O intervalo máximo deve ser maior ou igual ao intervalo mínimo.");
        }

        if (form.getIntervaloRapidoPadraoMs() == null || form.getIntervaloRapidoPadraoMs() < form.getIntervaloMinimoMs()) {
            throw new IllegalArgumentException("O intervalo rápido padrão é inválido.");
        }

        if (form.getIntervaloEstavelPadraoMs() == null || form.getIntervaloEstavelPadraoMs() < form.getIntervaloRapidoPadraoMs()) {
            throw new IllegalArgumentException("O intervalo estável padrão deve ser maior ou igual ao intervalo rápido.");
        }

        if (form.getDuracaoModoRapidoPadraoMs() == null || form.getDuracaoModoRapidoPadraoMs() < 1000) {
            throw new IllegalArgumentException("A duração do modo rápido deve ser válida.");
        }

        if (form.getMqttHost() == null || form.getMqttHost().isBlank()) {
            throw new IllegalArgumentException("O host MQTT é obrigatório.");
        }

        if (form.getMqttPorta() == null || form.getMqttPorta() < 1 || form.getMqttPorta() > 65535) {
            throw new IllegalArgumentException("A porta MQTT é inválida.");
        }

        if (form.getMqttTopicoBase() == null || form.getMqttTopicoBase().isBlank()) {
            throw new IllegalArgumentException("O tópico base MQTT é obrigatório.");
        }

        if (form.getTimeoutSemComunicacaoSegundos() == null || form.getTimeoutSemComunicacaoSegundos() < 10) {
            throw new IllegalArgumentException("O timeout sem comunicação deve ser pelo menos 10 segundos.");
        }
    }

    private void garantirConfiguracoesPadrao() {
        inserirSeNaoExiste("modo_manutencao", "false");
        inserirSeNaoExiste("permitir_calibracao_professor", "true");
        inserirSeNaoExiste("permitir_controlo_remoto_professor", "true");
        inserirSeNaoExiste("permitir_pedidos_intervalo", "true");

        inserirSeNaoExiste("intervalo_minimo_ms", "1000");
        inserirSeNaoExiste("intervalo_maximo_ms", "300000");
        inserirSeNaoExiste("intervalo_rapido_padrao_ms", "1000");
        inserirSeNaoExiste("intervalo_estavel_padrao_ms", "30000");
        inserirSeNaoExiste("duracao_modo_rapido_padrao_ms", "120000");

        inserirSeNaoExiste("retencao_leituras_dias", "365");
        inserirSeNaoExiste("retencao_logs_dias", "180");

        inserirSeNaoExiste("mqtt_host", "192.168.4.1");
        inserirSeNaoExiste("mqtt_porta", "1883");
        inserirSeNaoExiste("mqtt_topico_base", "esp32");

        inserirSeNaoExiste("timeout_sem_comunicacao_segundos", "120");
    }

    private void inserirSeNaoExiste(String chave, String valor) {
        jdbcTemplate.update("""
                INSERT INTO configuracoes_sistema (chave, valor, descricao, atualizado_em)
                SELECT ?, ?, ?, CURRENT_TIMESTAMP
                WHERE NOT EXISTS (
                    SELECT 1 FROM configuracoes_sistema WHERE chave = ?
                )
                """,
                chave,
                valor,
                chave,
                chave
        );
    }

    private void upsert(String chave, Object valor) {
        jdbcTemplate.update("""
                INSERT INTO configuracoes_sistema (chave, valor, descricao, atualizado_em)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                    valor = VALUES(valor),
                    atualizado_em = CURRENT_TIMESTAMP
                """,
                chave,
                String.valueOf(valor),
                chave
        );
    }

    private String getString(String chave, String fallback) {
        try {
            return jdbcTemplate.query("""
                            SELECT valor
                            FROM configuracoes_sistema
                            WHERE chave = ?
                            """,
                            (rs, rowNum) -> rs.getString("valor"),
                            chave
                    )
                    .stream()
                    .findFirst()
                    .orElse(fallback);
        } catch (Exception e) {
            return fallback;
        }
    }

    private Integer getInteger(String chave, Integer fallback) {
        try {
            return Integer.parseInt(getString(chave, String.valueOf(fallback)));
        } catch (Exception e) {
            return fallback;
        }
    }

    private boolean getBoolean(String chave, boolean fallback) {
        String valor = getString(chave, String.valueOf(fallback));

        return "true".equalsIgnoreCase(valor)
                || "1".equals(valor)
                || "sim".equalsIgnoreCase(valor)
                || "yes".equalsIgnoreCase(valor);
    }

    private String bool(boolean value) {
        return value ? "true" : "false";
    }
}