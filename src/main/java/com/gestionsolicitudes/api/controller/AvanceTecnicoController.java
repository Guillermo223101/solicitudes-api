package com.gestionsolicitudes.api.controller;

import com.gestionsolicitudes.api.dto.RegistrarAvanceTecnicoRequest;
import com.gestionsolicitudes.api.model.AvanceTecnico;
import com.gestionsolicitudes.api.model.RolUsuario;
import com.gestionsolicitudes.api.model.Usuario;
import com.gestionsolicitudes.api.repository.UsuarioRepository;
import com.gestionsolicitudes.api.service.AvanceTecnicoService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/solicitudes/tecnico")
public class AvanceTecnicoController {

    private final AvanceTecnicoService avanceService;
    private final UsuarioRepository usuarioRepository;

    public AvanceTecnicoController(
            AvanceTecnicoService avanceService,
            UsuarioRepository usuarioRepository) {

        this.avanceService = avanceService;
        this.usuarioRepository = usuarioRepository;
    }

    // Obtener el tecnico autenticado mediante JWT
    private Usuario obtenerTecnico(Jwt jwt) {

        Usuario usuario = usuarioRepository
            .findById(jwt.getSubject())
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no encontrado"
                )
            );

        if (usuario.getRol() != RolUsuario.TECNICO) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Se requiere el rol TECNICO"
            );
        }

        return usuario;
    }

    // Registrar un nuevo avance
    @PostMapping("/{id}/avances")
    public ResponseEntity<AvanceTecnico> registrarAvance(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") String identificadorSolicitud,
            @Valid @RequestBody RegistrarAvanceTecnicoRequest datos) {

        Usuario tecnico = obtenerTecnico(jwt);

        try {
            return avanceService.registrarAvance(
                    identificadorSolicitud,
                    tecnico.getCorreo(),
                    datos.comentario()
                )
                .map(avance -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(avance)
                )
                .orElseGet(() ->
                    ResponseEntity.notFound().build()
                );

        } catch (IllegalArgumentException error) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                error.getMessage()
            );
        }
    }

    // Consultar los avances de una solicitud asignada
    @GetMapping("/{id}/avances")
    public ResponseEntity<List<AvanceTecnico>> consultarAvances(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") String identificadorSolicitud) {

        Usuario tecnico = obtenerTecnico(jwt);

        return avanceService.consultarAvances(
                identificadorSolicitud,
                tecnico.getCorreo()
            )
            .map(ResponseEntity::ok)
            .orElseGet(() ->
                ResponseEntity.notFound().build()
            );
    }
}