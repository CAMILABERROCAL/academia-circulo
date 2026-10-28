package com.academiacirculo.academia.apoderados.dto;

import java.util.List;

public record PaginaApoderadosResponse(
        List<ApoderadoResumenResponse> apoderados,
        int pagina,
        int tamanio,
        long totalElementos,
        long totalPaginas) {
}