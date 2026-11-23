package com.gestionsolicitudes.api.service;

import com.gestionsolicitudes.api.model.RolUsuario;
import com.gestionsolicitudes.api.model.Usuario;
import com.gestionsolicitudes.api.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service 
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrarCliente(
            String nombre,
            String correo,
            String clave,
            String direccion) {

        // Validaciones iniciales
        if (nombre == null || nombre.isBlank() ||
                correo == null || correo.isBlank()) {
            throw new IllegalArgumentException(
                "Nombre y correo son obligatorios"
            );
        }

        if (clave == null || clave.length() < 8) {
            throw new IllegalArgumentException(
                "La clave debe tener al menos 8 caracteres"
            );
        }

        // Normalizar el correo
        String correoNormalizado =
                correo.trim().toLowerCase(Locale.ROOT);

        // Evitar correos duplicados
        if (repository.existsByCorreo(correoNormalizado)) {
            throw new CorreoDuplicadoException();
        }

        // Crear usuario
        Usuario usuario = new Usuario();

        usuario.setNombre(nombre.trim());
        usuario.setCorreo(correoNormalizado);
        usuario.setDireccion(
            direccion == null ? "" : direccion.trim()
        );

        // Nunca guardar la clave original
        usuario.setClaveHash(
            passwordEncoder.encode(clave)
        );

        // El registro publico solo permite CLIENTE
        usuario.setRol(RolUsuario.CLIENTE);

        return repository.save(usuario);
    }

    public Optional<Usuario> autenticar(String correo, String clave) {

        // Validar datos recibidos
        if (correo == null || correo.isBlank()
                || clave == null || clave.isBlank()) {

            return Optional.empty();
        }

        // Normalizar correo
        String correoNormalizado = correo.trim().toLowerCase(Locale.ROOT);

        // Buscar usuario y comprobar contraseña
        return repository.findByCorreo(correoNormalizado)
            .filter(usuario ->
                usuario.getClaveHash() != null &&
                passwordEncoder.matches(clave, usuario.getClaveHash())
            );
    }

}
