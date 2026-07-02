package com.iotroom.iotroom.config;

import com.iotroom.iotroom.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] TODOS_OS_PERFIS = {
            "ADMIN",
            "PROFESSOR",
            "USER",
            "ALUNO",
            "ROLE_ADMIN",
            "ROLE_PROFESSOR",
            "ROLE_USER",
            "ROLE_ALUNO"
    };

    private static final String[] ADMIN_PROFESSOR = {
            "ADMIN",
            "PROFESSOR",
            "ROLE_ADMIN",
            "ROLE_PROFESSOR"
    };

    private static final String[] ADMIN_ONLY = {
            "ADMIN",
            "ROLE_ADMIN"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/api/**",
                        "/auth/login",
                        "/auth/logout",
                        "/auth/register"
                ))

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/login",
                                "/auth/login",
                                "/auth/register",
                                "/auth/logout",
                                "/register",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/img/**",
                                "/webjars/**",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()

                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh"
                        ).permitAll()

                        /*
                         * APIs usadas pela app Android.
                         * O aluno estava a levar 403 porque USER/ALUNO não estavam autorizados.
                         */
                        .requestMatchers(
                                "/api/dashboard/**",
                                "/api/temperatura",
                                "/api/tds",
                                "/api/ph",
                                "/api/alertas/**",
                                "/api/pedidos-comando/**",
                                "/api/pedidos-configuracao/**",
                                "/api/assistente/**"
                        ).hasAnyAuthority(TODOS_OS_PERFIS)

                        .requestMatchers(
                                "/admin/**"
                        ).hasAnyAuthority(ADMIN_ONLY)

                        .requestMatchers(
                                "/professor/**"
                        ).hasAnyAuthority(ADMIN_PROFESSOR)

                        .requestMatchers(
                                "/aluno/**"
                        ).hasAnyAuthority(TODOS_OS_PERFIS)

                        /*
                         * Qualquer outra API do dashboard fica autenticada
                         * e acessível aos perfis existentes.
                         */
                        .requestMatchers(
                                "/api/**"
                        ).hasAnyAuthority(TODOS_OS_PERFIS)

                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout")
                        .permitAll()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
