package com.academiacirculo.academia.estudiantes.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record VincularApoderadoRequest(
        @NotNull(message = "El apoderado es obligatorio.") UUID apoderadoId,

        @NotBlank(message = "El parentesco es obligatorio.") @Pattern(regexp = "PADRE|MADRE|TUTOR|OTRO", message = "El parentesco debe ser PADRE, MADRE, TUTOR u OTRO.") String parentesco) {
}