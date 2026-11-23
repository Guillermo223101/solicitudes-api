// Encargado de emitir tokens
package com.gestionsolicitudes.api.service;

import com.gestionsolicitudes.api.model.Usuario;

import java.time.Instant;

import org.springframework.stereotype.Service;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@Service
public class JwtService {
    public static final long EXPIRA_EN_SEGUNDOS = 3600;

    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder){
        this.jwtEncoder = jwtEncoder;
    }

    public String generarToken(Usuario usuario){
        if (usuario.getId() == null || usuario.getRol() == null) {
            throw new IllegalArgumentException("Usuario sin identificador o rol");
        }

        Instant ahora = Instant.now();

        //Contenido del JWT
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("solicitudes-api")
            .subject(usuario.getId())
            .issuedAt(ahora)
            .expiresAt(
                ahora.plusSeconds(EXPIRA_EN_SEGUNDOS))
            .claim("rol", usuario.getRol().name())
            .build();

        //Algoritmo de firma
        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        //Firmar y devolver token
        return jwtEncoder.encode(
            JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}
