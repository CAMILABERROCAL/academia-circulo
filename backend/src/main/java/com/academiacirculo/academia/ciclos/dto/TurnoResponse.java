package com.academiacirculo.academia.ciclos.dto;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record TurnoResponse(
        UUID turnoId,
        UUID cicloId,
        String codigo,
        String nombre,
        String estado,
        List<HorarioResponse> horarios) {

    public record HorarioResponse(
            UUID horarioId,
            int diaSemana,
            LocalTime horaEntrada,
            LocalTime horaSalida) {
    }
}