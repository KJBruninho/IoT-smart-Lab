package com.iotroom.auth.repository;

import com.iotroom.auth.model.RefreshToken;
import com.iotroom.auth.model.Utilizador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUtilizadorAndRevogadoEmIsNull(Utilizador utilizador);

    List<RefreshToken> findByUtilizadorIdAndRevogadoEmIsNull(Long utilizadorId);

    List<RefreshToken> findByUtilizadorIdAndDeviceIdHashAndRevogadoEmIsNull(
            Long utilizadorId,
            String deviceIdHash
    );
}