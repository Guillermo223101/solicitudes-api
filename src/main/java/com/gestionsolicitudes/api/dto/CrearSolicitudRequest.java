package com.gestionsolicitudes.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearSolicitudRequest(

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 100)
    String titulo,

    @NotBlank(message = "La descripcion es obligatoria")
    @Size(max = 1000)
    String descripcion,

    String evidenciaBase64

) {
    // Constructor de compatibilidad para los tests existentes
    public CrearSolicitudRequest(String titulo, String descripcion) {
        this(titulo, descripcion, null);
    }
}
