package com.gestionsolicitudes.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;

@Configuration 
public class ApiSecurityConfig {

    @Bean 
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
            // API REST sin sesiones basadas en cookies
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Desactivar formularios y Basic Auth
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)

            //Reglas iniciales
            .authorizeHttpRequests(auth -> auth
                
                // Registro y login publicos
                .requestMatchers(
                    "/api/usuarios/registro",
                    "/api/usuarios/login"
                ).permitAll()

                // TEMPORAL: Conservar el CRUD existente
                .requestMatchers(
                    "/api/solicitudes",
                    "/api/solicitudes/**"
                ).permitAll()

                //Bloquear rutas no contempladas
                .anyRequest().denyAll()
            );

            return http.build();
    }

}
