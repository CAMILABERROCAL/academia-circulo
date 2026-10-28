CREATE TABLE pagos (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL,
    cuota_pago_id UUID NOT NULL,
    monto NUMERIC(12,2) NOT NULL,
    moneda CHAR(3) NOT NULL DEFAULT 'PEN',
    metodo VARCHAR(20) NOT NULL,
    origen VARCHAR(30) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    numero_operacion VARCHAR(100),
    pagado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    registrado_por_usuario_id UUID NOT NULL,
    revisado_por_usuario_id UUID,
    revisado_en TIMESTAMP WITH TIME ZONE,
    observacion VARCHAR(500),
    motivo_revision VARCHAR(500),
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_pagos PRIMARY KEY (id),

    CONSTRAINT fk_pagos_cuota
        FOREIGN KEY (cuota_pago_id)
        REFERENCES cuotas_pago (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_pagos_registrado_por
        FOREIGN KEY (registrado_por_usuario_id)
        REFERENCES usuarios (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_pagos_revisado_por
        FOREIGN KEY (revisado_por_usuario_id)
        REFERENCES usuarios (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_pagos_codigo_no_vacio
        CHECK (BTRIM(codigo) <> ''),

    CONSTRAINT ck_pagos_monto
        CHECK (monto > 0),

    CONSTRAINT ck_pagos_moneda
        CHECK (moneda ~ '^[A-Z]{3}$'),

    CONSTRAINT ck_pagos_metodo
        CHECK (metodo IN (
            'EFECTIVO',
            'YAPE',
            'TRANSFERENCIA',
            'POS'
        )),

    CONSTRAINT ck_pagos_origen
        CHECK (origen IN (
            'PORTAL_ESTUDIANTE',
            'PORTAL_APODERADO',
            'ADMINISTRADOR',
            'IMPORTACION'
        )),

    CONSTRAINT ck_pagos_estado
        CHECK (estado IN (
            'EN_REVISION',
            'APROBADO',
            'RECHAZADO',
            'ANULADO'
        )),

    CONSTRAINT ck_pagos_numero_operacion
        CHECK (
            metodo = 'EFECTIVO'
            OR (
                numero_operacion IS NOT NULL
                AND BTRIM(numero_operacion) <> ''
            )
        ),

    CONSTRAINT ck_pagos_revision
        CHECK (
            estado = 'EN_REVISION'
            OR (
                revisado_por_usuario_id IS NOT NULL
                AND revisado_en IS NOT NULL
            )
        ),

    CONSTRAINT ck_pagos_motivo_rechazo
        CHECK (
            estado <> 'RECHAZADO'
            OR (
                motivo_revision IS NOT NULL
                AND BTRIM(motivo_revision) <> ''
            )
        ),

    CONSTRAINT ck_pagos_version
        CHECK (version >= 0)
);

CREATE UNIQUE INDEX ux_pagos_codigo
    ON pagos (LOWER(codigo));

CREATE UNIQUE INDEX ux_pagos_cuota_activa
    ON pagos (cuota_pago_id)
    WHERE estado IN ('EN_REVISION', 'APROBADO');

CREATE INDEX ix_pagos_estado
    ON pagos (estado);

CREATE INDEX ix_pagos_metodo_estado
    ON pagos (metodo, estado);

CREATE INDEX ix_pagos_fecha
    ON pagos (pagado_en);


CREATE TABLE comprobantes_pago (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    pago_id UUID NOT NULL,
    archivo_clave VARCHAR(500) NOT NULL,
    archivo_nombre_original VARCHAR(255) NOT NULL,
    archivo_mime VARCHAR(100) NOT NULL,
    archivo_tamano_bytes BIGINT NOT NULL,
    archivo_sha256 CHAR(64) NOT NULL,
    subido_por_usuario_id UUID NOT NULL,
    subido_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_comprobantes_pago PRIMARY KEY (id),

    CONSTRAINT uq_comprobantes_pago
        UNIQUE (pago_id),

    CONSTRAINT fk_comprobantes_pago
        FOREIGN KEY (pago_id)
        REFERENCES pagos (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_comprobantes_subido_por
        FOREIGN KEY (subido_por_usuario_id)
        REFERENCES usuarios (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_comprobantes_clave_no_vacia
        CHECK (BTRIM(archivo_clave) <> ''),

    CONSTRAINT ck_comprobantes_tamano
        CHECK (archivo_tamano_bytes BETWEEN 1 AND 5242880),

    CONSTRAINT ck_comprobantes_hash
        CHECK (LENGTH(archivo_sha256) = 64),

    CONSTRAINT ck_comprobantes_mime
        CHECK (archivo_mime IN (
            'application/pdf',
            'image/jpeg',
            'image/png',
            'image/webp'
        ))
);

CREATE INDEX ix_comprobantes_sha256
    ON comprobantes_pago (archivo_sha256);


CREATE TABLE recibos_pago (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    pago_id UUID NOT NULL,
    numero_recibo VARCHAR(50) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'EMITIDO',
    archivo_clave VARCHAR(500) NOT NULL,
    archivo_tamano_bytes BIGINT NOT NULL,
    archivo_sha256 CHAR(64) NOT NULL,
    emitido_por_usuario_id UUID NOT NULL,
    emitido_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    anulado_en TIMESTAMP WITH TIME ZONE,
    motivo_anulacion VARCHAR(500),

    CONSTRAINT pk_recibos_pago PRIMARY KEY (id),

    CONSTRAINT uq_recibos_pago
        UNIQUE (pago_id),

    CONSTRAINT fk_recibos_pago
        FOREIGN KEY (pago_id)
        REFERENCES pagos (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_recibos_emitido_por
        FOREIGN KEY (emitido_por_usuario_id)
        REFERENCES usuarios (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_recibos_numero_no_vacio
        CHECK (BTRIM(numero_recibo) <> ''),

    CONSTRAINT ck_recibos_estado
        CHECK (estado IN (
            'EMITIDO',
            'ANULADO'
        )),

    CONSTRAINT ck_recibos_archivo_no_vacio
        CHECK (BTRIM(archivo_clave) <> ''),

    CONSTRAINT ck_recibos_tamano
        CHECK (archivo_tamano_bytes BETWEEN 1 AND 10485760),

    CONSTRAINT ck_recibos_hash
        CHECK (LENGTH(archivo_sha256) = 64),

    CONSTRAINT ck_recibos_anulacion
        CHECK (
            estado <> 'ANULADO'
            OR (
                anulado_en IS NOT NULL
                AND motivo_anulacion IS NOT NULL
                AND BTRIM(motivo_anulacion) <> ''
            )
        )
);

CREATE UNIQUE INDEX ux_recibos_numero
    ON recibos_pago (LOWER(numero_recibo));


CREATE TABLE pago_historial_estados (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    pago_id UUID NOT NULL,
    estado_anterior VARCHAR(20),
    estado_nuevo VARCHAR(20) NOT NULL,
    cambiado_por_usuario_id UUID NOT NULL,
    motivo VARCHAR(500),
    cambiado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_pago_historial_estados PRIMARY KEY (id),

    CONSTRAINT fk_pago_historial_pago
        FOREIGN KEY (pago_id)
        REFERENCES pagos (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_pago_historial_usuario
        FOREIGN KEY (cambiado_por_usuario_id)
        REFERENCES usuarios (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_pago_historial_anterior
        CHECK (
            estado_anterior IS NULL
            OR estado_anterior IN (
                'EN_REVISION',
                'APROBADO',
                'RECHAZADO',
                'ANULADO'
            )
        ),

    CONSTRAINT ck_pago_historial_nuevo
        CHECK (estado_nuevo IN (
            'EN_REVISION',
            'APROBADO',
            'RECHAZADO',
            'ANULADO'
        ))
);

CREATE INDEX ix_pago_historial_pago_fecha
    ON pago_historial_estados (pago_id, cambiado_en);