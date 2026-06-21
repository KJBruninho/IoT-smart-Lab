package com.iotroom.iotroom.repository;

import com.iotroom.iotroom.model.Utilizador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UtilizadorRepository extends JpaRepository<Utilizador, Long> {

    @Query(value = """
            SELECT u.*
            FROM utilizadores u
            LEFT JOIN utilizador_grupos ug
                   ON ug.utilizador_id = u.id
                  AND ug.grupo_id = :grupoId
            WHERE u.ativo = TRUE
              AND ug.utilizador_id IS NULL
            ORDER BY u.nome ASC
            """, nativeQuery = true)
    List<Utilizador> findUtilizadoresDisponiveisParaGrupo(@Param("grupoId") Long grupoId);
}