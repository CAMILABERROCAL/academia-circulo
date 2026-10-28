CREATE TABLE permisos (
    id SMALLINT GENERATED ALWAYS AS IDENTITY,
    codigo VARCHAR(80) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(255),

    CONSTRAINT pk_permisos PRIMARY KEY (id),
    CONSTRAINT uq_permisos_codigo UNIQUE (codigo),
    CONSTRAINT ck_permisos_codigo_no_vacio
        CHECK (BTRIM(codigo) <> ''),
    CONSTRAINT ck_permisos_nombre_no_vacio
        CHECK (BTRIM(nombre) <> '')
);

CREATE TABLE rol_permisos (
    rol_id SMALLINT NOT NULL,
    permiso_id SMALLINT NOT NULL,

    CONSTRAINT pk_rol_permisos
        PRIMARY KEY (rol_id, permiso_id),

    CONSTRAINT fk_rol_permisos_rol
        FOREIGN KEY (rol_id)
        REFERENCES roles (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_rol_permisos_permiso
        FOREIGN KEY (permiso_id)
        REFERENCES permisos (id)
        ON DELETE RESTRICT
);

CREATE TABLE usuario_permisos (
    usuario_id UUID NOT NULL,
    permiso_id SMALLINT NOT NULL,
    asignado_por UUID NOT NULL,
    asignado_en TIMESTAMP WITH TIME ZONE NOT NULL
        DEFAULT CURRENT_TIMESTAMP,
    motivo VARCHAR(500) NOT NULL,

    CONSTRAINT pk_usuario_permisos
        PRIMARY KEY (usuario_id, permiso_id),

    CONSTRAINT fk_usuario_permisos_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_usuario_permisos_permiso
        FOREIGN KEY (permiso_id)
        REFERENCES permisos (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_usuario_permisos_asignado_por
        FOREIGN KEY (asignado_por)
        REFERENCES usuarios (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_usuario_permisos_motivo
        CHECK (BTRIM(motivo) <> '')
);

CREATE INDEX ix_rol_permisos_permiso
    ON rol_permisos (permiso_id);

CREATE INDEX ix_usuario_permisos_permiso
    ON usuario_permisos (permiso_id);

CREATE INDEX ix_usuario_permisos_asignado_por
    ON usuario_permisos (asignado_por);

INSERT INTO permisos (codigo, nombre, descripcion)
VALUES (
    'NOTAS_EDITAR',
    'Registrar y modificar notas',
    'Permite registrar y corregir calificaciones académicas'
);

INSERT INTO rol_permisos (rol_id, permiso_id)
VALUES (
    (SELECT id FROM roles WHERE codigo = 'ROLE_SECRETARIA'),
    (SELECT id FROM permisos WHERE codigo = 'NOTAS_EDITAR')
);