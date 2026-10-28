CREATE TABLE turnos_ciclo (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    ciclo_id UUID NOT NULL,
    codigo VARCHAR(30) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_turnos_ciclo
        PRIMARY KEY (id),

    CONSTRAINT fk_turnos_ciclo_ciclo
        FOREIGN KEY (ciclo_id)
        REFERENCES ciclos (id)
        ON DELETE RESTRICT,

    CONSTRAINT uq_turnos_ciclo_id_ciclo
        UNIQUE (id, ciclo_id),

    CONSTRAINT ck_turnos_ciclo_codigo
        CHECK (
            codigo = UPPER(BTRIM(codigo))
            AND codigo ~ '^[A-Z0-9_-]+$'
        ),

    CONSTRAINT ck_turnos_ciclo_nombre
        CHECK (BTRIM(nombre) <> ''),

    CONSTRAINT ck_turnos_ciclo_estado
        CHECK (estado IN ('ACTIVO', 'INACTIVO')),

    CONSTRAINT ck_turnos_ciclo_version
        CHECK (version >= 0),

    CONSTRAINT uq_turnos_ciclo_codigo
        UNIQUE (ciclo_id, codigo)
);

CREATE TABLE horarios_turno (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    turno_id UUID NOT NULL,
    dia_semana SMALLINT NOT NULL,
    hora_entrada TIME WITHOUT TIME ZONE NOT NULL,
    hora_salida TIME WITHOUT TIME ZONE NOT NULL,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_horarios_turno
        PRIMARY KEY (id),

    CONSTRAINT fk_horarios_turno_turno
        FOREIGN KEY (turno_id)
        REFERENCES turnos_ciclo (id)
        ON DELETE RESTRICT,

    CONSTRAINT uq_horarios_turno_dia
        UNIQUE (turno_id, dia_semana),

    CONSTRAINT ck_horarios_turno_dia
        CHECK (dia_semana BETWEEN 1 AND 7),

    CONSTRAINT ck_horarios_turno_horas
        CHECK (hora_salida > hora_entrada),

    CONSTRAINT ck_horarios_turno_version
        CHECK (version >= 0)
);

COMMENT ON TABLE turnos_ciclo IS
    'Turnos disponibles por ciclo para la asistencia de entrada y salida.';

COMMENT ON COLUMN horarios_turno.dia_semana IS
    'Numeración ISO: 1 lunes, 2 martes, 3 miércoles, 4 jueves, 5 viernes, 6 sábado, 7 domingo.';

COMMENT ON COLUMN horarios_turno.hora_entrada IS
    'Hora local de entrada programada en America/Lima.';

COMMENT ON COLUMN horarios_turno.hora_salida IS
    'Hora local de salida programada en America/Lima.';