package com.academiacirculo.academia.matriculas.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.academiacirculo.academia.ciclos.dto.TurnoResponse.HorarioResponse;

public record ConsultaMatriculaResponse(
        Matricula matricula) {

    public record Matricula(
            UUID matriculaId,
            String codigoMatricula,
            String estado,
            LocalDate fechaMatricula,
            String codigoCiclo,
            String nombreCiclo,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String codigoTurno,
            String nombreTurno,
            String registradoPor,
            OffsetDateTime registradoEn,
            List<HorarioResponse> horarios) {
    }
}