package com.academiacirculo.academia.estudiantes.dto;

public record IndicadoresAlumnosResponse(
        long totalAlumnos,
        long conMatriculaActiva,
        long sinMatriculaActiva) {
}