package com.iotroom.iotroom.config;

import com.iotroom.iotroom.security.JwtAuthenticationFilter;
import com.iotroom.iotroom.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
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
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService customUserDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                /*
                 * A app Android usa JWT/cookies em /api/**.
                 * Evita 403 por CSRF em chamadas POST/PUT/DELETE da app.
                 */
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/api/**",
                        "/auth/login",
                        "/auth/logout",
                        "/auth/register"
                ))

                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(auth -> auth

                        /*
                         * Recursos públicos.
                         */
                        .requestMatchers(
                                "/",
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

                        /*
                         * Endpoints da Auth API quando passam pelo gateway/nginx.
                         * Estes pedidos são tratados pelo serviço iot-auth-api.
                         */
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh"
                        ).permitAll()

                        /*
                         * APIs usadas pela app Android.
                         * Têm de aceitar ADMIN, PROFESSOR e ALUNO/USER.
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

                        /*
                         * Área web de administração.
                         */
                        .requestMatchers(
                                "/admin/**"
                        ).hasAnyAuthority(ADMIN_ONLY)

                        /*
                         * Área web do professor.
                         * O admin também pode aceder.
                         */
                        .requestMatchers(
                                "/professor/**"
                        ).hasAnyAuthority(ADMIN_PROFESSOR)

                        /*
                         * Área web do aluno.
                         */
                        .requestMatchers(
                                "/aluno/**"
                        ).hasAnyAuthority(TODOS_OS_PERFIS)

                        /*
                         * Restantes APIs internas do dashboard.
                         * Importante: não bloquear USER/ALUNO por defeito.
                         */
                        .requestMatchers(
                                "/api/**"
                        ).hasAnyAuthority(TODOS_OS_PERFIS)

                        .anyRequest().authenticated()
                )

                /*
                 * Login web normal.
                 * Mantém compatibilidade com /auth/login.
                 */
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

                /*
                 * Filtro JWT para a app Android e chamadas API.
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
