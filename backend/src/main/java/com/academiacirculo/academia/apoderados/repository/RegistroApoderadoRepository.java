package com.academiacirculo.academia.apoderados.repository;

import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class RegistroApoderadoRepository {

    private final JdbcTemplate jdbcTemplate;

    public RegistroApoderadoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void bloquearRegistroPorCelular(String celular) {
        // El bloqueo se libera al finalizar la transacción.
        jdbcTemplate.query(
                "SELECT pg_advisory_xact_lock(18301, hashtext(?))",
                rs -> {
                    return null;
                },
                celular);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public UUID crearPerfil(UUID usuarioId) {
        UUID apoderadoId = UUID.randomUUID();

        int filas = jdbcTemplate.update(
                """
                        INSERT INTO apoderados (id, usuario_id)
                        VALUES (?, ?)
                        """,
                apoderadoId,
                usuarioId);

        if (filas != 1) {
            throw new IllegalStateException(
                    "No se pudo crear el perfil del apoderado.");
        }

        return apoderadoId;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarAuditoria(
            UUID responsableId,
            UUID apoderadoId) {

        int filas = jdbcTemplate.update(
                """
                        INSERT INTO auditoria_eventos (
                            usuario_id,
                            modulo,
                            accion,
                            entidad_tipo,
                            entidad_id,
                            resultado,
                            descripcion
                        )
                        VALUES (
                            ?,
                            'APODERADOS',
                            'REGISTRAR_APODERADO',
                            'APODERADO',
                            ?,
                            'EXITOSO',
                            'Registro de cuenta y perfil del apoderado.'
                        )
                        """,
                responsableId,
                apoderadoId.toString());

        if (filas != 1) {
            throw new IllegalStateException(
                    "No se pudo registrar la auditoría del apoderado.");
        }
    }
}