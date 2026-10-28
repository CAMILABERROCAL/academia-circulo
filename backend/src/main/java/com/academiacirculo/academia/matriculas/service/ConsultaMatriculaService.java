package com.academiacirculo.academia.matriculas.service;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.academiacirculo.academia.matriculas.dto.ConsultaMatriculaResponse;
import com.academiacirculo.academia.matriculas.repository.ConsultaMatriculaRepository;
import com.academiacirculo.academia.shared.exception.RecursoNoEncontradoException;

import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class ConsultaMatriculaService {

    private final ConsultaMatriculaRepository repository;

    public ConsultaMatriculaService(
            ConsultaMatriculaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_SECRETARIA')")
    public ConsultaMatriculaResponse consultar(
            @NotNull UUID estudianteId) {

        if (!repository.existeEstudiante(estudianteId)) {
            throw new RecursoNoEncontradoException(
                    "No se encontró el estudiante solicitado.");
        }

        return new ConsultaMatriculaResponse(
                repository.buscarActiva(estudianteId).orElse(null));
    }
}