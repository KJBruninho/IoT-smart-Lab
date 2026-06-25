package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.model.PedidoConfiguracaoSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import com.iotroom.iotroom.model.Sensor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PedidoConfiguracaoSensorRepository extends JpaRepository<PedidoConfiguracaoSensor, Long> {

    List<PedidoConfiguracaoSensor> findByEstadoOrderByCriadoEmDesc(String estado);

    List<PedidoConfiguracaoSensor> findTop10BySensorIdOrderByCriadoEmDesc(Long sensorId);

    List<PedidoConfiguracaoSensor> findBySolicitadoPorOrderByCriadoEmDesc(Long solicitadoPor);
    
    @Query(value = """
            SELECT DISTINCT s.*
            FROM sensores s
            INNER JOIN estacoes e ON e.id = s.estacao_id
            WHERE s.ativo = TRUE
            AND e.ativa = TRUE
            ORDER BY e.nome ASC, s.tipo ASC, s.nome ASC
            """, nativeQuery = true)
    List<Sensor> findSensoresAtivosComEstacao();

    @Query(value = """
            SELECT DISTINCT s.*
            FROM sensores s
            INNER JOIN estacoes e ON e.id = s.estacao_id
            INNER JOIN permissoes_grupo_estacao pge ON pge.estacao_id = e.id
            INNER JOIN utilizador_grupos ug ON ug.grupo_id = pge.grupo_id
            WHERE ug.utilizador_id = :utilizadorId
            AND s.ativo = TRUE
            AND e.ativa = TRUE
            ORDER BY e.nome ASC, s.tipo ASC, s.nome ASC
            """, nativeQuery = true)
    List<Sensor> findSensoresDisponiveisParaUtilizador(
            @Param("utilizadorId") Long utilizadorId
    );

    @Query(value = """
            SELECT COUNT(DISTINCT s.id)
            FROM sensores s
            INNER JOIN estacoes e ON e.id = s.estacao_id
            INNER JOIN permissoes_grupo_estacao pge ON pge.estacao_id = e.id
            INNER JOIN utilizador_grupos ug ON ug.grupo_id = pge.grupo_id
            WHERE ug.utilizador_id = :utilizadorId
            AND s.id = :sensorId
            AND s.ativo = TRUE
            AND e.ativa = TRUE
            """, nativeQuery = true)
    long countSensorDisponivelParaUtilizador(
            @Param("sensorId") Long sensorId,
            @Param("utilizadorId") Long utilizadorId
    );
    
}