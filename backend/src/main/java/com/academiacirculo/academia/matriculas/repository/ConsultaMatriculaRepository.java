package com.academiacirculo.academia.matriculas.repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.academiacirculo.academia.ciclos.dto.TurnoResponse.HorarioResponse;
import com.academiacirculo.academia.matriculas.dto.ConsultaMatriculaResponse.Matricula;

@Repository
public class ConsultaMatriculaRepository {

    private final JdbcTemplate jdbcTemplate;

    public ConsultaMatriculaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existeEstudiante(UUID estudianteId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM estudiantes
                    WHERE id = ?
                )
                """;

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(
                        sql,
                        Boolean.class,
                        estudianteId));
    }

    public Optional<Matricula> buscarActiva(UUID estudianteId) {
        String sql = """
                SELECT
                    m.id AS matricula_id,
                    m.codigo AS codigo_matricula,
                    m.estado,
                    m.fecha_matricula,
                    c.codigo AS codigo_ciclo,
                    c.nombre AS nombre_ciclo,
                    c.fecha_inicio,
                    c.fecha_fin,
                    t.codigo AS codigo_turno,
                    t.nombre AS nombre_turno,
                    u.nombre_usuario AS registrado_por,
                    auditoria.ocurrido_en AS registrado_en,
                    h.id AS horario_id,
                    h.dia_semana,
                    h.hora_entrada,
                    h.hora_salida
                FROM matriculas m
                JOIN ciclos c ON c.id = m.ciclo_id
                JOIN turnos_ciclo t
                    ON t.id = m.turno_id
                   AND t.ciclo_id = m.ciclo_id
                LEFT JOIN LATERAL (
                    SELECT ae.usuario_id, ae.ocurrido_en
                    FROM auditoria_eventos ae
                    WHERE ae.entidad_tipo = 'MATRICULA'
                      AND ae.entidad_id = m.id::text
                      AND ae.modulo = 'MATRICULAS'
                      AND ae.accion = 'REGISTRAR_MATRICULA'
                      AND ae.resultado = 'EXITOSO'
                    ORDER BY ae.ocurrido_en, ae.id
                    LIMIT 1
                ) auditoria ON TRUE
                LEFT JOIN usuarios u
                    ON u.id = auditoria.usuario_id
                LEFT JOIN horarios_turno h
                    ON h.turno_id = t.id
                WHERE m.estudiante_id = ?
                  AND m.estado = 'ACTIVA'
                ORDER BY h.dia_semana, h.id
                """;

        return jdbcTemplate.query(
                sql,
                rs -> {
                    if (!rs.next()) {
                        return Optional.<Matricula>empty();
                    }

                    UUID matriculaId = rs.getObject(
                            "matricula_id", UUID.class);
                    String codigoMatricula = rs.getString(
                            "codigo_matricula");
                    String estado = rs.getString("estado");
                    LocalDate fechaMatricula = rs.getObject(
                            "fecha_matricula", LocalDate.class);

                    String codigoCiclo = rs.getString("codigo_ciclo");
                    String nombreCiclo = rs.getString("nombre_ciclo");
                    LocalDate fechaInicio = rs.getObject(
                            "fecha_inicio", LocalDate.class);
                    LocalDate fechaFin = rs.getObject(
                            "fecha_fin", LocalDate.class);

                    String codigoTurno = rs.getString("codigo_turno");
                    String nombreTurno = rs.getString("nombre_turno");
                    String registradoPor = rs.getString("registrado_por");
                    OffsetDateTime registradoEn = rs.getObject(
                            "registrado_en", OffsetDateTime.class);

                    List<HorarioResponse> horarios = new ArrayList<>();

                    do {
                        UUID horarioId = rs.getObject(
                                "horario_id", UUID.class);

                        if (horarioId != null) {
                            horarios.add(new HorarioResponse(
                                    horarioId,
                                    rs.getInt("dia_semana"),
                                    rs.getTime("hora_entrada").toLocalTime(),
                                    rs.getTime("hora_salida").toLocalTime()));
                        }
                    } while (rs.next());

                    return Optional.of(new Matricula(
                            matriculaId,
                            codigoMatricula,
                            estado,
                            fechaMatricula,
                            codigoCiclo,
                            nombreCiclo,
                            fechaInicio,
                            fechaFin,
                            codigoTurno,
                            nombreTurno,
                            registradoPor,
                            registradoEn,
                            List.copyOf(horarios)));
                },
                estudianteId);
    }
}