package com.gestionsolicitudes.api.service;

import com.gestionsolicitudes.api.model.RolUsuario;
import com.gestionsolicitudes.api.model.Usuario;
import com.gestionsolicitudes.api.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

public class UsuarioServiceTest {
    @Test
    void debeRegistrarClienteConClaveProtegida() {
        //1- Simular el repositorio
        UsuarioRepository repository = mock(UsuarioRepository.class);
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        //2- Preparar una solicitud nueva
        UsuarioService service = new UsuarioService(repository, encoder);

        //3- Simular la respuesta de MongoDB
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        //4- Ejecutar el metodo del Service
        Usuario usuario = service.registrarCliente(
                "Cliente Prueba",
                "CLIENTE@EJEMPLO.CL",
                "ClaveSegura123!",
                "Santiago"
        );
        
        //5- Ejecutar actualizacion
        assertEquals(RolUsuario.CLIENTE, usuario.getRol());
        assertEquals("cliente@ejemplo.cl", usuario.getCorreo());
        assertNotEquals("ClaveSegura123!", usuario.getClaveHash());
        assertTrue(encoder.matches("ClaveSegura123!", usuario.getClaveHash()));
        verify(repository, times(1)).save(any(Usuario.class));
    }

    @Test
    void debeAutenticarUsuarioCorrectamente() {

        UsuarioRepository repository = mock(UsuarioRepository.class);
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        UsuarioService service = new UsuarioService(repository, encoder);

        Usuario usuario = new Usuario();
            usuario.setCorreo("cliente.api@example.com");
            usuario.setRol(RolUsuario.CLIENTE);
            usuario.setClaveHash(encoder.encode("ClaveSegura123!")
        );

        when(repository.findByCorreo(
            "cliente.api@example.com"
        )).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = service.autenticar(
            "CLIENTE.API@EXAMPLE.COM",
            "ClaveSegura123!"
        );

        assertTrue(resultado.isPresent());
        assertEquals(RolUsuario.CLIENTE, resultado.get().getRol());
    }

    @Test
    void debeRechazarClaveIncorrecta() {

        UsuarioRepository repository = mock(UsuarioRepository.class);
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        UsuarioService service = new UsuarioService(repository, encoder);

        Usuario usuario = new Usuario();
        usuario.setCorreo("cliente.api@example.com");
        usuario.setClaveHash(encoder.encode("ClaveSegura123!")
        );

        when(repository.findByCorreo(
            "cliente.api@example.com"
        )).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = service.autenticar(
            "cliente.api@example.com",
            "ClaveIncorrecta"
        );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void debeRechazarUsuarioInexistente() {

        UsuarioRepository repository = mock(UsuarioRepository.class);

        PasswordEncoder encoder = new BCryptPasswordEncoder();

        UsuarioService service = new UsuarioService(repository, encoder);

        when(repository.findByCorreo(
            "noexiste@example.com"
        )).thenReturn(Optional.empty());

        Optional<Usuario> resultado = service.autenticar(
            "noexiste@example.com",
            "ClaveSegura123!"
        );

        assertTrue(resultado.isEmpty());
    }

}
