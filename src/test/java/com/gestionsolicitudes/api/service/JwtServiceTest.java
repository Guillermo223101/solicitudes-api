package com.gestionsolicitudes.api.service;

import com.gestionsolicitudes.api.model.RolUsuario;
import com.gestionsolicitudes.api.model.Usuario;

import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {
    private SecretKey crearClavePrueba(){
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);

        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    private Usuario crearUsuarioPrueba(){
        Usuario usuario = new Usuario();

        usuario.setId("usuario-123");
        usuario.setRol(RolUsuario.CLIENTE);

        return usuario;
    }

    @Test 
    void debeGenerarTokenValido() {

        SecretKey clave = crearClavePrueba();

        JwtEncoder encoder = NimbusJwtEncoder
            .withSecretKey(clave)
            .algorithm(MacAlgorithm.HS256)
            .build();

        JwtDecoder decoder = NimbusJwtDecoder
            .withSecretKey(clave)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();

        JwtService service = new JwtService(encoder);

        String token = service.generarToken(crearUsuarioPrueba());

        Jwt jwt = decoder.decode(token);

        assertEquals("usuario-123", jwt.getSubject());
        assertEquals("CLIENTE", jwt.getClaimAsString("rol"));

        assertNotNull(jwt.getExpiresAt());
        assertNotNull(jwt.getIssuedAt());

        assertTrue(jwt.getExpiresAt().isAfter(jwt.getIssuedAt()));
    }

    @Test
    void debeRechazarTokenConOtraClave() {

        SecretKey claveOriginal = crearClavePrueba();
        SecretKey claveIncorrecta = crearClavePrueba();

        JwtEncoder encoder = NimbusJwtEncoder
            .withSecretKey(claveOriginal)
            .algorithm(MacAlgorithm.HS256)
            .build();

        JwtService service = new JwtService(encoder);

        String token = service.generarToken(crearUsuarioPrueba());

        JwtDecoder decoderIncorrecto = NimbusJwtDecoder
            .withSecretKey(claveIncorrecta)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();

        assertThrows(JwtException.class,() -> 
            decoderIncorrecto.decode(token)
        );
    }
}
