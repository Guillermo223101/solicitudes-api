package com.gestionsolicitudes.api.controller;

import com.gestionsolicitudes.api.dto.RegistroUsuarioRequest;
import com.gestionsolicitudes.api.dto.UsuarioRegistradoResponse;
import com.gestionsolicitudes.api.model.Usuario;
import com.gestionsolicitudes.api.service.CorreoDuplicadoException;
import com.gestionsolicitudes.api.service.UsuarioService;
import com.gestionsolicitudes.api.dto.LoginUsuarioRequest;

import jakarta.validation.Valid;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import com.gestionsolicitudes.api.dto.LoginUsuarioResponse;
import com.gestionsolicitudes.api.service.JwtService;
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    //Consturctor
    public UsuarioController(UsuarioService usuarioService, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    // Registrar nuevo cliente
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(
            @Valid @RequestBody RegistroUsuarioRequest request) {

        try {

            // Registrar usuario desde el Service
            Usuario guardado = usuarioService.registrarCliente(
                request.nombre(),
                request.correo(),
                request.clave(),
                request.direccion()
            );

            // Preparar respuesta sin contrasena
            UsuarioRegistradoResponse respuesta =
                new UsuarioRegistradoResponse(
                    guardado.getId(),
                    guardado.getNombre(),
                    guardado.getCorreo(),
                    guardado.getDireccion(),
                    guardado.getRol()
                );

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);

        } catch (CorreoDuplicadoException |
                 DuplicateKeyException e) {

            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                    "error", "El correo ya esta registrado"
                ));

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                .badRequest()
                .body(Map.of(
                    "error", e.getMessage()
                ));
        }
    }

    // Verificar credenciales del usuario
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginUsuarioRequest request) {

        // Buscar usuario y verificar contraseña
        var resultado = usuarioService.autenticar(
            request.correo(),
            request.clave()
        );

        // Credenciales incorrectas
        if (resultado.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "error", "Credenciales invalidas"
                ));
        }

        // Usuario autenticado correctamente
        Usuario usuario = resultado.get();

        // Preparar respuesta sin contraseña ni hash
        UsuarioRegistradoResponse respuesta =
            new UsuarioRegistradoResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getDireccion(),
                usuario.getRol()
            );

        String token = jwtService.generarToken(usuario);

        LoginUsuarioResponse respuestaLogin =
            new LoginUsuarioResponse(
                token,
                "Bearer",
                JwtService.EXPIRA_EN_SEGUNDOS,
                respuesta
            );
        
        return ResponseEntity.ok(respuestaLogin);
    }


}
