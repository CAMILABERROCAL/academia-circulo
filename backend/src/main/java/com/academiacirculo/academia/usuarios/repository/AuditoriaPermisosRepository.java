package com.academiacirculo.academia.usuarios.repository;

import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AuditoriaPermisosRepository {

    private final JdbcTemplate jdbcTemplate;

    public AuditoriaPermisosRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarOtorgamiento(
            UUID directorId,
            UUID usuarioId,
            String motivo) {

        registrar(
                directorId,
                usuarioId,
                "OTORGAR_NOTAS_EDITAR",
                motivo);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarRevocacion(
            UUID directorId,
            UUID usuarioId,
            String motivo) {

        registrar(
                directorId,
                usuarioId,
                "REVOCAR_NOTAS_EDITAR",
                motivo);
    }

    private void registrar(
            UUID directorId,
            UUID usuarioId,
            String accion,
            String motivo) {

        String sql = """
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
                    'PERMISOS',
                    ?,
                    'USUARIO',
                    ?,
                    'EXITOSO',
                    ?,
                    jsonb_build_object('permiso', 'NOTAS_EDITAR')
                )
                """;

        jdbcTemplate.update(
                sql,
                directorId,
                accion,
                usuarioId.toString(),
                motivo);
    }
}