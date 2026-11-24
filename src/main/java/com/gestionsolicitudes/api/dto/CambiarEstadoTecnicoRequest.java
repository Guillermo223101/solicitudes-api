package com.gestionsolicitudes.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CambiarEstadoTecnicoRequest(
    @NotBlank(message = "El estado es obligatorio")
    String estado
) {}