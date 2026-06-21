package com.iotroom.iotroom.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final JdbcTemplate jdbcTemplate;

    public CustomUserDetailsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.trim().isBlank()) {
            throw new UsernameNotFoundException("Email inválido.");
        }

        String emailNormalizado = email.trim().toLowerCase();

        UtilizadorAuth utilizador = jdbcTemplate.query("""
                SELECT
                    id,
                    nome,
                    email,
                    password_hash,
                    role,
                    ativo
                FROM utilizadores
                WHERE email = ?
                """,
                rs -> {
                    if (!rs.next()) {
                        throw new UsernameNotFoundException("Utilizador não encontrado.");
                    }

                    String passwordHash = rs.getString("password_hash");

                    if (passwordHash == null || passwordHash.isBlank()) {
                        throw new UsernameNotFoundException("Utilizador sem password configurada.");
                    }

                    String role = rs.getString("role");

                    if (role == null || role.isBlank()) {
                        throw new UsernameNotFoundException("Utilizador sem role configurada.");
                    }

                    return new UtilizadorAuth(
                            rs.getLong("id"),
                            rs.getString("nome"),
                            rs.getString("email"),
                            passwordHash,
                            role,
                            rs.getBoolean("ativo")
                    );
                },
                emailNormalizado
        );

        return User.builder()
                .username(utilizador.email())
                .password(utilizador.passwordHash())
                .disabled(!utilizador.ativo())
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_" + utilizador.role())
                ))
                .build();
    }

    private record UtilizadorAuth(
            Long id,
            String nome,
            String email,
            String passwordHash,
            String role,
            boolean ativo
    ) {
    }
}