package com.iotroom.iotroom.repository.leitura;

import com.iotroom.iotroom.model.RegraAlertaSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegraAlertaSensorRepository extends JpaRepository<RegraAlertaSensor, Long> {

    List<RegraAlertaSensor> findByProfessorIdOrderByCriadaEmDesc(Long professorId);

    Optional<RegraAlertaSensor> findByIdAndProfessorId(Long id, Long professorId);

    @Query(value = """
            SELECT r.*
            FROM regras_alerta_sensor r
            WHERE r.ativo = TRUE
              AND r.professor_id = :professorId
              AND r.tipo_sensor = :tipoSensor
              AND (r.grupo_id IS NULL OR r.grupo_id = :grupoId)
              AND (r.experiencia_id IS NULL OR r.experiencia_id = :experienciaId)
              AND (r.estacao_id IS NULL OR r.estacao_id = :estacaoId)
            ORDER BY r.criada_em DESC
            """, nativeQuery = true)
    List<RegraAlertaSensor> findRegrasAplicaveis(
            @Param("professorId") Long professorId,
            @Param("grupoId") Long grupoId,
            @Param("experienciaId") Long experienciaId,
            @Param("estacaoId") Long estacaoId,
            @Param("tipoSensor") String tipoSensor
    );
}