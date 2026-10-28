package com.academiacirculo.academia.estudiantes.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.academiacirculo.academia.estudiantes.dto.EstudianteResumenResponse;
import com.academiacirculo.academia.estudiantes.dto.FichaEstudianteResponse;

@Repository
public class FichaEstudianteRepository {

        private final JdbcTemplate jdbcTemplate;

        public FichaEstudianteRepository(JdbcTemplate jdbcTemplate) {
                this.jdbcTemplate = jdbcTemplate;
        }

        @Transactional(propagation = Propagation.MANDATORY)
        public Optional<UUID> bloquearYBuscarUsuarioId(UUID estudianteId) {
                String sql = """
                                SELECT usuario_id
                                FROM estudiantes
                                WHERE id = ?
                                FOR UPDATE
                                """;

                List<UUID> resultados = jdbcTemplate.query(
                                sql,
                                (rs, fila) -> rs.getObject("usuario_id", UUID.class),
                                estudianteId);

                return resultados.stream().findFirst();
        }

        public boolean tieneApoderado(UUID estudianteId) {
                String sql = """
                                SELECT EXISTS (
                                    SELECT 1
                                    FROM estudiante_apoderados
                                    WHERE estudiante_id = ?
                                )
                                """;

                Boolean existe = jdbcTemplate.queryForObject(
                                sql,
                                Boolean.class,
                                estudianteId);

                return Boolean.TRUE.equals(existe);
        }

        @Transactional(propagation = Propagation.MANDATORY)
        public void actualizarDatosFicha(
                        UUID estudianteId,
                        LocalDate fechaNacimiento,
                        String direccionReferencial,
                        boolean sigueTratamientoMedico) {

                String sql = """
                                UPDATE estudiantes
                                SET fecha_nacimiento = ?,
                                    direccion_referencial = ?,
                                    sigue_tratamiento_medico = ?,
                                    actualizado_en = CURRENT_TIMESTAMP,
                                    version = version + 1
                                WHERE id = ?
                                """;

                int filas = jdbcTemplate.update(
                                sql,
                                Date.valueOf(fechaNacimiento),
                                direccionReferencial,
                                sigueTratamientoMedico,
                                estudianteId);

                if (filas != 1) {
                        throw new IllegalStateException(
                                        "No se pudo actualizar la ficha del estudiante.");
                }
        }

        public Optional<FichaEstudianteResponse> buscarFicha(UUID estudianteId) {
                String sql = """
                                SELECT e.id AS estudiante_id,
                                       e.codigo_estudiante,
                                       u.nombres,
                                       u.apellidos,
                                       e.fecha_nacimiento,
                                       u.correo,
                                       u.telefono,
                                       e.direccion_referencial,
                                       e.sigue_tratamiento_medico,
                                       (ea.apoderado_id IS NOT NULL) AS tiene_apoderado,
                                       NULLIF(
                                           CONCAT_WS(' ', ua.nombres, ua.apellidos),
                                           ''
                                       ) AS nombre_apoderado,
                                       ua.telefono AS celular_apoderado,
                                       ea.parentesco AS parentesco_apoderado
                                FROM estudiantes e
                                JOIN usuarios u ON u.id = e.usuario_id
                                LEFT JOIN estudiante_apoderados ea
                                    ON ea.estudiante_id = e.id
                                LEFT JOIN apoderados a
                                    ON a.id = ea.apoderado_id
                                LEFT JOIN usuarios ua
                                    ON ua.id = a.usuario_id
                                WHERE e.id = ?
                                """;

                List<FichaEstudianteResponse> resultados = jdbcTemplate.query(
                                sql,
                                (rs, fila) -> new FichaEstudianteResponse(
                                                rs.getObject("estudiante_id", UUID.class),
                                                rs.getString("codigo_estudiante"),
                                                rs.getString("nombres"),
                                                rs.getString("apellidos"),
                                                rs.getObject("fecha_nacimiento", LocalDate.class),
                                                rs.getString("correo"),
                                                rs.getString("telefono"),
                                                rs.getString("direccion_referencial"),
                                                rs.getObject("sigue_tratamiento_medico", Boolean.class),
                                                rs.getBoolean("tiene_apoderado"),
                                                rs.getString("nombre_apoderado"),
                                                rs.getString("celular_apoderado"),
                                                rs.getString("parentesco_apoderado")),
                                estudianteId);

                return resultados.stream().findFirst();
        }

        public List<EstudianteResumenResponse> listarPagina(
                        int tamanio,
                        long desplazamiento,
                        String busqueda) {

                String sql = """
                                SELECT e.id AS estudiante_id,
                                       e.codigo_estudiante,
                                       u.nombres,
                                       u.apellidos,
                                       EXISTS (
                                           SELECT 1
                                           FROM matriculas m
                                           WHERE m.estudiante_id = e.id
                                             AND m.estado = 'ACTIVA'
                                       ) AS tiene_matricula_activa
                                FROM estudiantes e
                                JOIN usuarios u ON u.id = e.usuario_id
                                WHERE (
                                    e.codigo_estudiante ILIKE ? ESCAPE '!'
                                    OR CONCAT_WS(' ', u.nombres, u.apellidos)
                                        ILIKE ? ESCAPE '!'
                                )
                                ORDER BY u.apellidos, u.nombres, e.id
                                LIMIT ? OFFSET ?
                                """;

                String patron = crearPatronBusqueda(busqueda);

                return jdbcTemplate.query(
                                sql,
                                (rs, fila) -> new EstudianteResumenResponse(
                                                rs.getObject("estudiante_id", UUID.class),
                                                rs.getString("codigo_estudiante"),
                                                rs.getString("nombres"),
                                                rs.getString("apellidos"),
                                                rs.getBoolean("tiene_matricula_activa")),
                                patron,
                                patron,
                                tamanio,
                                desplazamiento);
        }

        public long contarEstudiantes(String busqueda) {
                String sql = """
                                SELECT COUNT(*)
                                FROM estudiantes e
                                JOIN usuarios u ON u.id = e.usuario_id
                                WHERE (
                                    e.codigo_estudiante ILIKE ? ESCAPE '!'
                                    OR CONCAT_WS(' ', u.nombres, u.apellidos)
                                        ILIKE ? ESCAPE '!'
                                )
                                """;

                String patron = crearPatronBusqueda(busqueda);

                Long total = jdbcTemplate.queryForObject(
                                sql,
                                Long.class,
                                patron,
                                patron);

                return total == null ? 0L : total;
        }

        private String crearPatronBusqueda(String busqueda) {
                String texto = busqueda
                                .replace("!", "!!")
                                .replace("%", "!%")
                                .replace("_", "!_");

                return "%" + texto + "%";
        }
}