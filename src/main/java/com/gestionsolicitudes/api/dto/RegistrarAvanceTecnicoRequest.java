package com.gestionsolicitudes.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrarAvanceTecnicoRequest(

    @NotBlank(message = "El comentario es obligatorio")
    @Size(max = 1000)
    String comentario

) {}