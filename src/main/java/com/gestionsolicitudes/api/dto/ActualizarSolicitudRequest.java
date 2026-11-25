package com.gestionsolicitudes.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record ActualizarSolicitudRequest(

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 100)
    String titulo,

    @NotBlank(message = "La descripcion es obligatoria")
    @Size(max = 1000)
    String descripcion,

    @Pattern(
        regexp = "Hardware|Software|Redes",
        message = "La categoria debe ser Hardware, Software o Redes"
    )
    String categoria
) {}
