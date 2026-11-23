package com.gestionsolicitudes.api.dto;

public record LoginUsuarioResponse(
    String token,
    String tipo,
    long expiraEnSegundos,
    UsuarioRegistradoResponse usuario
) {}
