package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.dto.ComparacaoLeituraProjection;
import com.iotroom.iotroom.model.LeituraSensor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProfessorDadosRepository extends JpaRepository<LeituraSensor, Long> {

    @Query(value = """
            SELECT
                l.id AS leituraId,
                g.nome AS grupoNome,
                exp.nome AS experienciaNome,
                e.nome AS estacaoNome,
                e.device_id AS deviceId,
                s.nome AS sensorNome,
                s.tipo AS tipoSensor,
                l.unidade AS unidade,
                l.valor AS valor,
                l.data_registo AS dataRegisto
            FROM leituras_sensor l
            INNER JOIN experiencias exp ON exp.id = l.experiencia_id
            INNER JOIN grupos g ON g.id = exp.grupo_id
            INNER JOIN sensores s ON s.id = l.sensor_id
            INNER JOIN estacoes e ON e.id = s.estacao_id
            WHERE exp.criado_por = :professorId
              AND (:grupoId IS NULL OR g.id = :grupoId)
              AND (:experienciaId IS NULL OR exp.id = :experienciaId)
              AND (:estacaoId IS NULL OR e.id = :estacaoId)
              AND (:tipoSensor IS NULL OR s.tipo = :tipoSensor)
            ORDER BY l.data_registo DESC
            """, nativeQuery = true)
    List<ComparacaoLeituraProjection> findLeiturasComparacao(
            @Param("professorId") Long professorId,
            @Param("grupoId") Long grupoId,
            @Param("experienciaId") Long experienciaId,
            @Param("estacaoId") Long estacaoId,
            @Param("tipoSensor") String tipoSensor,
            Pageable pageable
    );
}