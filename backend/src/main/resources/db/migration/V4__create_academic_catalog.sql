CREATE TABLE ciclos (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    codigo VARCHAR(30) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    duracion_meses SMALLINT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PLANIFICADO',
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_ciclos PRIMARY KEY (id),

    CONSTRAINT ck_ciclos_codigo_no_vacio
        CHECK (BTRIM(codigo) <> ''),

    CONSTRAINT ck_ciclos_nombre_no_vacio
        CHECK (BTRIM(nombre) <> ''),

    CONSTRAINT ck_ciclos_fechas
        CHECK (fecha_fin >= fecha_inicio),

    CONSTRAINT ck_ciclos_duracion
        CHECK (duracion_meses BETWEEN 1 AND 24),

    CONSTRAINT ck_ciclos_estado
        CHECK (estado IN (
            'PLANIFICADO',
            'ACTIVO',
            'FINALIZADO',
            'CANCELADO'
        )),

    CONSTRAINT ck_ciclos_version
        CHECK (version >= 0)
);

CREATE UNIQUE INDEX ux_ciclos_codigo
    ON ciclos (LOWER(codigo));

CREATE INDEX ix_ciclos_estado
    ON ciclos (estado);

CREATE INDEX ix_ciclos_fechas
    ON ciclos (fecha_inicio, fecha_fin);


CREATE TABLE cursos (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    codigo VARCHAR(30) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_cursos PRIMARY KEY (id),

    CONSTRAINT ck_cursos_codigo_no_vacio
        CHECK (BTRIM(codigo) <> ''),

    CONSTRAINT ck_cursos_nombre_no_vacio
        CHECK (BTRIM(nombre) <> ''),

    CONSTRAINT ck_cursos_estado
        CHECK (estado IN (
            'ACTIVO',
            'INACTIVO'
        )),

    CONSTRAINT ck_cursos_version
        CHECK (version >= 0)
);

CREATE UNIQUE INDEX ux_cursos_codigo
    ON cursos (LOWER(codigo));

CREATE INDEX ix_cursos_estado
    ON cursos (estado);


CREATE TABLE aulas (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    codigo VARCHAR(30) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    ubicacion VARCHAR(200),
    capacidad SMALLINT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_aulas PRIMARY KEY (id),

    CONSTRAINT ck_aulas_codigo_no_vacio
        CHECK (BTRIM(codigo) <> ''),

    CONSTRAINT ck_aulas_nombre_no_vacio
        CHECK (BTRIM(nombre) <> ''),

    CONSTRAINT ck_aulas_capacidad
        CHECK (capacidad > 0),

    CONSTRAINT ck_aulas_estado
        CHECK (estado IN (
            'ACTIVA',
            'INACTIVA',
            'MANTENIMIENTO'
        )),

    CONSTRAINT ck_aulas_version
        CHECK (version >= 0)
);

CREATE UNIQUE INDEX ux_aulas_codigo
    ON aulas (LOWER(codigo));

CREATE INDEX ix_aulas_estado
    ON aulas (estado);