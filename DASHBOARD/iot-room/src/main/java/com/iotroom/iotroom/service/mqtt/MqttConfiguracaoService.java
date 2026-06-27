package com.iotroom.iotroom.service.mqtt;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class MqttConfiguracaoService {

    private final JdbcTemplate jdbcTemplate;

    public MqttConfiguracaoService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getHost() {
        return getString("mqtt_host", "localhost");
    }

    public int getPorta() {
        return getInt("mqtt_porta", 1883);
    }

    public String getTopicoBase() {
        String topico = getString("mqtt_topico_base", "esp32");

        if (topico.endsWith("/")) {
            return topico.substring(0, topico.length() - 1);
        }

        return topico;
    }

    public String getBrokerUrl() {
        return "tcp://" + getHost() + ":" + getPorta();
    }

    private String getString(String chave, String valorPadrao) {
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
                    .orElse(valorPadrao);
        } catch (Exception e) {
            return valorPadrao;
        }
    }

    private int getInt(String chave, int valorPadrao) {
        try {
            return Integer.parseInt(getString(chave, String.valueOf(valorPadrao)));
        } catch (Exception e) {
            return valorPadrao;
        }
    }
}