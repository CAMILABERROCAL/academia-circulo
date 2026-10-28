package com.academiacirculo.academia.ciclos.repository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.academiacirculo.academia.ciclos.dto.TurnoResponse;
import com.academiacirculo.academia.ciclos.dto.TurnoResponse.HorarioResponse;

@Repository
public class ConsultaTurnoRepository {

    private final JdbcTemplate jdbcTemplate;

    public ConsultaTurnoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existeCiclo(UUID cicloId) {
        Boolean existe = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM ciclos WHERE id = ?)",
                Boolean.class,
                cicloId);

        return Boolean.TRUE.equals(existe);
    }

    public List<TurnoResponse> listarPorCiclo(UUID cicloId) {
        String sql = """
                SELECT t.id AS turno_id,
                       t.ciclo_id,
                       t.codigo,
                       t.nombre,
                       t.estado,
                       h.id AS horario_id,
                       h.dia_semana,
                       h.hora_entrada,
                       h.hora_salida
                FROM turnos_ciclo t
                LEFT JOIN horarios_turno h ON h.turno_id = t.id
                WHERE t.ciclo_id = ?
                ORDER BY t.codigo, t.id, h.dia_semana
                """;

        return jdbcTemplate.query(
                sql,
                rs -> {
                    var turnos = new LinkedHashMap<UUID, TurnoResponse>();

                    while (rs.next()) {
                        UUID turnoId = rs.getObject("turno_id", UUID.class);
                        TurnoResponse turno = turnos.get(turnoId);

                        if (turno == null) {
                            turno = new TurnoResponse(
                                    turnoId,
                                    rs.getObject("ciclo_id", UUID.class),
                                    rs.getString("codigo"),
                                    rs.getString("nombre"),
                                    rs.getString("estado"),
                                    new ArrayList<>());

                            turnos.put(turnoId, turno);
                        }

                        UUID horarioId = rs.getObject("horario_id", UUID.class);

                        if (horarioId != null) {
                            turno.horarios().add(new HorarioResponse(
                                    horarioId,
                                    rs.getInt("dia_semana"),
                                    rs.getObject("hora_entrada", LocalTime.class),
                                    rs.getObject("hora_salida", LocalTime.class)));
                        }
                    }

                    return new ArrayList<>(turnos.values());
                },
                cicloId);
    }
}