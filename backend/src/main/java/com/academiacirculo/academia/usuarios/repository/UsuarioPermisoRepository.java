package com.academiacirculo.academia.usuarios.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UsuarioPermisoRepository {

    private final JdbcTemplate jdbcTemplate;

    public UsuarioPermisoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> buscarCodigosPorUsuarioId(UUID usuarioId) {
        String sql = """
                SELECT p.codigo
                FROM usuario_permisos up
                INNER JOIN permisos p ON p.id = up.permiso_id
                WHERE up.usuario_id = ?
                ORDER BY p.codigo
                """;

        return jdbcTemplate.queryForList(
                sql,
                String.class,
                usuarioId);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public boolean otorgarPermisoNotas(
            UUID usuarioId,
            UUID asignadoPor,
            String motivo) {

        String sql = """
                INSERT INTO usuario_permisos (
                    usuario_id,
                    permiso_id,
                    asignado_por,
                    motivo
                )
                VALUES (
                    ?,
                    (SELECT id FROM permisos WHERE codigo = 'NOTAS_EDITAR'),
                    ?,
                    ?
                )
                ON CONFLICT (usuario_id, permiso_id) DO NOTHING
                """;

        int filas = jdbcTemplate.update(
                sql,
                usuarioId,
                asignadoPor,
                motivo);

        return filas == 1;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public boolean revocarPermisoNotas(UUID usuarioId) {
        String sql = """
                DELETE FROM usuario_permisos up
                USING permisos p
                WHERE up.permiso_id = p.id
                  AND up.usuario_id = ?
                  AND p.codigo = 'NOTAS_EDITAR'
                """;

        int filas = jdbcTemplate.update(sql, usuarioId);

        return filas == 1;
    }
}