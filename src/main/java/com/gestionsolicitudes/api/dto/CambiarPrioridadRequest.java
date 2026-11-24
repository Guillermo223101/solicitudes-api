package com.gestionsolicitudes.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CambiarPrioridadRequest(
    @NotBlank (message = "La prioridad es obligatorio")
    String prioridad
) {}
