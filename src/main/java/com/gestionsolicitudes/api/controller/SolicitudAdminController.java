
package com.gestionsolicitudes.api.controller;

import com.gestionsolicitudes.api.dto.AsignarTecnicoRequest;
import com.gestionsolicitudes.api.dto.CambiarPrioridadRequest;
import com.gestionsolicitudes.api.model.RolUsuario;
import com.gestionsolicitudes.api.model.Solicitud;
import com.gestionsolicitudes.api.model.Usuario;
import com.gestionsolicitudes.api.repository.UsuarioRepository;
import com.gestionsolicitudes.api.service.SolicitudService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/solicitudes/admin")
public class SolicitudAdminController {

    private final SolicitudService solicitudService;
    private final UsuarioRepository usuarioRepository;

    public SolicitudAdminController(
            SolicitudService solicitudService,
            UsuarioRepository usuarioRepository) {

        this.solicitudService = solicitudService;
        this.usuarioRepository = usuarioRepository;
    }

    // Comprobar que el administrador sigue autorizado
    private void verificarAdministrador(Jwt jwt) {

        Usuario administrador = usuarioRepository
            .findById(jwt.getSubject())
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no encontrado"
                )
            );

        if (administrador.getRol() != RolUsuario.ADMIN) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Se requiere el rol ADMIN"
            );
        }
    }

    // Supervisar todas las solicitudes
    @GetMapping
    public List<Solicitud> listarSolicitudes(
            @AuthenticationPrincipal Jwt jwt) {

        verificarAdministrador(jwt);

        return solicitudService.listarSolicitudes();
    }

    // Cambiar prioridad
    @PatchMapping("/{id}/prioridad")
    public ResponseEntity<Solicitud> cambiarPrioridad(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") String identificadorSolicitud,
            @Valid @RequestBody CambiarPrioridadRequest datos) {

        verificarAdministrador(jwt);

        try {
            return solicitudService.actualizarPrioridad(
                    identificadorSolicitud,
                    datos.prioridad()
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

    // Asignar un tecnico existente
    @PatchMapping("/{id}/tecnico")
    public ResponseEntity<Solicitud> asignarTecnico(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") String identificadorSolicitud,
            @Valid @RequestBody AsignarTecnicoRequest datos) {

        verificarAdministrador(jwt);

        String correoTecnico = datos.correoTecnico()
            .trim()
            .toLowerCase(Locale.ROOT);

        // Validar que el tecnico exista y tenga su rol
        Usuario tecnico = usuarioRepository
            .findByCorreo(correoTecnico)
            .filter(usuario ->
                usuario.getRol() == RolUsuario.TECNICO
            )
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tecnico no existe o no tiene rol TECNICO"
                )
            );

        return solicitudService.asignarTecnico(
                identificadorSolicitud,
                tecnico.getCorreo()
            )
            .map(ResponseEntity::ok)
            .orElseGet(() ->
                ResponseEntity.notFound().build()
            );
    }
}
