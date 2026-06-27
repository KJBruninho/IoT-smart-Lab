package com.iotroom.iotroom.service.leitura;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.iotroom.iotroom.dto.leitura.GraficoLeituraDTO;
import com.iotroom.iotroom.dto.leitura.LeituraCacheDTO;
import com.iotroom.iotroom.dto.leitura.LeituraEntradaDTO;
import com.iotroom.iotroom.dto.leitura.UltimaLeituraDTO;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class LocalSqliteCacheService {

    @Value("${cache.sqlite.path:/var/lib/iot-room/cache.db}")
    private String sqlitePath;

    private final DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private String dbUrl() {
        return "jdbc:sqlite:" + sqlitePath;
    }

    @PostConstruct
    public void init() {
        try {
            Path path = Path.of(sqlitePath).toAbsolutePath();
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (Connection conn = DriverManager.getConnection(dbUrl());
                 Statement stmt = conn.createStatement()) {

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS cache_leituras (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        device_id TEXT NOT NULL,
                        tipo_sensor TEXT NOT NULL,
                        valor TEXT NOT NULL,
                        unidade TEXT NOT NULL,
                        data_registo TEXT NOT NULL,
                        sincronizado INTEGER NOT NULL DEFAULT 0,
                        criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                """);

                stmt.execute("""
                    CREATE INDEX IF NOT EXISTS idx_cache_leituras_sync
                    ON cache_leituras(sincronizado, id)
                """);

                stmt.execute("""
                    CREATE INDEX IF NOT EXISTS idx_cache_leituras_tipo_data
                    ON cache_leituras(tipo_sensor, data_registo)
                """);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao inicializar cache SQLite local", e);
        }
    }

    public void guardar(LeituraEntradaDTO dto) {
        String sql = """
            INSERT INTO cache_leituras
            (device_id, tipo_sensor, valor, unidade, data_registo, sincronizado)
            VALUES (?, ?, ?, ?, ?, 0)
        """;

        try (Connection conn = DriverManager.getConnection(dbUrl());
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dto.deviceId());
            ps.setString(2, dto.tipoSensor());
            ps.setString(3, dto.valor().toPlainString());
            ps.setString(4, dto.unidade());
            ps.setString(5, dto.dataRegisto().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar leitura em SQLite local", e);
        }
    }

    public List<LeituraCacheDTO> listarPendentes(int limite) {
        List<LeituraCacheDTO> leituras = new ArrayList<>();

        String sql = """
            SELECT id, device_id, tipo_sensor, valor, unidade, data_registo
            FROM cache_leituras
            WHERE sincronizado = 0
            ORDER BY id ASC
            LIMIT ?
        """;

        try (Connection conn = DriverManager.getConnection(dbUrl());
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    leituras.add(new LeituraCacheDTO(
                            rs.getLong("id"),
                            mapLeituraEntrada(rs)
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar cache SQLite local", e);
        }

        return leituras;
    }

    public Optional<UltimaLeituraDTO> ultimaPorTipo(String tipoSensor) {
        String sql = """
            SELECT tipo_sensor, valor, unidade, data_registo
            FROM cache_leituras
            WHERE tipo_sensor = ?
            ORDER BY data_registo DESC, id DESC
            LIMIT 1
        """;

        try (Connection conn = DriverManager.getConnection(dbUrl());
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tipoSensor);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(new UltimaLeituraDTO(
                        rs.getString("tipo_sensor"),
                        new BigDecimal(rs.getString("valor")),
                        rs.getString("unidade"),
                        LocalDateTime.parse(rs.getString("data_registo"))
                ));
            }
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    public List<GraficoLeituraDTO> graficoPorTipo(String tipoSensor, int limite) {
        List<GraficoLeituraDTO> leituras = new ArrayList<>();

        String sql = """
            SELECT valor, data_registo
            FROM cache_leituras
            WHERE tipo_sensor = ?
            ORDER BY data_registo DESC, id DESC
            LIMIT ?
        """;

        try (Connection conn = DriverManager.getConnection(dbUrl());
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tipoSensor);
            ps.setInt(2, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDateTime data = LocalDateTime.parse(rs.getString("data_registo"));
                    leituras.add(new GraficoLeituraDTO(
                            data.format(horaFormatter),
                            new BigDecimal(rs.getString("valor"))
                    ));
                }
            }
        } catch (SQLException e) {
            return List.of();
        }

        Collections.reverse(leituras);
        return leituras;
    }

    public long contarPendentes() {
        String sql = "SELECT COUNT(*) FROM cache_leituras WHERE sincronizado = 0";

        try (Connection conn = DriverManager.getConnection(dbUrl());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            return -1;
        }
    }

    public void marcarComoSincronizada(long id) {
        String sql = "UPDATE cache_leituras SET sincronizado = 1 WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl());
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar cache SQLite local", e);
        }
    }

    private LeituraEntradaDTO mapLeituraEntrada(ResultSet rs) throws SQLException {
        return new LeituraEntradaDTO(
                rs.getString("device_id"),
                rs.getString("tipo_sensor"),
                new BigDecimal(rs.getString("valor")),
                rs.getString("unidade"),
                LocalDateTime.parse(rs.getString("data_registo"))
        );
    }
}
