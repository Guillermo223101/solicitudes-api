package com.gestionsolicitudes.api.service;

import com.gestionsolicitudes.api.config.ApiSecurityConfig;
import com.gestionsolicitudes.api.model.RolUsuario;

import org.junit.jupiter.api.Test;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import static org.junit.jupiter.api.Assertions.*;

public class ApiSecurityConfigTest {

    private final JwtAuthenticationConverter converter =
        new ApiSecurityConfig().jwtAuthenticationConverter();

    @Test
    void debeConvertirLosTresRoles() {

        for (RolUsuario rol : RolUsuario.values()) {

            // Simular un JWT con un rol
            Jwt jwt = Jwt.withTokenValue("token-de-prueba")
                .header("alg", "HS256")
                .claim("sub", "usuario-123")
                .claim("rol", rol.name())
                .build();

            var autenticacion = converter.convert(jwt);

            assertNotNull(autenticacion);

            assertEquals(
                "usuario-123",
                autenticacion.getName()
            );

            assertTrue(
                autenticacion.getAuthorities()
                    .stream()
                    .anyMatch(autoridad ->
                        autoridad.getAuthority().equals(
                            "ROLE_" + rol.name()
                        )
                    )
            );
        }
    }

    @Test
    void noDebeAsignarRolesSiFaltaElAtributo() {

        //Simular un JWT sin el atributo rol
        Jwt jwt = Jwt.withTokenValue("token-de-prueba")
            .header("alg", "HS256")
            .claim("sub", "usuario-123")
            .build();

        var autenticacion = converter.convert(jwt);

        assertNotNull(autenticacion);

        //Ninguna autoridad debe comenzar con ROLE_
        boolean tieneRol = autenticacion.getAuthorities()
            .stream()
            .anyMatch(autoridad ->
                autoridad.getAuthority().startsWith("ROLE_")
            );

        assertFalse(tieneRol);
    }
}
