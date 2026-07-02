package com.iotroom.iotroom.controller.api;

import com.iotroom.iotroom.dto.dashboard.DashboardEstadoDTO;
import com.iotroom.iotroom.dto.leitura.GraficoLeituraDTO;
import com.iotroom.iotroom.dto.leitura.UltimaLeituraDTO;
import com.iotroom.iotroom.service.leitura.LocalSqliteCacheService;
import com.iotroom.iotroom.service.mqtt.MqttStatusService;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final JdbcTemplate jdbcTemplate;
    private final MqttStatusService mqttStatusService;
    private final LocalSqliteCacheService localSqliteCacheService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ApiController(
            JdbcTemplate jdbcTemplate,
            MqttStatusService mqttStatusService,
            LocalSqliteCacheService localSqliteCacheService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.mqttStatusService = mqttStatusService;
        this.localSqliteCacheService = localSqliteCacheService;
    }

    @GetMapping("/temperatura")
    public List<GraficoLeituraDTO> temperatura() {
        return dadosPorTipo("TEMPERATURA");
    }

    @GetMapping("/tds")
    public List<GraficoLeituraDTO> tds() {
        return dadosPorTipo("TDS");
    }

    @GetMapping("/ph")
    public List<GraficoLeituraDTO> ph() {
        return dadosPorTipo("PH");
    }

    @GetMapping("/dashboard/estado")
    public DashboardEstadoDTO estadoDashboard() {
        try {
            List<UltimaLeituraDTO> ultimas = new ArrayList<>();

            buscarUltimaLeituraMysqlPorTipo("TEMPERATURA", "ºC", false)
                    .ifPresent(ultimas::add);

            buscarUltimaLeituraMysqlPorTipo("TDS", "ppm", false)
                    .ifPresent(ultimas::add);

            /*
             * O pH pode existir como sensor mas ainda não ter leituras.
             * Nesse caso devolvemos -9999 para a app o mostrar como desligado/sem leitura.
             */
            buscarUltimaLeituraMysqlPorTipo("PH", "pH", true)
                    .ifPresent(ultimas::add);

            return new DashboardEstadoDTO(
                    mqttStatusService.isOnline(),
                    "mysql",
                    localSqliteCacheService.contarPendentes(),
                    ultimas
            );

        } catch (Exception e) {
            List<UltimaLeituraDTO> ultimas = new ArrayList<>();

            localSqliteCacheService.ultimaPorTipo("TEMPERATURA")
                    .ifPresent(ultimas::add);

            localSqliteCacheService.ultimaPorTipo("TDS")
                    .ifPresent(ultimas::add);

            localSqliteCacheService.ultimaPorTipo("PH")
                    .ifPresentOrElse(
                            ultimas::add,
                            () -> ultimas.add(new UltimaLeituraDTO(
                                    "PH",
                                    BigDecimal.valueOf(-9999),
                                    "pH",
                                    null
                            ))
                    );

            return new DashboardEstadoDTO(
                    mqttStatusService.isOnline(),
                    "sqlite-local",
                    localSqliteCacheService.contarPendentes(),
                    ultimas
            );
        }
    }

    private List<GraficoLeituraDTO> dadosPorTipo(String tipo) {
        try {
            String sql = """
                    SELECT
                        l.valor AS valor,
                        l.data_registo AS data_registo
                    FROM leituras_sensor l
                    INNER JOIN sensores s ON s.id = l.sensor_id
                    WHERE s.tipo = ?
                    AND s.ativo = 1
                    AND l.valor <> -9999
                    ORDER BY l.data_registo DESC
                    LIMIT 30
                    """;

            List<GraficoLeituraDTO> leituras = jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> {
                        BigDecimal valor = rs.getBigDecimal("valor");

                        Timestamp timestamp = rs.getTimestamp("data_registo");
                        String hora = timestamp != null
                                ? timestamp.toLocalDateTime().format(formatter)
                                : "";

                        return new GraficoLeituraDTO(
                                hora,
                                valor
                        );
                    },
                    tipo
            );

            Collections.reverse(leituras);
            return leituras;

        } catch (Exception e) {
            return localSqliteCacheService.graficoPorTipo(tipo, 30);
        }
    }

    private Optional<UltimaLeituraDTO> buscarUltimaLeituraMysqlPorTipo(
            String tipo,
            String unidadeFallback,
            boolean devolverSentinelaSeSemLeitura
    ) {
        String sql = """
                SELECT
                    s.tipo AS tipo,
                    s.unidade AS unidade,
                    l.valor AS valor,
                    l.data_registo AS data_registo
                FROM leituras_sensor l
                INNER JOIN sensores s ON s.id = l.sensor_id
                WHERE s.tipo = ?
                AND s.ativo = 1
                ORDER BY l.data_registo DESC
                LIMIT 1
                """;

        List<UltimaLeituraDTO> leituras = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    String tipoSensor = rs.getString("tipo");
                    String unidade = rs.getString("unidade");

                    BigDecimal valor = rs.getBigDecimal("valor");

                    Timestamp timestamp = rs.getTimestamp("data_registo");
                    LocalDateTime dataRegisto = timestamp != null
                            ? timestamp.toLocalDateTime()
                            : null;

                    return new UltimaLeituraDTO(
                            tipoSensor,
                            valor,
                            unidade,
                            dataRegisto
                    );
                },
                tipo
        );

        if (!leituras.isEmpty()) {
            return Optional.of(leituras.get(0));
        }

        if (!devolverSentinelaSeSemLeitura) {
            return Optional.empty();
        }

        if (!existeSensorAtivoDoTipo(tipo)) {
            return Optional.empty();
        }

        return Optional.of(new UltimaLeituraDTO(
                tipo,
                BigDecimal.valueOf(-9999),
                unidadeFallback,
                null
        ));
    }

    private boolean existeSensorAtivoDoTipo(String tipo) {
        String sql = """
                SELECT COUNT(*)
                FROM sensores
                WHERE tipo = ?
                AND ativo = 1
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                tipo
        );

        return count != null && count > 0;
    }
}
