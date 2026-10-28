CREATE TABLE estudiantes (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    codigo_estudiante VARCHAR(30) NOT NULL,
    fecha_nacimiento DATE,
    fecha_ingreso DATE NOT NULL DEFAULT CURRENT_DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_estudiantes PRIMARY KEY (id),

    CONSTRAINT uq_estudiantes_usuario
        UNIQUE (usuario_id),

    CONSTRAINT fk_estudiantes_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_estudiantes_codigo_no_vacio
        CHECK (BTRIM(codigo_estudiante) <> ''),

    CONSTRAINT ck_estudiantes_estado
        CHECK (estado IN (
            'ACTIVO',
            'INACTIVO',
            'RETIRADO',
            'EGRESADO'
        )),

    CONSTRAINT ck_estudiantes_version
        CHECK (version >= 0)
);

CREATE UNIQUE INDEX ux_estudiantes_codigo
    ON estudiantes (LOWER(codigo_estudiante));

CREATE INDEX ix_estudiantes_estado
    ON estudiantes (estado);

CREATE TABLE apoderados (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    direccion VARCHAR(255),
    ocupacion VARCHAR(120),
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_apoderados PRIMARY KEY (id),

    CONSTRAINT uq_apoderados_usuario
        UNIQUE (usuario_id),

    CONSTRAINT fk_apoderados_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_apoderados_version
        CHECK (version >= 0)
);

CREATE TABLE estudiante_apoderados (
    estudiante_id UUID NOT NULL,
    apoderado_id UUID NOT NULL,
    parentesco VARCHAR(20) NOT NULL,
    es_principal BOOLEAN NOT NULL DEFAULT FALSE,
    recibe_notificaciones BOOLEAN NOT NULL DEFAULT TRUE,
    vinculado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_estudiante_apoderados
        PRIMARY KEY (estudiante_id, apoderado_id),

    CONSTRAINT fk_estudiante_apoderados_estudiante
        FOREIGN KEY (estudiante_id)
        REFERENCES estudiantes (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_estudiante_apoderados_apoderado
        FOREIGN KEY (apoderado_id)
        REFERENCES apoderados (id)
        ON DELETE CASCADE,

    CONSTRAINT ck_estudiante_apoderados_parentesco
        CHECK (parentesco IN (
            'PADRE',
            'MADRE',
            'TUTOR',
            'OTRO'
        ))
);

CREATE INDEX ix_estudiante_apoderados_apoderado
    ON estudiante_apoderados (apoderado_id);

CREATE UNIQUE INDEX ux_estudiante_apoderado_principal
    ON estudiante_apoderados (estudiante_id)
    WHERE es_principal = TRUE;