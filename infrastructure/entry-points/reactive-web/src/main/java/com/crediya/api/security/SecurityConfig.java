package com.crediya.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationManager jwtAuthenticationManager() {
        return new JwtAuthenticationManager(jwtUtil);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        AuthenticationWebFilter jwtAuthFilter =
                new AuthenticationWebFilter(new JwtAuthenticationManager(jwtUtil));
        jwtAuthFilter.setServerAuthenticationConverter(new JwtAuthenticationConverter(jwtUtil));

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/*/api/v1/login", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/**")
                        .permitAll()
                        .pathMatchers("/*/api/v1/usuarios").hasAnyRole(Role.Admin.name(), Role.adviser.name())
                        .pathMatchers("/*/api/v1/usuarios/**").hasAnyRole(Role.Admin.name(), Role.adviser.name(), Role.User.name())
                        .pathMatchers("/*/api/v1/solicitud/**").hasAnyRole(Role.User.name())
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .exceptionHandling(exceptionHandlingSpec ->
                        exceptionHandlingSpec.accessDeniedHandler(new AccessDeniedHandler())
                )
                .build();
    }
}
