package com.academiacirculo.academia.estudiantes.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class VinculoApoderadoRepository {

    private final JdbcTemplate jdbcTemplate;

    public VinculoApoderadoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UUID> buscarUsuarioApoderado(UUID apoderadoId) {
        List<UUID> resultados = jdbcTemplate.query(
                """
                        SELECT usuario_id
                        FROM apoderados
                        WHERE id = ?
                        """,
                (rs, fila) -> rs.getObject("usuario_id", UUID.class),
                apoderadoId);

        return resultados.stream().findFirst();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void vincular(
            UUID estudianteId,
            UUID apoderadoId,
            String parentesco) {

        int filas = jdbcTemplate.update(
                """
                        INSERT INTO estudiante_apoderados (
                            estudiante_id,
                            apoderado_id,
                            parentesco,
                            es_principal,
                            recibe_notificaciones
                        )
                        VALUES (?, ?, ?, TRUE, TRUE)
                        """,
                estudianteId,
                apoderadoId,
                parentesco);

        if (filas != 1) {
            throw new IllegalStateException(
                    "No se pudo vincular al apoderado.");
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarAuditoria(
            UUID responsableId,
            UUID estudianteId,
            UUID apoderadoId,
            String parentesco) {

        int filas = jdbcTemplate.update(
                """
                        INSERT INTO auditoria_eventos (
                            usuario_id,
                            modulo,
                            accion,
                            entidad_tipo,
                            entidad_id,
                            resultado,
                            descripcion,
                            metadata
                        )
                        VALUES (
                            ?,
                            'ESTUDIANTES',
                            'VINCULAR_APODERADO',
                            'ESTUDIANTE',
                            ?,
                            'EXITOSO',
                            'Vinculación inicial de apoderado.',
                            jsonb_build_object(
                                'apoderadoId', CAST(? AS text),
                                'parentesco', CAST(? AS text)
                            )
                        )
                        """,
                responsableId,
                estudianteId.toString(),
                apoderadoId.toString(),
                parentesco);

        if (filas != 1) {
            throw new IllegalStateException(
                    "No se pudo registrar la auditoría de vinculación.");
        }
    }
}