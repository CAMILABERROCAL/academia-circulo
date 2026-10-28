package com.academiacirculo.academia.ciclos.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CicloResponse(
        UUID cicloId,
        String codigo,
        String nombre,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        int duracionMeses,
        String estado) {
}