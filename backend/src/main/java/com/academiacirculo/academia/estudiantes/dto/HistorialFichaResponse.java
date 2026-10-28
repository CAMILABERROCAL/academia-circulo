package com.academiacirculo.academia.estudiantes.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record HistorialFichaResponse(
        UUID eventoId,
        String realizadoPor,
        String accion,
        OffsetDateTime ocurridoEn) {
}