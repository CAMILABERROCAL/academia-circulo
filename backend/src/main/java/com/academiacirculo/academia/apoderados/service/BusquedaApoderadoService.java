package com.academiacirculo.academia.apoderados.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academiacirculo.academia.apoderados.dto.ApoderadoResumenResponse;
import com.academiacirculo.academia.apoderados.repository.BusquedaApoderadoRepository;

@Service
public class BusquedaApoderadoService {

    private final BusquedaApoderadoRepository busquedaRepository;

    public BusquedaApoderadoService(
            BusquedaApoderadoRepository busquedaRepository) {

        this.busquedaRepository = busquedaRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
    public List<ApoderadoResumenResponse> buscar(
            String busqueda,
            int pagina,
            int tamanio) {

        if (pagina < 0) {
            throw new IllegalArgumentException(
                    "La página no puede ser negativa.");
        }

        if (tamanio < 1 || tamanio > 100) {
            throw new IllegalArgumentException(
                    "El tamaño de página debe estar entre 1 y 100.");
        }

        String texto = busqueda == null ? "" : busqueda.strip();

        if (texto.length() > 120) {
            throw new IllegalArgumentException(
                    "La búsqueda no debe superar 120 caracteres.");
        }

        long desplazamiento = (long) pagina * tamanio;

        return busquedaRepository.listar(
                texto,
                tamanio,
                desplazamiento);
    }
}