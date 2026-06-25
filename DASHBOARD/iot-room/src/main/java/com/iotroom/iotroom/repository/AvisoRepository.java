package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.model.Aviso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvisoRepository extends JpaRepository<Aviso, Long> {

    long countByCriadoPorIdAndAtivoTrue(Long professorId);

    List<Aviso> findByCriadoPorIdOrderByCriadoEmDesc(Long professorId);

    List<Aviso> findTop5ByCriadoPorIdOrderByCriadoEmDesc(Long professorId);

    Optional<Aviso> findByIdAndCriadoPorId(Long id, Long professorId);

    boolean existsByIdAndCriadoPorId(Long id, Long professorId);
}