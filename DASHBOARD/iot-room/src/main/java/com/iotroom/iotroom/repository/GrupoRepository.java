package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {

    long countByProfessorIdAndAtivoTrue(Long professorId);

    List<Grupo> findByProfessorIdOrderByCriadoEmDesc(Long professorId);

    List<Grupo> findTop5ByProfessorIdOrderByCriadoEmDesc(Long professorId);

    Optional<Grupo> findByIdAndProfessorId(Long id, Long professorId);

    boolean existsByIdAndProfessorId(Long id, Long professorId);
}