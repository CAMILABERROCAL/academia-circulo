CREATE TABLE roles (
    id SMALLINT GENERATED ALWAYS AS IDENTITY,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uq_roles_codigo UNIQUE (codigo),
    CONSTRAINT ck_roles_codigo_no_vacio
        CHECK (BTRIM(codigo) <> '')
);

CREATE TABLE usuarios (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    nombre_usuario VARCHAR(80) NOT NULL,
    correo VARCHAR(254),
    password_hash VARCHAR(255) NOT NULL,
    nombres VARCHAR(120) NOT NULL,
    apellidos VARCHAR(120) NOT NULL,
    tipo_documento VARCHAR(20) NOT NULL,
    numero_documento VARCHAR(30) NOT NULL,
    telefono VARCHAR(30),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    intentos_fallidos SMALLINT NOT NULL DEFAULT 0,
    bloqueado_hasta TIMESTAMP WITH TIME ZONE,
    debe_cambiar_password BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_acceso_en TIMESTAMP WITH TIME ZONE,
    password_cambiado_en TIMESTAMP WITH TIME ZONE,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_usuarios PRIMARY KEY (id),

    CONSTRAINT uq_usuarios_documento
        UNIQUE (tipo_documento, numero_documento),

    CONSTRAINT ck_usuarios_estado
        CHECK (estado IN (
            'ACTIVO',
            'BLOQUEADO',
            'INACTIVO',
            'PENDIENTE'
        )),

    CONSTRAINT ck_usuarios_intentos
        CHECK (intentos_fallidos >= 0),

    CONSTRAINT ck_usuarios_version
        CHECK (version >= 0),

    CONSTRAINT ck_usuarios_nombre_usuario_no_vacio
        CHECK (BTRIM(nombre_usuario) <> '')
);

CREATE UNIQUE INDEX ux_usuarios_nombre_usuario
    ON usuarios (LOWER(nombre_usuario));

CREATE UNIQUE INDEX ux_usuarios_correo
    ON usuarios (LOWER(correo))
    WHERE correo IS NOT NULL;

CREATE INDEX ix_usuarios_estado
    ON usuarios (estado);

CREATE TABLE usuario_roles (
    usuario_id UUID NOT NULL,
    rol_id SMALLINT NOT NULL,
    asignado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_usuario_roles
        PRIMARY KEY (usuario_id, rol_id),

    CONSTRAINT fk_usuario_roles_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_usuario_roles_rol
        FOREIGN KEY (rol_id)
        REFERENCES roles (id)
        ON DELETE RESTRICT
);

CREATE INDEX ix_usuario_roles_rol
    ON usuario_roles (rol_id);

INSERT INTO roles (codigo, nombre, descripcion)
VALUES
    ('ROLE_ADMIN', 'Administrador',
     'Administra el sistema académico'),

    ('ROLE_ESTUDIANTE', 'Estudiante',
     'Consulta sus notas, asistencias y pagos'),

    ('ROLE_APODERADO', 'Apoderado',
     'Consulta la información de los estudiantes vinculados');