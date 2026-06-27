package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.dto.dashboard.DashboardAlertaProjection;
import com.iotroom.iotroom.dto.dashboard.DashboardEstacaoProjection;
import com.iotroom.iotroom.dto.dashboard.DashboardExperienciaProjection;
import com.iotroom.iotroom.dto.dashboard.DashboardLeituraProjection;
import com.iotroom.iotroom.model.LeituraSensor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface ProfessorDashboardRepository extends JpaRepository<LeituraSensor, Long> {

    @Query(value = """
            SELECT COUNT(DISTINCT s.id)
            FROM sensores s
            INNER JOIN estacoes e ON e.id = s.estacao_id
            LEFT JOIN permissoes_utilizador_estacao pue 
                ON pue.estacao_id = e.id
            WHERE s.ativo = TRUE
              AND e.ativa = TRUE
              AND (:admin = TRUE OR pue.utilizador_id = :utilizadorId)
            """, nativeQuery = true)
    long countSensoresAtivos(
            @Param("utilizadorId") Long utilizadorId,
            @Param("admin") boolean admin
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM (
                SELECT 
                    e.id, 
                    MAX(l.data_registo) AS ultima_leitura
                FROM estacoes e
                LEFT JOIN permissoes_utilizador_estacao pue 
                    ON pue.estacao_id = e.id
                LEFT JOIN sensores s 
                    ON s.estacao_id = e.id 
                   AND s.ativo = TRUE
                LEFT JOIN leituras_sensor l 
                    ON l.sensor_id = s.id
                WHERE e.ativa = TRUE
                  AND (:admin = TRUE OR pue.utilizador_id = :utilizadorId)
                GROUP BY e.id
                HAVING ultima_leitura >= DATE_SUB(NOW(), INTERVAL 2 MINUTE)
            ) x
            """, nativeQuery = true)
    long countEstacoesOnline(
            @Param("utilizadorId") Long utilizadorId,
            @Param("admin") boolean admin
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM alertas_sensor
            WHERE estado = 'NOVO'
              AND (:admin = TRUE OR professor_id = :utilizadorId)
            """, nativeQuery = true)
    long countAlertasNovos(
            @Param("utilizadorId") Long utilizadorId,
            @Param("admin") boolean admin
    );

    @Query(value = """
            SELECT MAX(l.data_registo)
            FROM leituras_sensor l
            INNER JOIN sensores s ON s.id = l.sensor_id
            INNER JOIN estacoes e ON e.id = s.estacao_id
            LEFT JOIN permissoes_utilizador_estacao pue 
                ON pue.estacao_id = e.id
            LEFT JOIN experiencias exp 
                ON exp.id = l.experiencia_id
            WHERE e.ativa = TRUE
              AND s.ativo = TRUE
              AND (:admin = TRUE OR pue.utilizador_id = :utilizadorId OR exp.criado_por = :utilizadorId)
            """, nativeQuery = true)
    Timestamp findUltimaLeituraEm(
            @Param("utilizadorId") Long utilizadorId,
            @Param("admin") boolean admin
    );

    @Query(value = """
            SELECT DISTINCT
                l.id AS leituraId,
                s.nome AS sensorNome,
                s.tipo AS tipoSensor,
                l.unidade AS unidade,
                l.valor AS valor,
                l.data_registo AS dataRegisto,
                e.nome AS estacaoNome,
                e.device_id AS deviceId,
                COALESCE(exp.nome, 'Sem experiência') AS experienciaNome
            FROM leituras_sensor l
            INNER JOIN sensores s ON s.id = l.sensor_id
            INNER JOIN estacoes e ON e.id = s.estacao_id
            LEFT JOIN experiencias exp 
                ON exp.id = l.experiencia_id
            LEFT JOIN permissoes_utilizador_estacao pue 
                ON pue.estacao_id = e.id
            WHERE e.ativa = TRUE
              AND s.ativo = TRUE
              AND (:admin = TRUE OR pue.utilizador_id = :utilizadorId OR exp.criado_por = :utilizadorId)
            ORDER BY l.data_registo DESC
            """, nativeQuery = true)
    List<DashboardLeituraProjection> findUltimasLeituras(
            @Param("utilizadorId") Long utilizadorId,
            @Param("admin") boolean admin,
            Pageable pageable
    );

    @Query(value = """
            SELECT
                id AS id,
                titulo AS titulo,
                mensagem AS mensagem,
                severidade AS severidade,
                estado AS estado,
                tipo_sensor AS tipoSensor,
                valor_lido AS valorLido,
                criado_em AS criadoEm
            FROM alertas_sensor
            WHERE (:admin = TRUE OR professor_id = :utilizadorId)
            ORDER BY criado_em DESC
            """, nativeQuery = true)
    List<DashboardAlertaProjection> findAlertasRecentes(
            @Param("utilizadorId") Long utilizadorId,
            @Param("admin") boolean admin,
            Pageable pageable
    );

    @Query(value = """
            SELECT
                exp.id AS experienciaId,
                exp.nome AS nome,
                g.nome AS grupoNome,
                exp.estado AS estado,
                MAX(l.data_registo) AS ultimaLeituraEm,
                COUNT(l.id) AS totalLeituras
            FROM experiencias exp
            INNER JOIN grupos g ON g.id = exp.grupo_id
            LEFT JOIN leituras_sensor l ON l.experiencia_id = exp.id
            WHERE (:admin = TRUE OR exp.criado_por = :utilizadorId)
              AND exp.estado IN ('CRIADA', 'ATIVA', 'EM_EXECUCAO')
            GROUP BY exp.id, exp.nome, g.nome, exp.estado, exp.data_inicio
            ORDER BY
                CASE exp.estado
                    WHEN 'ATIVA' THEN 1
                    WHEN 'EM_EXECUCAO' THEN 2
                    WHEN 'CRIADA' THEN 3
                    ELSE 4
                END,
                ultimaLeituraEm DESC,
                exp.data_inicio DESC
            """, nativeQuery = true)
    List<DashboardExperienciaProjection> findExperienciasAtivas(
            @Param("utilizadorId") Long utilizadorId,
            @Param("admin") boolean admin,
            Pageable pageable
    );

    @Query(value = """
            SELECT
                e.id AS estacaoId,
                e.nome AS estacaoNome,
                e.device_id AS deviceId,
                MAX(l.data_registo) AS ultimaLeituraEm,
                COUNT(DISTINCT s.id) AS totalSensores,
                GROUP_CONCAT(DISTINCT s.tipo ORDER BY s.tipo SEPARATOR ', ') AS tiposSensores
            FROM estacoes e
            LEFT JOIN permissoes_utilizador_estacao pue 
                ON pue.estacao_id = e.id
            LEFT JOIN sensores s 
                ON s.estacao_id = e.id 
               AND s.ativo = TRUE
            LEFT JOIN leituras_sensor l 
                ON l.sensor_id = s.id
            WHERE e.ativa = TRUE
              AND (:admin = TRUE OR pue.utilizador_id = :utilizadorId)
            GROUP BY e.id, e.nome, e.device_id
            ORDER BY ultimaLeituraEm DESC
            """, nativeQuery = true)
    List<DashboardEstacaoProjection> findEstadoEstacoes(
            @Param("utilizadorId") Long utilizadorId,
            @Param("admin") boolean admin,
            Pageable pageable
    );
}