package com.academiacirculo.academia.shared.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        Instant fechaHora,
        int estado,
        String error,
        String mensaje,
        String ruta,
        Map<String, String> detalles) {
}