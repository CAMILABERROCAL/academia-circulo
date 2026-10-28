package com.academiacirculo.academia.ciclos.dto;

import java.time.LocalTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearTurnoRequest(
        @NotBlank @Size(max = 30) @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "El código admite letras, números, guion y guion bajo.") String codigo,

        @NotBlank @Size(max = 100) String nombre,

        @NotNull @Size(min = 1, max = 7, message = "Asigna entre uno y siete días al turno.") List<@NotNull @Valid Horario> horarios) {

    public record Horario(
            @NotNull @Min(1) @Max(7) Integer diaSemana,
            @NotNull LocalTime horaEntrada,
            @NotNull LocalTime horaSalida) {
    }
}