package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.model.LeituraSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeituraSensorRepository extends JpaRepository<LeituraSensor, Long> {
    List<LeituraSensor> findTop10ByOrderByRegistadoEmDesc();
    List<LeituraSensor> findTop30BySensorTipoOrderByRegistadoEmDesc(String tipo);

    @Query("SELECT l FROM LeituraSensor l WHERE l.sensor.tipo = :tipo ORDER BY l.registadoEm DESC")
    List<LeituraSensor> findByTipo(@Param("tipo") String tipo);
}
