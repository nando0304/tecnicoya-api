-- =====================================================================
--  TécnicoYa - Esquema de base de datos (MySQL 8.0.16 o superior)
-- ---------------------------------------------------------------------
--  Crea la base de datos tecnicoya_db y sus 11 tablas.
--  ATENCIÓN: el script es re-ejecutable y BORRA las tablas existentes
--  (y sus datos) antes de crearlas de nuevo. Úselo solo en desarrollo.
--
--  Convenciones de integridad referencial:
--    * ON DELETE CASCADE  -> datos "propiedad" del padre (perfil técnico,
--                            disponibilidad, evidencias, experiencia,
--                            calificación del servicio).
--    * ON DELETE RESTRICT -> datos históricos o financieros (servicios,
--                            pagos, suscripciones): no se pierden al borrar
--                            al padre; primero debe desactivarse o limpiarse.
--    * ON UPDATE CASCADE  -> en todas las FK (las PK son AUTO_INCREMENT y en
--                            la práctica no cambian).
-- =====================================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS tecnicoya_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE tecnicoya_db;

-- Orden inverso a las dependencias de las FK
DROP TABLE IF EXISTS pago_suscripcion;
DROP TABLE IF EXISTS suscripcion;
DROP TABLE IF EXISTS plan_suscripcion;
DROP TABLE IF EXISTS evidencia_pago;
DROP TABLE IF EXISTS calificacion;
DROP TABLE IF EXISTS experiencia_laboral;
DROP TABLE IF EXISTS evidencia;
DROP TABLE IF EXISTS disponibilidad;
DROP TABLE IF EXISTS servicio;
DROP TABLE IF EXISTS tecnico;
DROP TABLE IF EXISTS usuario;

-- ---------------------------------------------------------------------
-- USUARIO: toda persona que accede a la plataforma
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    idUsuario       BIGINT        NOT NULL AUTO_INCREMENT,
    nombres         VARCHAR(100)  NOT NULL,
    apellidos       VARCHAR(100)  NOT NULL,
    correo          VARCHAR(150)  NOT NULL,
    contrasena      VARCHAR(255)  NOT NULL COMMENT 'Hash BCrypt; nunca texto plano',
    telefono        VARCHAR(20)   NULL,
    tipo_usuario    ENUM('CLIENTE','TECNICO','ADMINISTRADOR') NOT NULL,
    estado          ENUM('ACTIVO','INACTIVO','BLOQUEADO')     NOT NULL DEFAULT 'ACTIVO',
    fecha_registro  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuario PRIMARY KEY (idUsuario),
    CONSTRAINT uq_usuario_correo UNIQUE (correo)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- TECNICO: perfil profesional (1 a 1 con un USUARIO de tipo TECNICO)
-- ---------------------------------------------------------------------
CREATE TABLE tecnico (
    idTecnico            BIGINT         NOT NULL AUTO_INCREMENT,
    usuario_id           BIGINT         NOT NULL,
    especialidad         VARCHAR(100)   NOT NULL,
    descripcion          VARCHAR(1000)  NULL,
    estado_verificacion  ENUM('PENDIENTE','VERIFICADO','RECHAZADO') NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT pk_tecnico PRIMARY KEY (idTecnico),
    CONSTRAINT uq_tecnico_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_tecnico_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario (idUsuario)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- SERVICIO: solicitud de un cliente, atendida por un técnico
-- ---------------------------------------------------------------------
CREATE TABLE servicio (
    idServicio            BIGINT         NOT NULL AUTO_INCREMENT,
    cliente_id            BIGINT         NOT NULL,
    tecnico_id            BIGINT         NULL COMMENT 'NULL mientras no se asigne un técnico',
    titulo                VARCHAR(150)   NOT NULL,
    descripcion_problema  VARCHAR(1000)  NOT NULL,
    estado_servicio       ENUM('PENDIENTE','ASIGNADO','EN_PROCESO','FINALIZADO','CANCELADO') NOT NULL DEFAULT 'PENDIENTE',
    prioridad             ENUM('BAJA','MEDIA','ALTA','URGENTE') NOT NULL DEFAULT 'MEDIA',
    fecha_solicitud       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_servicio        DATETIME       NULL COMMENT 'Fecha y hora programada para la atención',
    fecha_cierre          DATETIME       NULL COMMENT 'Solo en estados FINALIZADO o CANCELADO',
    CONSTRAINT pk_servicio PRIMARY KEY (idServicio),
    CONSTRAINT fk_servicio_cliente FOREIGN KEY (cliente_id)
        REFERENCES usuario (idUsuario)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_servicio_tecnico FOREIGN KEY (tecnico_id)
        REFERENCES tecnico (idTecnico)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_servicio_fecha_servicio CHECK (fecha_servicio IS NULL OR fecha_servicio >= fecha_solicitud),
    CONSTRAINT chk_servicio_fecha_cierre   CHECK (fecha_cierre   IS NULL OR fecha_cierre   >= fecha_solicitud)
) ENGINE = InnoDB;

CREATE INDEX idx_servicio_estado ON servicio (estado_servicio);

-- ---------------------------------------------------------------------
-- DISPONIBILIDAD: franjas horarias semanales del técnico
-- ---------------------------------------------------------------------
CREATE TABLE disponibilidad (
    idDisponibilidad  BIGINT  NOT NULL AUTO_INCREMENT,
    tecnico_id        BIGINT  NOT NULL,
    dia_semana        ENUM('LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO','DOMINGO') NOT NULL,
    hora_inicio       TIME    NOT NULL,
    hora_fin          TIME    NOT NULL,
    estado            ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT pk_disponibilidad PRIMARY KEY (idDisponibilidad),
    CONSTRAINT uq_disponibilidad_franja UNIQUE (tecnico_id, dia_semana, hora_inicio),
    CONSTRAINT fk_disponibilidad_tecnico FOREIGN KEY (tecnico_id)
        REFERENCES tecnico (idTecnico)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_disponibilidad_horas CHECK (hora_fin > hora_inicio)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- EVIDENCIA: documentos que respaldan la idoneidad del técnico
-- ---------------------------------------------------------------------
CREATE TABLE evidencia (
    idEvidencia        BIGINT        NOT NULL AUTO_INCREMENT,
    tecnico_id         BIGINT        NOT NULL,
    tipo_evidencia     ENUM('CERTIFICADO','TITULO_PROFESIONAL','ANTECEDENTES','DOCUMENTO_IDENTIDAD','FOTO_TRABAJO','OTRO') NOT NULL,
    url_archivo        VARCHAR(500)  NOT NULL,
    descripcion        VARCHAR(500)  NULL,
    estado_validacion  ENUM('PENDIENTE','APROBADO','RECHAZADO') NOT NULL DEFAULT 'PENDIENTE',
    fecha_carga        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_evidencia PRIMARY KEY (idEvidencia),
    CONSTRAINT fk_evidencia_tecnico FOREIGN KEY (tecnico_id)
        REFERENCES tecnico (idTecnico)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- EXPERIENCIA_LABORAL: historial laboral del técnico
-- ---------------------------------------------------------------------
CREATE TABLE experiencia_laboral (
    idExperiencia  BIGINT         NOT NULL AUTO_INCREMENT,
    tecnico_id     BIGINT         NOT NULL,
    empresa        VARCHAR(150)   NOT NULL,
    cargo          VARCHAR(100)   NOT NULL,
    descripcion    VARCHAR(1000)  NULL,
    fecha_inicio   DATE           NOT NULL,
    fecha_fin      DATE           NULL COMMENT 'NULL si es el trabajo actual',
    actualidad     BOOLEAN        NOT NULL DEFAULT FALSE COMMENT 'TRUE si aún trabaja allí',
    CONSTRAINT pk_experiencia_laboral PRIMARY KEY (idExperiencia),
    CONSTRAINT fk_experiencia_tecnico FOREIGN KEY (tecnico_id)
        REFERENCES tecnico (idTecnico)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_experiencia_fechas     CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio),
    CONSTRAINT chk_experiencia_actualidad CHECK ((actualidad = TRUE  AND fecha_fin IS NULL)
                                              OR (actualidad = FALSE AND fecha_fin IS NOT NULL))
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- CALIFICACION: valoración del cliente (máximo una por servicio)
-- ---------------------------------------------------------------------
CREATE TABLE calificacion (
    idCalificacion      BIGINT        NOT NULL AUTO_INCREMENT,
    servicio_id         BIGINT        NOT NULL,
    puntuacion          INT           NOT NULL,
    comentario          VARCHAR(500)  NULL,
    fecha_calificacion  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_calificacion PRIMARY KEY (idCalificacion),
    CONSTRAINT uq_calificacion_servicio UNIQUE (servicio_id),
    CONSTRAINT fk_calificacion_servicio FOREIGN KEY (servicio_id)
        REFERENCES servicio (idServicio)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_calificacion_puntuacion CHECK (puntuacion BETWEEN 1 AND 5)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- EVIDENCIA_PAGO: comprobante del pago de un servicio
-- ---------------------------------------------------------------------
CREATE TABLE evidencia_pago (
    idEvidenciaPago    BIGINT         NOT NULL AUTO_INCREMENT,
    servicio_id        BIGINT         NOT NULL,
    monto              DECIMAL(10,2)  NOT NULL,
    metodo_pago        ENUM('EFECTIVO','TARJETA_CREDITO','TARJETA_DEBITO','TRANSFERENCIA','BILLETERA_DIGITAL') NOT NULL,
    archivo_evidencia  VARCHAR(500)   NOT NULL COMMENT 'URL del comprobante',
    fecha_pago         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_validacion  ENUM('PENDIENTE','APROBADO','RECHAZADO') NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT pk_evidencia_pago PRIMARY KEY (idEvidenciaPago),
    CONSTRAINT fk_evidencia_pago_servicio FOREIGN KEY (servicio_id)
        REFERENCES servicio (idServicio)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_evidencia_pago_monto CHECK (monto > 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- PLAN_SUSCRIPCION: planes que ofrece la plataforma a los técnicos
-- ---------------------------------------------------------------------
CREATE TABLE plan_suscripcion (
    idPlan            BIGINT         NOT NULL AUTO_INCREMENT,
    nombre_plan       VARCHAR(100)   NOT NULL,
    precio_inicial    DECIMAL(10,2)  NOT NULL COMMENT 'Precio mientras no se supere limite_clientes',
    limite_clientes   INT            NOT NULL,
    precio_posterior  DECIMAL(10,2)  NOT NULL COMMENT 'Precio al superar limite_clientes',
    descripcion       VARCHAR(500)   NULL,
    estado            ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT pk_plan_suscripcion PRIMARY KEY (idPlan),
    CONSTRAINT uq_plan_suscripcion_nombre UNIQUE (nombre_plan),
    CONSTRAINT chk_plan_precio_inicial   CHECK (precio_inicial >= 0),
    CONSTRAINT chk_plan_precio_posterior CHECK (precio_posterior >= 0),
    CONSTRAINT chk_plan_limite_clientes  CHECK (limite_clientes > 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- SUSCRIPCION: contratación de un plan por parte de un técnico
-- ---------------------------------------------------------------------
CREATE TABLE suscripcion (
    idSuscripcion       BIGINT  NOT NULL AUTO_INCREMENT,
    tecnico_id          BIGINT  NOT NULL,
    plan_id             BIGINT  NOT NULL,
    fecha_inicio        DATE    NOT NULL,
    fecha_fin           DATE    NOT NULL,
    fecha_cancelacion   DATE    NULL COMMENT 'Solo en estado CANCELADA',
    estado_suscripcion  ENUM('PENDIENTE','ACTIVA','VENCIDA','CANCELADA') NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT pk_suscripcion PRIMARY KEY (idSuscripcion),
    CONSTRAINT fk_suscripcion_tecnico FOREIGN KEY (tecnico_id)
        REFERENCES tecnico (idTecnico)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_suscripcion_plan FOREIGN KEY (plan_id)
        REFERENCES plan_suscripcion (idPlan)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_suscripcion_fechas CHECK (fecha_fin > fecha_inicio)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- PAGO_SUSCRIPCION: pagos que realiza el técnico por su suscripción
-- ---------------------------------------------------------------------
CREATE TABLE pago_suscripcion (
    idPagoSuscripcion  BIGINT         NOT NULL AUTO_INCREMENT,
    suscripcion_id     BIGINT         NOT NULL,
    monto_pago         DECIMAL(10,2)  NOT NULL,
    metodo_pago        ENUM('EFECTIVO','TARJETA_CREDITO','TARJETA_DEBITO','TRANSFERENCIA','BILLETERA_DIGITAL') NOT NULL,
    estado_pago        ENUM('PENDIENTE','APROBADO','RECHAZADO','REEMBOLSADO') NOT NULL DEFAULT 'PENDIENTE',
    fecha_pago         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_pago_suscripcion PRIMARY KEY (idPagoSuscripcion),
    CONSTRAINT fk_pago_suscripcion_suscripcion FOREIGN KEY (suscripcion_id)
        REFERENCES suscripcion (idSuscripcion)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_pago_suscripcion_monto CHECK (monto_pago > 0)
) ENGINE = InnoDB;
