package com.academiacirculo.academia.matriculas.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record RegistrarMatriculaRequest(
        @NotNull(message = "Selecciona un estudiante.") UUID estudianteId,

        @NotNull(message = "Selecciona un ciclo.") UUID cicloId,

        @NotNull(message = "Selecciona un turno.") UUID turnoId) {
}