package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.model.ForumTopico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ForumTopicoRepository extends JpaRepository<ForumTopico, Long> {

    List<ForumTopico> findByCriadoPorIdOrderByCriadoEmDesc(Long professorId);

    Optional<ForumTopico> findByIdAndCriadoPorId(Long id, Long professorId);
}