ALTER TABLE estudiante_apoderados
    ADD CONSTRAINT uq_estudiante_apoderado_unico
    UNIQUE (estudiante_id);