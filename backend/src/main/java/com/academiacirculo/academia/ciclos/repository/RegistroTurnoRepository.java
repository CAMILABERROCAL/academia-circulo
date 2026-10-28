package com.academiacirculo.academia.ciclos.repository;

import java.sql.Time;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.academiacirculo.academia.ciclos.dto.CrearTurnoRequest;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public class RegistroTurnoRepository {

    private final JdbcTemplate jdbcTemplate;

    public RegistroTurnoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<String> bloquearCiclo(UUID cicloId) {
        return jdbcTemplate.query(
                "SELECT estado FROM ciclos WHERE id = ? FOR UPDATE",
                (rs, fila) -> rs.getString("estado"),
                cicloId)
                .stream()
                .findFirst();
    }

    public boolean existeCodigo(UUID cicloId, String codigo) {
        Boolean existe = jdbcTemplate.queryForObject(
                """
                        SELECT EXISTS (
                            SELECT 1
                            FROM turnos_ciclo
                            WHERE ciclo_id = ? AND codigo = ?
                        )
                        """,
                Boolean.class,
                cicloId,
                codigo);

        return Boolean.TRUE.equals(existe);
    }

    public UUID crearTurno(UUID cicloId, String codigo, String nombre) {
        UUID turnoId = jdbcTemplate.queryForObject(
                """
                        INSERT INTO turnos_ciclo (ciclo_id, codigo, nombre)
                        VALUES (?, ?, ?)
                        RETURNING id
                        """,
                UUID.class,
                cicloId,
                codigo,
                nombre);

        if (turnoId == null) {
            throw new IllegalStateException("No se pudo crear el turno.");
        }

        return turnoId;
    }

    public void crearHorario(
            UUID turnoId,
            CrearTurnoRequest.Horario horario) {

        int filas = jdbcTemplate.update(
                """
                        INSERT INTO horarios_turno (
                            turno_id, dia_semana, hora_entrada, hora_salida
                        )
                        VALUES (?, ?, ?, ?)
                        """,
                turnoId,
                horario.diaSemana(),
                Time.valueOf(horario.horaEntrada()),
                Time.valueOf(horario.horaSalida()));

        if (filas != 1) {
            throw new IllegalStateException(
                    "No se pudo registrar el horario del turno.");
        }
    }

    public void registrarAuditoria(
            UUID responsableId,
            UUID cicloId,
            UUID turnoId,
            String codigo,
            int cantidadDias) {

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
                            'CICLOS',
                            'CREAR_TURNO',
                            'TURNO_CICLO',
                            ?,
                            'EXITOSO',
                            'Creación de turno con horarios de entrada y salida.',
                            jsonb_build_object(
                                'cicloId', CAST(? AS text),
                                'codigo', CAST(? AS text),
                                'cantidadDias', CAST(? AS integer)
                            )
                        )
                        """,
                responsableId,
                turnoId.toString(),
                cicloId.toString(),
                codigo,
                cantidadDias);

        if (filas != 1) {
            throw new IllegalStateException(
                    "No se pudo registrar la auditoría del turno.");
        }
    }
}