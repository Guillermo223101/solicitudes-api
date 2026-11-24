package com.gestionsolicitudes.api.controller;

import com.gestionsolicitudes.api.dto.CambiarEstadoTecnicoRequest;
import com.gestionsolicitudes.api.model.RolUsuario;
import com.gestionsolicitudes.api.model.Solicitud;
import com.gestionsolicitudes.api.model.Usuario;
import com.gestionsolicitudes.api.repository.UsuarioRepository;
import com.gestionsolicitudes.api.service.SolicitudService;

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
public class SolicitudTecnicoController {

    private final SolicitudService solicitudService;
    private final UsuarioRepository usuarioRepository;

    public SolicitudTecnicoController(
            SolicitudService solicitudService,
            UsuarioRepository usuarioRepository) {

        this.solicitudService = solicitudService;
        this.usuarioRepository = usuarioRepository;
    }

    // Identificar al tecnico autenticado
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

    // Consultar solicitudes asignadas al tecnico
    @GetMapping
    public List<Solicitud> consultarAsignadas(
            @AuthenticationPrincipal Jwt jwt) {

        Usuario tecnico = obtenerTecnico(jwt);

        return solicitudService.listarAsignadasATecnico(
            tecnico.getCorreo()
        );
    }

    // Actualizar el estado de una solicitud asignada
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Solicitud> cambiarEstado(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") String identificadorSolicitud,
            @Valid @RequestBody CambiarEstadoTecnicoRequest datos) {

        Usuario tecnico = obtenerTecnico(jwt);

        try {
            return solicitudService.actualizarEstadoDeTecnico(
                    identificadorSolicitud,
                    tecnico.getCorreo(),
                    datos.estado()
                )
                .map(ResponseEntity::ok)
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
}
