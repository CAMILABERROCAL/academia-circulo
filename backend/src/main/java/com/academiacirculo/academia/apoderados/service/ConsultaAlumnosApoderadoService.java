package com.academiacirculo.academia.apoderados.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academiacirculo.academia.apoderados.dto.AlumnoVinculadoResponse;
import com.academiacirculo.academia.apoderados.repository.ConsultaAlumnosApoderadoRepository;
import com.academiacirculo.academia.shared.exception.RecursoNoEncontradoException;

@Service
public class ConsultaAlumnosApoderadoService {

    private final ConsultaAlumnosApoderadoRepository repository;

    public ConsultaAlumnosApoderadoService(
            ConsultaAlumnosApoderadoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
    public List<AlumnoVinculadoResponse> consultar(UUID apoderadoId) {
        if (!repository.existeApoderado(apoderadoId)) {
            throw new RecursoNoEncontradoException(
                    "No se encontró el apoderado solicitado.");
        }

        return repository.listarAlumnos(apoderadoId);
    }
}