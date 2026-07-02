package com.iotroom.iotroom.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.iotroom.iotroom.security.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] TODOS_OS_PERFIS = {
            "ADMIN",
            "PROFESSOR",
            "ALUNO",
            "USER"
    };

    private static final String[] ADMIN_PROFESSOR = {
            "ADMIN",
            "PROFESSOR"
    };

    private static final String[] ADMIN_PROFESSOR_ALUNO = {
            "ADMIN",
            "PROFESSOR",
            "ALUNO",
            "USER"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Não autenticado");
                            } else {
                                response.sendRedirect("/auth/login");
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado");
                            } else {
                                response.sendRedirect("/acesso-negado");
                            }
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(
                                "/auth/login",
                                "/auth/register",
                                "/auth/logout",
                                "/acesso-negado",
                                "/error",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/img/**",
                                "/webjars/**",
                                "/favicon.ico"
                        ).permitAll()

                        /*
                         * Rotas da Auth API quando passam pelo gateway.
                         * Normalmente o Nginx envia isto para o serviço iot-auth-api,
                         * mas deixar aqui como permitAll evita bloqueios se cair no dashboard.
                         */
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh"
                        ).permitAll()

                        /*
                         * Áreas web.
                         */
                        .requestMatchers("/admin/**")
                        .hasAuthority("ADMIN")

                        .requestMatchers("/professor/**")
                        .hasAnyAuthority(ADMIN_PROFESSOR)

                        .requestMatchers("/aluno/**")
                        .hasAnyAuthority(ADMIN_PROFESSOR_ALUNO)

                        /*
                         * APIs específicas por área.
                         */
                        .requestMatchers("/api/admin/**")
                        .hasAuthority("ADMIN")

                        .requestMatchers("/api/professor/**")
                        .hasAnyAuthority(ADMIN_PROFESSOR)

                        .requestMatchers("/api/aluno/**")
                        .hasAnyAuthority(ADMIN_PROFESSOR_ALUNO)

                        /*
                         * APIs usadas pela app Android.
                         * Antes estavam só para PROFESSOR/ADMIN, causando 403 em contas ALUNO/USER.
                         */
                        .requestMatchers(
                                "/api/dashboard/**",
                                "/api/temperatura",
                                "/api/tds",
                                "/api/ph",
                                "/api/alertas/**",
                                "/api/estacoes/**",
                                "/api/sensores/**",
                                "/api/leituras/**",
                                "/api/grupos/**",
                                "/api/experiencias/**",
                                "/api/pedidos-comando/**",
                                "/api/pedidos-configuracao/**",
                                "/api/assistente/**"
                        ).hasAnyAuthority(TODOS_OS_PERFIS)

                        /*
                         * Qualquer outra API exige autenticação válida.
                         */
                        .requestMatchers("/api/**")
                        .hasAnyAuthority(TODOS_OS_PERFIS)

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
