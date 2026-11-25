package com.gestionsolicitudes.api.controller;

import com.gestionsolicitudes.api.model.RolUsuario;
import com.gestionsolicitudes.api.model.Solicitud;
import com.gestionsolicitudes.api.model.Usuario;
import com.gestionsolicitudes.api.repository.UsuarioRepository;
import com.gestionsolicitudes.api.service.SolicitudService;
import com.gestionsolicitudes.api.dto.ActualizarSolicitudRequest;
import com.gestionsolicitudes.api.dto.CrearSolicitudRequest;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudClienteController {
    private final SolicitudService solicitudService;
    private final UsuarioRepository usuarioRepository;

    public SolicitudClienteController(
        SolicitudService solicitudService,
        UsuarioRepository usuarioRepository) {
            this.solicitudService = solicitudService;
            this.usuarioRepository = usuarioRepository;
    }

    // Consultar solicitudes propias
    @GetMapping("/mis")
    public List<Solicitud> misSolicitudes(@AuthenticationPrincipal Jwt jwt) {

        Usuario cliente = obtenerCliente(jwt);

        return solicitudService.listarPorClienteCorreo(
            cliente.getCorreo()
        );
    }

    // Crear una solicitud propia
    @PostMapping("/mis")
    public ResponseEntity<Solicitud> crearSolicitud(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CrearSolicitudRequest request) {

        Usuario cliente = obtenerCliente(jwt);

        Solicitud guardada = solicitudService.crearParaCliente(
            cliente.getCorreo(),
            request.titulo(),
            request.descripcion(),
            request.evidenciaBase64()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(guardada);
    }

    // Identificar al cliente a partir del JWT
    private Usuario obtenerCliente(Jwt jwt) {

        Usuario usuario = usuarioRepository
            .findById(jwt.getSubject())
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no encontrado"
                )
            );

        if (usuario.getRol() != RolUsuario.CLIENTE) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Operacion exclusiva para CLIENTE"
            );
        }

        return usuario;
    }

    // Actualizar una solicitud propia
    @PutMapping("/mis/{id}")
    public ResponseEntity<Solicitud> actualizarMiSolicitud(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @Valid @RequestBody ActualizarSolicitudRequest request) {

        Usuario cliente = obtenerCliente(jwt);

        var resultado = solicitudService.actualizarContenidoDeCliente(
            id,
            cliente.getCorreo(),
            request.titulo(),
            request.descripcion()
        );

        if (resultado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(resultado.get());
    }

    // Eliminar una solicitud propia
    @DeleteMapping("/mis/{id}")
    public ResponseEntity<Void> eliminarMiSolicitud(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id) {

        Usuario cliente = obtenerCliente(jwt);

        boolean eliminada = solicitudService.eliminarDeCliente(
            id,
            cliente.getCorreo()
        );

        if (!eliminada) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

}