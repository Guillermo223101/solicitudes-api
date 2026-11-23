
package com.gestionsolicitudes.api.repository;

import com.gestionsolicitudes.api.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    // Buscar usuario por correo
    Optional<Usuario> findByCorreo(String correo);

    // Verificar si el correo ya existe
    boolean existsByCorreo(String correo);
}
