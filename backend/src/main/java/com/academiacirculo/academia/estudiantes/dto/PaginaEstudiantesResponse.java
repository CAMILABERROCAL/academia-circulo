package com.academiacirculo.academia.estudiantes.dto;

import java.util.List;

public record PaginaEstudiantesResponse(
        List<EstudianteResumenResponse> estudiantes,
        int pagina,
        int tamanio,
        long totalElementos,
        long totalPaginas) {
}