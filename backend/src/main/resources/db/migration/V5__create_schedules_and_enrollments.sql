CREATE TABLE grupos_academicos (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    ciclo_id UUID NOT NULL,
    curso_id UUID NOT NULL,
    codigo VARCHAR(40) NOT NULL,
    capacidad SMALLINT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PLANIFICADO',
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_grupos_academicos PRIMARY KEY (id),

    CONSTRAINT fk_grupos_ciclo
        FOREIGN KEY (ciclo_id)
        REFERENCES ciclos (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_grupos_curso
        FOREIGN KEY (curso_id)
        REFERENCES cursos (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_grupos_codigo_no_vacio
        CHECK (BTRIM(codigo) <> ''),

    CONSTRAINT ck_grupos_capacidad
        CHECK (capacidad > 0),

    CONSTRAINT ck_grupos_estado
        CHECK (estado IN (
            'PLANIFICADO',
            'ACTIVO',
            'CERRADO',
            'CANCELADO'
        )),

    CONSTRAINT ck_grupos_version
        CHECK (version >= 0)
);

CREATE UNIQUE INDEX ux_grupos_ciclo_codigo
    ON grupos_academicos (ciclo_id, LOWER(codigo));

CREATE INDEX ix_grupos_ciclo
    ON grupos_academicos (ciclo_id);

CREATE INDEX ix_grupos_curso
    ON grupos_academicos (curso_id);

CREATE INDEX ix_grupos_estado
    ON grupos_academicos (estado);


CREATE TABLE horarios (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    grupo_id UUID NOT NULL,
    aula_id UUID NOT NULL,
    dia_semana SMALLINT NOT NULL,
    hora_inicio TIME WITHOUT TIME ZONE NOT NULL,
    hora_fin TIME WITHOUT TIME ZONE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_horarios PRIMARY KEY (id),

    CONSTRAINT fk_horarios_grupo
        FOREIGN KEY (grupo_id)
        REFERENCES grupos_academicos (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_horarios_aula
        FOREIGN KEY (aula_id)
        REFERENCES aulas (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_horarios_dia
        CHECK (dia_semana BETWEEN 1 AND 7),

    CONSTRAINT ck_horarios_horas
        CHECK (hora_fin > hora_inicio),

    CONSTRAINT ck_horarios_estado
        CHECK (estado IN (
            'ACTIVO',
            'INACTIVO',
            'CANCELADO'
        )),

    CONSTRAINT ck_horarios_version
        CHECK (version >= 0),

    CONSTRAINT uq_horarios_grupo
        UNIQUE (grupo_id, dia_semana, hora_inicio)
);

CREATE INDEX ix_horarios_aula
    ON horarios (aula_id);

CREATE INDEX ix_horarios_dia
    ON horarios (dia_semana);


CREATE TABLE matriculas (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    codigo VARCHAR(40) NOT NULL,
    estudiante_id UUID NOT NULL,
    ciclo_id UUID NOT NULL,
    fecha_matricula DATE NOT NULL DEFAULT CURRENT_DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_matriculas PRIMARY KEY (id),

    CONSTRAINT fk_matriculas_estudiante
        FOREIGN KEY (estudiante_id)
        REFERENCES estudiantes (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_matriculas_ciclo
        FOREIGN KEY (ciclo_id)
        REFERENCES ciclos (id)
        ON DELETE RESTRICT,

    CONSTRAINT uq_matriculas_estudiante_ciclo
        UNIQUE (estudiante_id, ciclo_id),

    CONSTRAINT ck_matriculas_codigo_no_vacio
        CHECK (BTRIM(codigo) <> ''),

    CONSTRAINT ck_matriculas_estado
        CHECK (estado IN (
            'PENDIENTE',
            'ACTIVA',
            'RETIRADA',
            'FINALIZADA',
            'CANCELADA'
        )),

    CONSTRAINT ck_matriculas_version
        CHECK (version >= 0)
);

CREATE UNIQUE INDEX ux_matriculas_codigo
    ON matriculas (LOWER(codigo));

CREATE INDEX ix_matriculas_ciclo
    ON matriculas (ciclo_id);

CREATE INDEX ix_matriculas_estado
    ON matriculas (estado);


CREATE TABLE matricula_grupos (
    matricula_id UUID NOT NULL,
    grupo_id UUID NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'INSCRITO',
    inscrito_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    retirado_en TIMESTAMP WITH TIME ZONE,

    CONSTRAINT pk_matricula_grupos
        PRIMARY KEY (matricula_id, grupo_id),

    CONSTRAINT fk_matricula_grupos_matricula
        FOREIGN KEY (matricula_id)
        REFERENCES matriculas (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_matricula_grupos_grupo
        FOREIGN KEY (grupo_id)
        REFERENCES grupos_academicos (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_matricula_grupos_estado
        CHECK (estado IN (
            'INSCRITO',
            'RETIRADO',
            'FINALIZADO'
        ))
);

CREATE INDEX ix_matricula_grupos_grupo
    ON matricula_grupos (grupo_id);