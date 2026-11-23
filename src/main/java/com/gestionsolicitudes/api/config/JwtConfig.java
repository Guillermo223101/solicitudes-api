package com.gestionsolicitudes.api.config;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {

    //Leer clave desde una variable de entorno
    @Bean 
    public SecretKey jwSecretKey(
        @Value ("${JWT_SECRET:}") String secretoBase64) {

            if (secretoBase64.isBlank()) {
                throw new IllegalStateException("Falta configurar JWT_SECRET");
            }

            byte[] bytes;

            try{
                bytes = Base64.getDecoder().decode(secretoBase64);
            } catch(IllegalStateException e){
                throw new IllegalStateException(
                    "JWT_SECRET debe ser en Base64", e
                );
            }

            if (bytes.length < 32) {
                throw new IllegalStateException(
                    "JWT_SECRET debe contener al menos 32 bytes"
                );
            }

            return new SecretKeySpec(bytes, "HmacSHA256");
        }

    // Firmar tokens JWT
        @Bean 
        public JwtEncoder jwtEncoder(SecretKey jwtSecretKey){
            return NimbusJwtEncoder
                .withSecretKey(jwtSecretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
        }

        // Verificar firma y vencimiento de JWT
        @Bean
        public JwtDecoder jwtDecoder(SecretKey jwtSecretKey){
            NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

            decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                    "solicitudes-api"
                )
            );

            return decoder;
        }

}
