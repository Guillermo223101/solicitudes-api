package com.gestionsolicitudes.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record CrearSolicitudRequest(

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 100)
    String titulo,

    @NotBlank(message = "La descripcion es obligatoria")
    @Size(max = 1000)
    String descripcion,

    @Pattern(
        regexp = "Hardware|Software|Redes",
        message = "La categoria debe ser Hardware, Sofware o Redes"
    )
    String categoria,

    @Size(
        max = 1400000,
        message = "La evidencia fotografica supera el limite permitido"
    )
    String evidenciaBase64

) {
    // Compatibilidad con llamadas anteriores
    public CrearSolicitudRequest(
            String titulo,
            String descripcion,
            String categoria) {

        this(titulo, descripcion, categoria, null);
    }

    public CrearSolicitudRequest(
            String titulo,
            String descripcion) {

        this(titulo, descripcion, null, null);
    }
}
