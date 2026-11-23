package com.gestionsolicitudes.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroUsuarioRequest(
    @NotBlank(message = "El nombre es obligatorio")
    String nombre,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es valido")
    String correo,

    @NotBlank(message = "La clave es obligatoria")
    @Size(min = 8, message = "La clave debe tener al menos 8 caracteres")
    String clave,

    String direccion
) {}
