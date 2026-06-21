package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.model.LeituraSensor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LeituraSensorRepository extends JpaRepository<LeituraSensor, Long> {

    List<LeituraSensor> findTop30BySensorTipoOrderByRegistadoEmDesc(String tipo);

    Optional<LeituraSensor> findTopBySensorTipoOrderByRegistadoEmDesc(String tipo);

    List<LeituraSensor> findTop10ByOrderByRegistadoEmDesc();

    @Query("""
           SELECT l
           FROM LeituraSensor l
           JOIN FETCH l.sensor s
           JOIN FETCH s.estacao
           ORDER BY l.registadoEm DESC
           """)
    List<LeituraSensor> findRecentesComSensorEEstacao(Pageable pageable);
}