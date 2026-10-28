package com.academiacirculo.academia.ciclos.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academiacirculo.academia.ciclos.dto.TurnoResponse;
import com.academiacirculo.academia.ciclos.repository.ConsultaTurnoRepository;
import com.academiacirculo.academia.shared.exception.RecursoNoEncontradoException;

@Service
public class ConsultaTurnoService {

    private final ConsultaTurnoRepository repository;

    public ConsultaTurnoService(ConsultaTurnoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_DIRECTOR', 'ROLE_SECRETARIA')")
    public List<TurnoResponse> listar(UUID cicloId) {
        if (!repository.existeCiclo(cicloId)) {
            throw new RecursoNoEncontradoException(
                    "No se encontró el ciclo solicitado.");
        }

        return repository.listarPorCiclo(cicloId);
    }
}