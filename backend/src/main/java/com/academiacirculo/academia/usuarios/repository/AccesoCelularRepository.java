package com.academiacirculo.academia.usuarios.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.academiacirculo.academia.auth.dto.TipoAcceso;

@Repository
public class AccesoCelularRepository {

    private final JdbcTemplate jdbcTemplate;

    public AccesoCelularRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<UUID> buscarCandidatos(
            TipoAcceso tipoAcceso,
            String celular,
            String codigoAlumno) {

        if (tipoAcceso == null) {
            throw new IllegalArgumentException(
                    "El tipo de acceso es obligatorio.");
        }

        if (celular == null
                || !celular.matches("^(?:\\+51)?9[0-9]{8}$")) {

            throw new IllegalArgumentException(
                    "El celular no tiene un formato válido.");
        }

        String numeroLocal = celular.startsWith("+51")
                ? celular.substring(3)
                : celular;

        String numeroInternacional = "+51" + numeroLocal;

        String condicion = switch (tipoAcceso) {
            case ALUMNO -> """
                    EXISTS (
                        SELECT 1
                        FROM usuario_roles ur
                        JOIN roles r ON r.id = ur.rol_id
                        WHERE ur.usuario_id = u.id
                          AND r.codigo = 'ROLE_ESTUDIANTE'
                    )
                    AND EXISTS (
                        SELECT 1
                        FROM estudiantes e
                        WHERE e.usuario_id = u.id
                          AND LOWER(e.codigo_estudiante) = LOWER(?)
                    )
                    """;

            case APODERADO -> """
                    EXISTS (
                        SELECT 1
                        FROM usuario_roles ur
                        JOIN roles r ON r.id = ur.rol_id
                        WHERE ur.usuario_id = u.id
                          AND r.codigo = 'ROLE_APODERADO'
                    )
                    """;

            case PERSONAL -> """
                    EXISTS (
                        SELECT 1
                        FROM usuario_roles ur
                        JOIN roles r ON r.id = ur.rol_id
                        WHERE ur.usuario_id = u.id
                          AND r.codigo IN (
                              'ROLE_ADMIN',
                              'ROLE_DIRECTOR',
                              'ROLE_SECRETARIA',
                              'ROLE_PORTERO'
                          )
                    )
                    """;
        };

        String sql = """
                SELECT u.id
                FROM usuarios u
                WHERE u.telefono IN (?, ?)
                  AND (%s)
                ORDER BY u.id
                LIMIT 2
                """.formatted(condicion);

        if (tipoAcceso == TipoAcceso.ALUMNO) {
            if (codigoAlumno == null || codigoAlumno.isBlank()) {
                throw new IllegalArgumentException(
                        "El código de alumno es obligatorio.");
            }

            return jdbcTemplate.query(
                    sql,
                    (rs, fila) -> rs.getObject("id", UUID.class),
                    numeroLocal,
                    numeroInternacional,
                    codigoAlumno.strip());
        }

        return jdbcTemplate.query(
                sql,
                (rs, fila) -> rs.getObject("id", UUID.class),
                numeroLocal,
                numeroInternacional);
    }
}