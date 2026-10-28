package com.academiacirculo.academia.estudiantes.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academiacirculo.academia.estudiantes.dto.IndicadoresAlumnosResponse;
import com.academiacirculo.academia.estudiantes.repository.IndicadoresAlumnosRepository;

@Service
public class IndicadoresAlumnosService {

    private final IndicadoresAlumnosRepository repository;

    public IndicadoresAlumnosService(
            IndicadoresAlumnosRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
    public IndicadoresAlumnosResponse consultar() {
        return repository.consultar();
    }
}