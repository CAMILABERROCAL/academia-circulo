package com.academiacirculo.academia.estudiantes.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academiacirculo.academia.estudiantes.dto.AlumnoRecienteResponse;
import com.academiacirculo.academia.estudiantes.repository.AlumnosRecientesRepository;

@Service
public class AlumnosRecientesService {

    private final AlumnosRecientesRepository repository;

    public AlumnosRecientesService(
            AlumnosRecientesRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
    public List<AlumnoRecienteResponse> consultar() {
        return repository.listarUltimosCinco();
    }
}