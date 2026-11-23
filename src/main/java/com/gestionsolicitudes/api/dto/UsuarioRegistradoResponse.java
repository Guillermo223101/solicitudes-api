package com.gestionsolicitudes.api.dto;

import com.gestionsolicitudes.api.model.RolUsuario;

public record UsuarioRegistradoResponse(
    String id,
    String nombre,
    String correo,
    String direccion,
    RolUsuario rol
) {}
