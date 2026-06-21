package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.dto.LeituraAlertaContextProjection;
import com.iotroom.iotroom.model.AlertaSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AlertaSensorRepository extends JpaRepository<AlertaSensor, Long> {

    List<AlertaSensor> findByProfessorIdOrderByCriadoEmDesc(Long professorId);

    Optional<AlertaSensor> findByIdAndProfessorId(Long id, Long professorId);

    boolean existsByRegraIdAndCriadoEmAfter(Long regraId, LocalDateTime dataLimite);

    long countByProfessorIdAndEstado(Long professorId, String estado);

    @Query(value = """
            SELECT
                l.id AS leituraId,
                exp.criado_por AS professorId,
                exp.id AS experienciaId,
                exp.grupo_id AS grupoId,
                e.id AS estacaoId,
                s.id AS sensorId,
                s.tipo AS tipoSensor,
                l.valor AS valorLido
            FROM leituras_sensor l
            INNER JOIN experiencias exp ON exp.id = l.experiencia_id
            INNER JOIN sensores s ON s.id = l.sensor_id
            INNER JOIN estacoes e ON e.id = s.estacao_id
            WHERE l.id = :leituraId
            LIMIT 1
            """, nativeQuery = true)
    Optional<LeituraAlertaContextProjection> obterContextoLeitura(@Param("leituraId") Long leituraId);
}