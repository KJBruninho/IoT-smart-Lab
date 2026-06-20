package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.model.RoleGrupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleGrupoRepository extends JpaRepository<RoleGrupo, Long> {

    Optional<RoleGrupo> findByNome(String nome);

    List<RoleGrupo> findAllByOrderByNomeAsc();
}