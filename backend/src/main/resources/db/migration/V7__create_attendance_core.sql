ALTER TABLE horarios
ADD CONSTRAINT uq_horarios_id_grupo
    UNIQUE (id, grupo_id);


CREATE TABLE sesiones_clase (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    grupo_id UUID NOT NULL,
    horario_id UUID NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio_programada TIME WITHOUT TIME ZONE NOT NULL,
    hora_fin_programada TIME WITHOUT TIME ZONE NOT NULL,
    tolerancia_minutos SMALLINT NOT NULL DEFAULT 10,
    estado VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADA',
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_sesiones_clase PRIMARY KEY (id),

    CONSTRAINT uq_sesiones_clase_id_grupo
        UNIQUE (id, grupo_id),

    CONSTRAINT uq_sesiones_horario_fecha
        UNIQUE (horario_id, fecha),

    CONSTRAINT fk_sesiones_horario_grupo
        FOREIGN KEY (horario_id, grupo_id)
        REFERENCES horarios (id, grupo_id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_sesiones_horas
        CHECK (hora_fin_programada > hora_inicio_programada),

    CONSTRAINT ck_sesiones_tolerancia
        CHECK (tolerancia_minutos BETWEEN 0 AND 120),

    CONSTRAINT ck_sesiones_estado
        CHECK (estado IN (
            'PROGRAMADA',
            'ABIERTA',
            'CERRADA',
            'CANCELADA'
        )),

    CONSTRAINT ck_sesiones_version
        CHECK (version >= 0)
);

CREATE INDEX ix_sesiones_grupo_fecha
    ON sesiones_clase (grupo_id, fecha);

CREATE INDEX ix_sesiones_fecha_estado
    ON sesiones_clase (fecha, estado);


CREATE TABLE asistencias (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    sesion_clase_id UUID NOT NULL,
    grupo_id UUID NOT NULL,
    matricula_id UUID NOT NULL,
    entrada_en TIMESTAMP WITH TIME ZONE,
    salida_en TIMESTAMP WITH TIME ZONE,
    metodo_entrada VARCHAR(20),
    metodo_salida VARCHAR(20),
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    observacion VARCHAR(500),
    registrado_por_usuario_id UUID,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_asistencias PRIMARY KEY (id),

    CONSTRAINT uq_asistencias_sesion_matricula
        UNIQUE (sesion_clase_id, matricula_id),

    CONSTRAINT fk_asistencias_sesion_grupo
        FOREIGN KEY (sesion_clase_id, grupo_id)
        REFERENCES sesiones_clase (id, grupo_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_asistencias_matricula_grupo
        FOREIGN KEY (matricula_id, grupo_id)
        REFERENCES matricula_grupos (matricula_id, grupo_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_asistencias_registrado_por
        FOREIGN KEY (registrado_por_usuario_id)
        REFERENCES usuarios (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_asistencias_horas
        CHECK (
            salida_en IS NULL
            OR entrada_en IS NULL
            OR salida_en >= entrada_en
        ),

    CONSTRAINT ck_asistencias_metodo_entrada
        CHECK (
            metodo_entrada IS NULL
            OR metodo_entrada IN ('QR', 'MANUAL', 'IMPORTACION')
        ),

    CONSTRAINT ck_asistencias_metodo_salida
        CHECK (
            metodo_salida IS NULL
            OR metodo_salida IN ('QR', 'MANUAL', 'IMPORTACION')
        ),

    CONSTRAINT ck_asistencias_estado
        CHECK (estado IN (
            'PENDIENTE',
            'PUNTUAL',
            'TARDANZA',
            'AUSENTE',
            'JUSTIFICADA'
        )),

    CONSTRAINT ck_asistencias_version
        CHECK (version >= 0)
);

CREATE INDEX ix_asistencias_matricula
    ON asistencias (matricula_id);

CREATE INDEX ix_asistencias_sesion
    ON asistencias (sesion_clase_id);

CREATE INDEX ix_asistencias_estado
    ON asistencias (estado);