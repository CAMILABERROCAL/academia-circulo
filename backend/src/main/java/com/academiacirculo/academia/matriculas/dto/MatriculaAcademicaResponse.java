package com.academiacirculo.academia.matriculas.dto;

import java.time.LocalDate;
import java.util.List;

import com.academiacirculo.academia.ciclos.dto.TurnoResponse.HorarioResponse;

public record MatriculaAcademicaResponse(
        Matricula matricula) {

    public record Matricula(
            String codigoMatricula,
            String estado,
            LocalDate fechaMatricula,
            String codigoCiclo,
            String nombreCiclo,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String codigoTurno,
            String nombreTurno,
            List<HorarioResponse> horarios) {
    }

    public static MatriculaAcademicaResponse desde(
            ConsultaMatriculaResponse.Matricula origen) {

        if (origen == null) {
            return new MatriculaAcademicaResponse(null);
        }

        return new MatriculaAcademicaResponse(
                new Matricula(
                        origen.codigoMatricula(),
                        origen.estado(),
                        origen.fechaMatricula(),
                        origen.codigoCiclo(),
                        origen.nombreCiclo(),
                        origen.fechaInicio(),
                        origen.fechaFin(),
                        origen.codigoTurno(),
                        origen.nombreTurno(),
                        origen.horarios()));
    }
}