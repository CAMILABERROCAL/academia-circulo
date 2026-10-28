package com.academiacirculo.academia.ciclos.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academiacirculo.academia.ciclos.dto.CicloResponse;
import com.academiacirculo.academia.ciclos.repository.CicloRepository;

@Service
public class CicloService {

    private final CicloRepository cicloRepository;

    public CicloService(CicloRepository cicloRepository) {
        this.cicloRepository = cicloRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SECRETARIA', 'ROLE_DIRECTOR')")
    public List<CicloResponse> listar(int pagina, int tamanio) {
        if (pagina < 0) {
            throw new IllegalArgumentException(
                    "La página no puede ser negativa.");
        }

        if (tamanio < 1 || tamanio > 100) {
            throw new IllegalArgumentException(
                    "El tamaño de página debe estar entre 1 y 100.");
        }

        long desplazamiento = (long) pagina * tamanio;

        return cicloRepository.listar(tamanio, desplazamiento);
    }
}