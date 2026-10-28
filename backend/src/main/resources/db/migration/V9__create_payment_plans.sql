CREATE TABLE conceptos_pago (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    codigo VARCHAR(40) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500),
    tipo VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_conceptos_pago PRIMARY KEY (id),

    CONSTRAINT ck_conceptos_pago_codigo_no_vacio
        CHECK (BTRIM(codigo) <> ''),

    CONSTRAINT ck_conceptos_pago_nombre_no_vacio
        CHECK (BTRIM(nombre) <> ''),

    CONSTRAINT ck_conceptos_pago_tipo
        CHECK (tipo IN (
            'MATRICULA',
            'PENSION',
            'OTRO'
        )),

    CONSTRAINT ck_conceptos_pago_version
        CHECK (version >= 0)
);

CREATE UNIQUE INDEX ux_conceptos_pago_codigo
    ON conceptos_pago (LOWER(codigo));

CREATE INDEX ix_conceptos_pago_activo
    ON conceptos_pago (activo);


CREATE TABLE planes_pago (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    codigo VARCHAR(40) NOT NULL,
    matricula_id UUID NOT NULL,
    moneda CHAR(3) NOT NULL DEFAULT 'PEN',
    monto_total NUMERIC(12,2) NOT NULL,
    cantidad_cuotas SMALLINT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    creado_por_usuario_id UUID NOT NULL,
    activado_en TIMESTAMP WITH TIME ZONE,
    completado_en TIMESTAMP WITH TIME ZONE,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_planes_pago PRIMARY KEY (id),

    CONSTRAINT fk_planes_pago_matricula
        FOREIGN KEY (matricula_id)
        REFERENCES matriculas (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_planes_pago_creado_por
        FOREIGN KEY (creado_por_usuario_id)
        REFERENCES usuarios (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_planes_pago_codigo_no_vacio
        CHECK (BTRIM(codigo) <> ''),

    CONSTRAINT ck_planes_pago_moneda
        CHECK (moneda ~ '^[A-Z]{3}$'),

    CONSTRAINT ck_planes_pago_monto
        CHECK (monto_total > 0),

    CONSTRAINT ck_planes_pago_cantidad
        CHECK (cantidad_cuotas BETWEEN 1 AND 36),

    CONSTRAINT ck_planes_pago_estado
        CHECK (estado IN (
            'BORRADOR',
            'ACTIVO',
            'COMPLETADO',
            'CANCELADO'
        )),

    CONSTRAINT ck_planes_pago_version
        CHECK (version >= 0)
);

CREATE UNIQUE INDEX ux_planes_pago_codigo
    ON planes_pago (LOWER(codigo));

CREATE UNIQUE INDEX ux_planes_pago_matricula_activo
    ON planes_pago (matricula_id)
    WHERE estado IN ('BORRADOR', 'ACTIVO');

CREATE INDEX ix_planes_pago_estado
    ON planes_pago (estado);


CREATE TABLE cuotas_pago (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    plan_pago_id UUID NOT NULL,
    concepto_pago_id UUID NOT NULL,
    numero_cuota SMALLINT NOT NULL,
    descripcion VARCHAR(200) NOT NULL,
    periodo_inicio DATE,
    periodo_fin DATE,
    fecha_vencimiento DATE NOT NULL,
    monto NUMERIC(12,2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_cuotas_pago PRIMARY KEY (id),

    CONSTRAINT uq_cuotas_pago_plan_numero
        UNIQUE (plan_pago_id, numero_cuota),

    CONSTRAINT fk_cuotas_pago_plan
        FOREIGN KEY (plan_pago_id)
        REFERENCES planes_pago (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_cuotas_pago_concepto
        FOREIGN KEY (concepto_pago_id)
        REFERENCES conceptos_pago (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_cuotas_pago_numero
        CHECK (numero_cuota > 0),

    CONSTRAINT ck_cuotas_pago_descripcion_no_vacia
        CHECK (BTRIM(descripcion) <> ''),

    CONSTRAINT ck_cuotas_pago_periodo
        CHECK (
            (
                periodo_inicio IS NULL
                AND periodo_fin IS NULL
            )
            OR
            (
                periodo_inicio IS NOT NULL
                AND periodo_fin IS NOT NULL
                AND periodo_fin >= periodo_inicio
            )
        ),

    CONSTRAINT ck_cuotas_pago_monto
        CHECK (monto > 0),

    CONSTRAINT ck_cuotas_pago_estado
        CHECK (estado IN (
            'PENDIENTE',
            'EN_REVISION',
            'PAGADO',
            'VENCIDO',
            'ANULADO'
        )),

    CONSTRAINT ck_cuotas_pago_version
        CHECK (version >= 0)
);

CREATE INDEX ix_cuotas_pago_plan
    ON cuotas_pago (plan_pago_id);

CREATE INDEX ix_cuotas_pago_estado_vencimiento
    ON cuotas_pago (estado, fecha_vencimiento);

CREATE INDEX ix_cuotas_pago_concepto
    ON cuotas_pago (concepto_pago_id);


INSERT INTO conceptos_pago (
    codigo,
    nombre,
    descripcion,
    tipo
)
VALUES
    (
        'MATRICULA',
        'Matrícula',
        'Pago correspondiente a la matrícula académica',
        'MATRICULA'
    ),
    (
        'PENSION_MENSUAL',
        'Pensión mensual',
        'Pago mensual correspondiente al ciclo académico',
        'PENSION'
    );