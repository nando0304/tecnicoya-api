-- =====================================================================
--  TécnicoYa - Datos de ejemplo (ficticios)
-- ---------------------------------------------------------------------
--  Ejecutar DESPUÉS de schema.sql, sobre las tablas vacías.
--  Todos los usuarios de ejemplo tienen la contraseña:  TecnicoYa2026!
--  (guardada como hash BCrypt, igual que la API).
--  Los dominios .example están reservados: ningún correo o URL es real.
-- =====================================================================

SET NAMES utf8mb4;
USE tecnicoya_db;

START TRANSACTION;

-- ---------------------------------------------------------------------
-- USUARIO: 1 administrador, 3 clientes, 4 técnicos
-- ---------------------------------------------------------------------
INSERT INTO usuario (idUsuario, nombres, apellidos, correo, contrasena, telefono, tipo_usuario, estado, fecha_registro) VALUES
(1, 'Carlos Alberto', 'Mendoza Ríos',     'carlos.mendoza@tecnicoya.example',  '$2a$10$4mymzKNPjzDSnS1PmHWTYOQsanPwV8xhXQKym0xqSgZ0pK8jfS3S.', '987100200', 'ADMINISTRADOR', 'ACTIVO', '2026-01-05 09:00:00'),
(2, 'María Fernanda', 'Quispe Huamán',    'maria.quispe@correo.example',       '$2a$10$4mymzKNPjzDSnS1PmHWTYOQsanPwV8xhXQKym0xqSgZ0pK8jfS3S.', '987200301', 'CLIENTE',       'ACTIVO', '2026-02-11 18:22:10'),
(3, 'José Luis',      'Torres Vega',      'jose.torres@correo.example',        '$2a$10$4mymzKNPjzDSnS1PmHWTYOQsanPwV8xhXQKym0xqSgZ0pK8jfS3S.', '987300402', 'CLIENTE',       'ACTIVO', '2026-03-02 10:15:45'),
(4, 'Ana Lucía',      'Paredes Salas',    'ana.paredes@correo.example',        '$2a$10$4mymzKNPjzDSnS1PmHWTYOQsanPwV8xhXQKym0xqSgZ0pK8jfS3S.', '987400503', 'CLIENTE',       'ACTIVO', '2026-04-19 20:40:00'),
(5, 'Pedro Alonso',   'Ramírez Castillo', 'pedro.ramirez@tecnicoya.example',   '$2a$10$4mymzKNPjzDSnS1PmHWTYOQsanPwV8xhXQKym0xqSgZ0pK8jfS3S.', '987500604', 'TECNICO',       'ACTIVO', '2026-01-20 08:30:00'),
(6, 'Rosa Elena',     'Gutiérrez Flores', 'rosa.gutierrez@tecnicoya.example',  '$2a$10$4mymzKNPjzDSnS1PmHWTYOQsanPwV8xhXQKym0xqSgZ0pK8jfS3S.', '987600705', 'TECNICO',       'ACTIVO', '2026-02-01 11:05:00'),
(7, 'Luis Miguel',    'Chávez Rojas',     'luis.chavez@tecnicoya.example',     '$2a$10$4mymzKNPjzDSnS1PmHWTYOQsanPwV8xhXQKym0xqSgZ0pK8jfS3S.', '987700806', 'TECNICO',       'ACTIVO', '2026-03-14 16:50:00'),
(8, 'Jorge Enrique',  'Vargas Soto',      'jorge.vargas@tecnicoya.example',    '$2a$10$4mymzKNPjzDSnS1PmHWTYOQsanPwV8xhXQKym0xqSgZ0pK8jfS3S.', '987800907', 'TECNICO',       'ACTIVO', '2026-09-15 12:00:00');

-- ---------------------------------------------------------------------
-- TECNICO: perfiles de los usuarios 5 a 8
-- ---------------------------------------------------------------------
INSERT INTO tecnico (idTecnico, usuario_id, especialidad, descripcion, estado_verificacion) VALUES
(1, 5, 'Electricidad',                   'Instalaciones eléctricas domiciliarias, tableros y cortocircuitos.',        'VERIFICADO'),
(2, 6, 'Plomería',                       'Reparación de fugas, instalación de termas y desatoro de tuberías.',         'VERIFICADO'),
(3, 7, 'Refrigeración y línea blanca',   'Mantenimiento de refrigeradoras, lavadoras y aires acondicionados.',          'VERIFICADO'),
(4, 8, 'Soporte técnico de computadoras', 'Formateo, cambio de piezas y configuración de redes domésticas.',            'PENDIENTE');

-- ---------------------------------------------------------------------
-- SERVICIO: distintos estados del ciclo de vida
-- ---------------------------------------------------------------------
INSERT INTO servicio (idServicio, cliente_id, tecnico_id, titulo, descripcion_problema, estado_servicio, prioridad, fecha_solicitud, fecha_servicio, fecha_cierre) VALUES
(1, 2, 1,    'Cortocircuito en la cocina',       'Al encender el microondas se baja la llave general y huele a quemado.',  'FINALIZADO', 'ALTA',    '2026-08-03 09:15:00', '2026-08-04 10:00:00', '2026-08-04 12:30:00'),
(2, 3, 2,    'Fuga de agua en el baño',          'Sale agua por debajo del inodoro y se está mojando el piso.',            'FINALIZADO', 'URGENTE', '2026-08-10 07:40:00', '2026-08-10 11:00:00', '2026-08-10 13:15:00'),
(3, 4, 3,    'Refrigeradora no enfría',          'La refrigeradora enciende pero no enfría desde hace dos días.',          'FINALIZADO', 'MEDIA',   '2026-08-18 14:20:00', '2026-08-20 09:00:00', '2026-08-20 11:45:00'),
(4, 2, 2,    'Instalación de terma eléctrica',   'Necesito instalar una terma de 50 litros en el baño principal.',         'EN_PROCESO', 'MEDIA',   '2026-09-20 16:00:00', '2026-09-25 09:30:00', NULL),
(5, 3, NULL, 'Laptop no enciende',               'La laptop no muestra imagen ni enciende las luces del teclado.',         'PENDIENTE',  'BAJA',    '2026-09-24 19:10:00', NULL,                  NULL),
(6, 4, 1,    'Instalación de luminarias LED',    'Cambiar seis luminarias de la sala y el comedor por paneles LED.',       'ASIGNADO',   'BAJA',    '2026-09-25 10:05:00', '2026-10-02 15:00:00', NULL);

-- ---------------------------------------------------------------------
-- DISPONIBILIDAD: franjas semanales
-- ---------------------------------------------------------------------
INSERT INTO disponibilidad (idDisponibilidad, tecnico_id, dia_semana, hora_inicio, hora_fin, estado) VALUES
(1, 1, 'LUNES',     '08:00:00', '13:00:00', 'ACTIVO'),
(2, 1, 'MIERCOLES', '14:00:00', '19:00:00', 'ACTIVO'),
(3, 2, 'MARTES',    '09:00:00', '18:00:00', 'ACTIVO'),
(4, 2, 'SABADO',    '08:00:00', '12:00:00', 'ACTIVO'),
(5, 3, 'JUEVES',    '10:00:00', '17:00:00', 'ACTIVO'),
(6, 4, 'VIERNES',   '15:00:00', '20:00:00', 'INACTIVO');

-- ---------------------------------------------------------------------
-- EVIDENCIA: documentos de respaldo de los técnicos
-- ---------------------------------------------------------------------
INSERT INTO evidencia (idEvidencia, tecnico_id, tipo_evidencia, url_archivo, descripcion, estado_validacion, fecha_carga) VALUES
(1, 1, 'CERTIFICADO',         'https://archivos.tecnicoya.example/evidencias/tec1-certificado-electricista.pdf', 'Certificado de electricista industrial',          'APROBADO',  '2026-01-21 09:00:00'),
(2, 2, 'ANTECEDENTES',        'https://archivos.tecnicoya.example/evidencias/tec2-antecedentes.pdf',            'Certificado de antecedentes policiales',          'APROBADO',  '2026-02-02 10:30:00'),
(3, 3, 'FOTO_TRABAJO',        'https://archivos.tecnicoya.example/evidencias/tec3-trabajo-refrigeracion.jpg',   'Foto de mantenimiento de un equipo de aire acondicionado', 'APROBADO',  '2026-03-15 17:20:00'),
(4, 4, 'DOCUMENTO_IDENTIDAD', 'https://archivos.tecnicoya.example/evidencias/tec4-documento-identidad.pdf',     'Documento de identidad (ambas caras)',            'PENDIENTE', '2026-09-15 12:30:00');

-- ---------------------------------------------------------------------
-- EXPERIENCIA_LABORAL
-- ---------------------------------------------------------------------
INSERT INTO experiencia_laboral (idExperiencia, tecnico_id, empresa, cargo, descripcion, fecha_inicio, fecha_fin, actualidad) VALUES
(1, 1, 'Electro Instalaciones del Sur S.A.C.', 'Electricista',                 'Instalaciones eléctricas en edificios residenciales.', '2018-03-01', '2022-12-31', FALSE),
(2, 1, 'Trabajador independiente',             'Electricista independiente',   'Atención de emergencias eléctricas a domicilio.',      '2023-01-15', NULL,         TRUE),
(3, 2, 'Constructora Andina S.A.',             'Técnica en plomería',          'Instalaciones sanitarias en obras de vivienda.',       '2016-05-01', '2024-06-30', FALSE),
(4, 3, 'Frío Total E.I.R.L.',                  'Técnico de refrigeración',     'Mantenimiento preventivo y correctivo de equipos.',    '2019-02-01', NULL,         TRUE),
(5, 4, 'SoporteTI Soluciones S.A.C.',          'Técnico de soporte',           'Mesa de ayuda y reparación de equipos de cómputo.',    '2021-07-01', '2025-12-31', FALSE);

-- ---------------------------------------------------------------------
-- CALIFICACION: solo servicios FINALIZADOS (máximo una por servicio)
-- ---------------------------------------------------------------------
INSERT INTO calificacion (idCalificacion, servicio_id, puntuacion, comentario, fecha_calificacion) VALUES
(1, 1, 5, 'Excelente trabajo, muy puntual y explicó todo lo que hizo.',       '2026-08-04 18:00:00'),
(2, 2, 4, 'Resolvió la fuga rápido, aunque llegó un poco tarde.',             '2026-08-11 09:30:00'),
(3, 3, 5, 'Muy profesional, la refrigeradora quedó funcionando perfecto.',    '2026-08-21 08:15:00');

-- ---------------------------------------------------------------------
-- EVIDENCIA_PAGO: pagos de servicios con técnico asignado
-- ---------------------------------------------------------------------
INSERT INTO evidencia_pago (idEvidenciaPago, servicio_id, monto, metodo_pago, archivo_evidencia, fecha_pago, estado_validacion) VALUES
(1, 1, 150.00, 'BILLETERA_DIGITAL', 'https://archivos.tecnicoya.example/pagos/servicio1-comprobante.png', '2026-08-04 12:45:00', 'APROBADO'),
(2, 2, 120.00, 'EFECTIVO',          'https://archivos.tecnicoya.example/pagos/servicio2-recibo.jpg',      '2026-08-10 13:30:00', 'APROBADO'),
(3, 3, 200.00, 'TRANSFERENCIA',     'https://archivos.tecnicoya.example/pagos/servicio3-voucher.pdf',     '2026-08-20 12:00:00', 'APROBADO'),
(4, 4,  80.00, 'TARJETA_DEBITO',    'https://archivos.tecnicoya.example/pagos/servicio4-adelanto.png',    '2026-09-25 09:40:00', 'PENDIENTE');

-- ---------------------------------------------------------------------
-- PLAN_SUSCRIPCION
-- ---------------------------------------------------------------------
INSERT INTO plan_suscripcion (idPlan, nombre_plan, precio_inicial, limite_clientes, precio_posterior, descripcion, estado) VALUES
(1, 'Básico',      0.00,   5, 29.90, 'Gratis hasta 5 clientes al mes; luego se cobra el precio posterior.',       'ACTIVO'),
(2, 'Profesional', 29.90, 30, 49.90, 'Hasta 30 clientes al mes y posicionamiento destacado en búsquedas.',        'ACTIVO'),
(3, 'Premium',     59.90, 100, 89.90, 'Hasta 100 clientes al mes, soporte prioritario y estadísticas avanzadas.', 'ACTIVO');

-- ---------------------------------------------------------------------
-- SUSCRIPCION: a lo sumo una ACTIVA por técnico
-- ---------------------------------------------------------------------
INSERT INTO suscripcion (idSuscripcion, tecnico_id, plan_id, fecha_inicio, fecha_fin, fecha_cancelacion, estado_suscripcion) VALUES
(1, 1, 2, '2026-08-01', '2026-08-31', NULL,         'VENCIDA'),
(2, 1, 2, '2026-09-01', '2026-09-30', NULL,         'ACTIVA'),
(3, 2, 3, '2026-09-01', '2026-09-30', NULL,         'ACTIVA'),
(4, 3, 1, '2026-07-15', '2026-08-14', '2026-08-01', 'CANCELADA'),
(5, 3, 1, '2026-09-10', '2026-10-09', NULL,         'ACTIVA'),
(6, 4, 2, '2026-09-20', '2026-10-19', NULL,         'PENDIENTE');

-- ---------------------------------------------------------------------
-- PAGO_SUSCRIPCION: solo planes de pago (el plan Básico inicia gratis)
-- ---------------------------------------------------------------------
INSERT INTO pago_suscripcion (idPagoSuscripcion, suscripcion_id, monto_pago, metodo_pago, estado_pago, fecha_pago) VALUES
(1, 1, 29.90, 'TARJETA_CREDITO',   'APROBADO',  '2026-08-01 08:00:00'),
(2, 2, 29.90, 'TARJETA_CREDITO',   'APROBADO',  '2026-09-01 08:00:00'),
(3, 3, 59.90, 'TRANSFERENCIA',     'APROBADO',  '2026-09-01 10:20:00'),
(4, 6, 29.90, 'TARJETA_CREDITO',   'RECHAZADO', '2026-09-20 10:00:00'),
(5, 6, 29.90, 'BILLETERA_DIGITAL', 'PENDIENTE', '2026-09-21 09:30:00');

COMMIT;
