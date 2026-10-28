ALTER TABLE matriculas
    ADD COLUMN turno_id UUID NOT NULL;

ALTER TABLE matriculas
    ADD CONSTRAINT fk_matriculas_turno_ciclo
    FOREIGN KEY (turno_id, ciclo_id)
    REFERENCES turnos_ciclo (id, ciclo_id)
    ON DELETE RESTRICT;

CREATE INDEX ix_matriculas_turno_ciclo
    ON matriculas (turno_id, ciclo_id);

CREATE UNIQUE INDEX ux_matriculas_una_activa_por_estudiante
    ON matriculas (estudiante_id)
    WHERE estado = 'ACTIVA';

COMMENT ON COLUMN matriculas.turno_id IS
    'Único turno seleccionado para la matrícula; debe pertenecer al mismo ciclo.';