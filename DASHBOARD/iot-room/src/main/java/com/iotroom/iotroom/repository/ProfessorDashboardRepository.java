package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.dto.*;
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
            INNER JOIN permissoes_utilizador_estacao pue ON pue.estacao_id = e.id
            WHERE pue.utilizador_id = :professorId
              AND s.ativo = TRUE
              AND e.ativa = TRUE
            """, nativeQuery = true)
    long countSensoresAtivos(@Param("professorId") Long professorId);

    @Query(value = """
            SELECT COUNT(*)
            FROM (
                SELECT e.id, MAX(l.data_registo) AS ultima_leitura
                FROM estacoes e
                INNER JOIN permissoes_utilizador_estacao pue ON pue.estacao_id = e.id
                LEFT JOIN sensores s ON s.estacao_id = e.id AND s.ativo = TRUE
                LEFT JOIN leituras_sensor l ON l.sensor_id = s.id
                WHERE pue.utilizador_id = :professorId
                  AND e.ativa = TRUE
                GROUP BY e.id
                HAVING ultima_leitura >= DATE_SUB(NOW(), INTERVAL 2 MINUTE)
            ) x
            """, nativeQuery = true)
    long countEstacoesOnline(@Param("professorId") Long professorId);

    @Query(value = """
            SELECT COUNT(*)
            FROM alertas_sensor
            WHERE professor_id = :professorId
              AND estado = 'NOVO'
            """, nativeQuery = true)
    long countAlertasNovos(@Param("professorId") Long professorId);

    @Query(value = """
            SELECT MAX(l.data_registo)
            FROM leituras_sensor l
            INNER JOIN experiencias exp ON exp.id = l.experiencia_id
            WHERE exp.criado_por = :professorId
            """, nativeQuery = true)
    Timestamp findUltimaLeituraEm(@Param("professorId") Long professorId);

    @Query(value = """
            SELECT
                l.id AS leituraId,
                s.nome AS sensorNome,
                s.tipo AS tipoSensor,
                l.unidade AS unidade,
                l.valor AS valor,
                l.data_registo AS dataRegisto,
                e.nome AS estacaoNome,
                e.device_id AS deviceId,
                exp.nome AS experienciaNome
            FROM leituras_sensor l
            INNER JOIN experiencias exp ON exp.id = l.experiencia_id
            INNER JOIN sensores s ON s.id = l.sensor_id
            INNER JOIN estacoes e ON e.id = s.estacao_id
            WHERE exp.criado_por = :professorId
            ORDER BY l.data_registo DESC
            """, nativeQuery = true)
    List<DashboardLeituraProjection> findUltimasLeituras(
            @Param("professorId") Long professorId,
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
            WHERE professor_id = :professorId
            ORDER BY criado_em DESC
            """, nativeQuery = true)
    List<DashboardAlertaProjection> findAlertasRecentes(
            @Param("professorId") Long professorId,
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
            WHERE exp.criado_por = :professorId
              AND exp.estado IN ('CRIADA', 'EM_EXECUCAO')
            GROUP BY exp.id, exp.nome, g.nome, exp.estado, exp.data_inicio
            ORDER BY
                CASE exp.estado
                    WHEN 'EM_EXECUCAO' THEN 1
                    WHEN 'CRIADA' THEN 2
                    ELSE 3
                END,
                ultimaLeituraEm DESC,
                exp.data_inicio DESC
            """, nativeQuery = true)
    List<DashboardExperienciaProjection> findExperienciasAtivas(
            @Param("professorId") Long professorId,
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
            INNER JOIN permissoes_utilizador_estacao pue ON pue.estacao_id = e.id
            LEFT JOIN sensores s ON s.estacao_id = e.id AND s.ativo = TRUE
            LEFT JOIN leituras_sensor l ON l.sensor_id = s.id
            WHERE pue.utilizador_id = :professorId
              AND e.ativa = TRUE
            GROUP BY e.id, e.nome, e.device_id
            ORDER BY ultimaLeituraEm DESC
            """, nativeQuery = true)
    List<DashboardEstacaoProjection> findEstadoEstacoes(
            @Param("professorId") Long professorId,
            Pageable pageable
    );
}