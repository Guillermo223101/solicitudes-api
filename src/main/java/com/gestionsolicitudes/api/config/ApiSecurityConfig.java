package com.gestionsolicitudes.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@Configuration 
public class ApiSecurityConfig {
    // Convertir el campo rol en una autoridad de Spring
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtGrantedAuthoritiesConverter rolesConverter = new JwtGrantedAuthoritiesConverter();

        //Leer el atributo rol de nuestro JWT
        rolesConverter.setAuthoritiesClaimName("rol");

        // CLIENTE se convierte en ROLE_CLIENTE
        rolesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(rolesConverter);

        return converter;
    }

    @Bean 
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        JwtAuthenticationConverter jwtAuthenticationConverter)
         throws Exception{
        
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

                    // Ruta que exige autenticacion
                    .requestMatchers(
                        "/api/usuarios/me"
                    ).authenticated()

                    // TEMPORAL: Conservar el CRUD existente
                    .requestMatchers(
                        "/api/solicitudes",
                        "/api/solicitudes/**"
                    ).permitAll()

                    //Bloquear rutas no contempladas
                    .anyRequest().denyAll()

                // Validar JWT y obtener sus autoridades
                ).oauth2ResourceServer(oauth2 ->
                    oauth2.jwt( jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

                return http.build();
    }

}
