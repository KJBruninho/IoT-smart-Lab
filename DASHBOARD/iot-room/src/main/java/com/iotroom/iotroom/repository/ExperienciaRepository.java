package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.model.Experiencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExperienciaRepository extends JpaRepository<Experiencia, Long> {

    long countByCriadoPorIdAndEstadoIn(Long professorId, List<String> estados);

    List<Experiencia> findByCriadoPorIdOrderByCriadoEmDesc(Long professorId);

    List<Experiencia> findTop5ByCriadoPorIdOrderByCriadoEmDesc(Long professorId);

    Optional<Experiencia> findByIdAndCriadoPorId(Long id, Long professorId);

    boolean existsByIdAndCriadoPorId(Long id, Long professorId);
}