package com.gestionsolicitudes.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroUsuarioRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
    String nombre,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es valido")
    @Pattern(
    regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
    message = "Ingresa un correo valido (ej: usuario@dominio.cl)"
    )
    String correo,

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 6, max = 64, message = "La contrasena debe tener al menos 6 caracteres")
    @Pattern(regexp = "^\\S+$", message = "La contrasena no puede contener espacios")
    String clave,

    @NotBlank(message = "La direccion es obligatoria")
    @Size(min = 5, max = 150, message = "La direccion debe tener entre 5 y 150 caracteres")
    String direccion
) {}