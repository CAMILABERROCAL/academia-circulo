CREATE TABLE evaluaciones (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    grupo_id UUID NOT NULL,
    codigo VARCHAR(30) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    fecha_evaluacion DATE,
    ponderacion NUMERIC(5,2) NOT NULL,
    puntaje_maximo NUMERIC(6,2) NOT NULL DEFAULT 20,
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    creado_por_usuario_id UUID NOT NULL,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_evaluaciones PRIMARY KEY (id),

    CONSTRAINT uq_evaluaciones_id_grupo
        UNIQUE (id, grupo_id),

    CONSTRAINT fk_evaluaciones_grupo
        FOREIGN KEY (grupo_id)
        REFERENCES grupos_academicos (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_evaluaciones_creado_por
        FOREIGN KEY (creado_por_usuario_id)
        REFERENCES usuarios (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_evaluaciones_codigo_no_vacio
        CHECK (BTRIM(codigo) <> ''),

    CONSTRAINT ck_evaluaciones_nombre_no_vacio
        CHECK (BTRIM(nombre) <> ''),

    CONSTRAINT ck_evaluaciones_tipo
        CHECK (tipo IN (
            'TAREA',
            'PRACTICA',
            'EXAMEN',
            'PROYECTO',
            'PARTICIPACION',
            'OTRO'
        )),

    CONSTRAINT ck_evaluaciones_ponderacion
        CHECK (ponderacion > 0 AND ponderacion <= 100),

    CONSTRAINT ck_evaluaciones_puntaje_maximo
        CHECK (puntaje_maximo > 0),

    CONSTRAINT ck_evaluaciones_estado
        CHECK (estado IN (
            'BORRADOR',
            'PUBLICADA',
            'CERRADA',
            'ANULADA'
        )),

    CONSTRAINT ck_evaluaciones_version
        CHECK (version >= 0)
);

CREATE UNIQUE INDEX ux_evaluaciones_grupo_codigo
    ON evaluaciones (grupo_id, LOWER(codigo));

CREATE INDEX ix_evaluaciones_grupo
    ON evaluaciones (grupo_id);

CREATE INDEX ix_evaluaciones_estado
    ON evaluaciones (estado);


CREATE TABLE calificaciones (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    evaluacion_id UUID NOT NULL,
    grupo_id UUID NOT NULL,
    matricula_id UUID NOT NULL,
    puntaje NUMERIC(6,2),
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    observacion VARCHAR(500),
    registrado_por_usuario_id UUID NOT NULL,
    publicado_en TIMESTAMP WITH TIME ZONE,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_calificaciones PRIMARY KEY (id),

    CONSTRAINT uq_calificaciones_evaluacion_matricula
        UNIQUE (evaluacion_id, matricula_id),

    CONSTRAINT fk_calificaciones_evaluacion_grupo
        FOREIGN KEY (evaluacion_id, grupo_id)
        REFERENCES evaluaciones (id, grupo_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_calificaciones_matricula_grupo
        FOREIGN KEY (matricula_id, grupo_id)
        REFERENCES matricula_grupos (matricula_id, grupo_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_calificaciones_registrado_por
        FOREIGN KEY (registrado_por_usuario_id)
        REFERENCES usuarios (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_calificaciones_puntaje
        CHECK (puntaje IS NULL OR puntaje >= 0),

    CONSTRAINT ck_calificaciones_estado
        CHECK (estado IN (
            'PENDIENTE',
            'REGISTRADA',
            'PUBLICADA',
            'ANULADA'
        )),

    CONSTRAINT ck_calificaciones_version
        CHECK (version >= 0)
);

CREATE INDEX ix_calificaciones_evaluacion
    ON calificaciones (evaluacion_id);

CREATE INDEX ix_calificaciones_matricula
    ON calificaciones (matricula_id);

CREATE INDEX ix_calificaciones_estado
    ON calificaciones (estado);