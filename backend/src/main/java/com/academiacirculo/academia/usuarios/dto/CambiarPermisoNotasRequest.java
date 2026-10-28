package com.academiacirculo.academia.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarPermisoNotasRequest(

        @NotBlank(message = "El motivo es obligatorio.") @Size(max = 500, message = "El motivo no debe superar 500 caracteres.") String motivo

) {
}