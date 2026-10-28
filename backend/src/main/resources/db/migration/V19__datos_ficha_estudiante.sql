ALTER TABLE estudiantes
    ADD COLUMN direccion_referencial VARCHAR(255),
    ADD COLUMN sigue_tratamiento_medico BOOLEAN;

ALTER TABLE estudiantes
    ADD CONSTRAINT ck_estudiantes_direccion_no_vacia
    CHECK (
        direccion_referencial IS NULL
        OR BTRIM(direccion_referencial) <> ''
    );

COMMENT ON COLUMN estudiantes.direccion_referencial IS
    'Dirección referencial declarada en la ficha del estudiante.';

COMMENT ON COLUMN estudiantes.sigue_tratamiento_medico IS
    'Respuesta declarada: true sí, false no, null todavía no registrado.';