package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.model.ForumResposta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumRespostaRepository extends JpaRepository<ForumResposta, Long> {

    List<ForumResposta> findByTopicoIdAndAtivoTrueOrderByCriadoEmAsc(Long topicoId);

    long countByTopicoIdAndAtivoTrue(Long topicoId);
}