package com.academiacirculo.academia.matriculas.dto;

import java.time.LocalDate;
import java.util.UUID;

public record MatriculaRegistradaResponse(
        UUID matriculaId,
        String codigoMatricula,
        UUID estudianteId,
        UUID cicloId,
        UUID turnoId,
        LocalDate fechaMatricula,
        String estado) {
}