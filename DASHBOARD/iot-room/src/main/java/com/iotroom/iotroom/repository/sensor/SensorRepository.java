package com.iotroom.iotroom.repository.sensor;

import com.iotroom.iotroom.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {

    Optional<Sensor> findByEstacaoDeviceIdAndTipoAndAtivoTrueAndEstacaoAtivaTrue(
            String deviceId,
            String tipo
    );

    @Query(value = """
            SELECT s.*
            FROM sensores s
            INNER JOIN estacoes e ON e.id = s.estacao_id
            WHERE e.ativa = TRUE
              AND s.ativo = TRUE
              AND EXISTS (
                  SELECT 1
                  FROM permissoes_utilizador_estacao pue
                  WHERE pue.estacao_id = e.id
                    AND pue.utilizador_id = :professorId
              )
            ORDER BY e.nome ASC, s.tipo ASC
            """, nativeQuery = true)
    List<Sensor> findSensoresDoProfessor(@Param("professorId") Long professorId);

    @Query(value = """
            SELECT COUNT(*)
            FROM sensores s
            INNER JOIN estacoes e ON e.id = s.estacao_id
            WHERE s.id = :sensorId
              AND EXISTS (
                  SELECT 1
                  FROM permissoes_utilizador_estacao pue
                  WHERE pue.estacao_id = e.id
                    AND pue.utilizador_id = :professorId
              )
            """, nativeQuery = true)
    long countSensorDoProfessor(
            @Param("sensorId") Long sensorId,
            @Param("professorId") Long professorId
    );
    

    @Query("""
           SELECT s
           FROM Sensor s
           LEFT JOIN FETCH s.estacao
           ORDER BY s.id
           """)
    List<Sensor> findAllComEstacao();

    @Query("""
           SELECT s
           FROM Sensor s
           LEFT JOIN FETCH s.estacao
           WHERE s.tipo = :tipo
           ORDER BY s.id
           """)
    List<Sensor> findByTipoComEstacao(String tipo);

    @Query("""
           SELECT s
           FROM Sensor s
           LEFT JOIN FETCH s.estacao
           WHERE s.estacao.id = :estacaoId
           ORDER BY s.id
           """)
    List<Sensor> findByEstacaoIdComEstacao(Long estacaoId);
}


