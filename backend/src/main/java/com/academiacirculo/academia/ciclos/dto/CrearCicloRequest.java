package com.academiacirculo.academia.ciclos.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearCicloRequest(
        @NotBlank(message = "El código es obligatorio.") @Size(max = 30) @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "El código admite letras, números, guion y guion bajo.") String codigo,

        @NotBlank(message = "El nombre es obligatorio.") @Size(max = 120) String nombre,

        @NotNull(message = "La fecha de inicio es obligatoria.") LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria.") LocalDate fechaFin,

        @NotNull(message = "La duración es obligatoria.") @Min(value = 1, message = "La duración mínima es 1 mes.") @Max(value = 24, message = "La duración máxima es 24 meses.") Integer duracionMeses) {

    @JsonIgnore
    @AssertTrue(message = "La fecha de fin no puede ser anterior al inicio.")
    public boolean isRangoFechasValido() {
        return fechaInicio == null
                || fechaFin == null
                || !fechaFin.isBefore(fechaInicio);
    }
}