# Referencia de la API REST

Endpoints de la API de **TécnicoYa**: parámetros, respuestas y ejemplos de uso. Todos los ejemplos de respuesta son reales y usan los datos de ejemplo de `database/data.sql`.

- Para probarlos: importe [`postman/TecnicoYa-API.postman_collection.json`](../postman/TecnicoYa-API.postman_collection.json) en Postman.
- Para la arquitectura y el modelo de datos: [descripcion-sistema.md](descripcion-sistema.md).

## Contenido

1. [Convenciones](#convenciones)
2. [Errores](#errores)
3. [Valores permitidos](#valores-permitidos)
4. [Autenticación](#autenticación)
5. Recursos: [Usuarios](#usuarios) · [Técnicos](#técnicos) · [Servicios](#servicios) · [Disponibilidades](#disponibilidades) · [Evidencias](#evidencias) · [Experiencias laborales](#experiencias-laborales) · [Calificaciones](#calificaciones) · [Evidencias de pago](#evidencias-de-pago) · [Planes de suscripción](#planes-de-suscripción) · [Suscripciones](#suscripciones) · [Pagos de suscripción](#pagos-de-suscripción)

---

## Convenciones

**URL base:** `http://localhost:8080/api`

**Formato:** JSON en UTF-8. En `POST` y `PUT` envíe la cabecera `Content-Type: application/json`.

| Tipo de dato | Formato | Ejemplo |
|---|---|---|
| Fecha y hora | `yyyy-MM-ddTHH:mm:ss` (hora local, sin zona) | `"2026-10-02T15:00:00"` |
| Fecha | `yyyy-MM-dd` | `"2026-10-02"` |
| Hora | `HH:mm` o `HH:mm:ss` | `"08:00"` |
| Monto | Número con hasta 2 decimales | `29.90` |
| Id | Número entero generado por la base de datos | `5` |

### Operaciones CRUD

Los 11 recursos exponen las mismas cinco operaciones:

| Operación | Método y ruta | Respuesta correcta | Errores posibles |
|---|---|---|---|
| Listar | `GET /api/{recurso}` | `200` con un arreglo | — |
| Obtener | `GET /api/{recurso}/{id}` | `200` con el registro | `400` id no numérico, `404` |
| Crear | `POST /api/{recurso}` | `201` con el registro y la cabecera `Location` | `400`, `404`, `409` |
| Actualizar | `PUT /api/{recurso}/{id}` | `200` con el registro actualizado | `400`, `404`, `409` |
| Eliminar | `DELETE /api/{recurso}/{id}` | `204` sin cuerpo | `404`, `409` si otros registros dependen de él |

**`PUT` reemplaza el registro completo.** Siempre hay que enviar los campos obligatorios; un campo opcional de datos que se omita (por ejemplo `descripcion`) queda en `null`. Hay dos excepciones: los campos de **estado** (`estado`, `estadoServicio`, `prioridad`, `estadoValidacion`…) y la **contraseña** conservan su valor actual si no se envían.

Los campos que asigna el sistema (ids, `fechaRegistro`, `fechaSolicitud`, `fechaCarga`, `fechaCalificacion`) se ignoran si se envían.

---

## Errores

Todos los errores devuelven el mismo cuerpo JSON:

| Campo | Descripción |
|---|---|
| `timestamp` | Fecha y hora del error |
| `status` | Código HTTP |
| `error` | Nombre del código en español |
| `mensaje` | Qué pasó, en español |
| `ruta` | Ruta solicitada |
| `errores` | Solo en errores de validación: mensaje por cada campo inválido |

| Código | `error` | Cuándo ocurre |
|---|---|---|
| `400` | Solicitud incorrecta | Datos inválidos, JSON mal formado, valor de enum inexistente, id no numérico o regla de negocio incumplida |
| `401` | No autorizado | Credenciales incorrectas o cuenta no `ACTIVO` en el inicio de sesión |
| `404` | No encontrado | El registro (o uno al que se hace referencia) no existe, o la ruta no existe |
| `405` | Método no permitido | Método HTTP no soportado en la ruta (p. ej. `DELETE /api/usuarios`) |
| `409` | Conflicto | Dato único duplicado, conflicto con otro registro o intento de borrar un registro con datos relacionados |
| `415` | Tipo de contenido no soportado | Falta `Content-Type: application/json` |
| `500` | Error interno del servidor | Error inesperado (queda registrado en el log) |

**Validación de campos** (`400`):

```json
{
  "timestamp": "2026-09-27T19:50:57.6868739",
  "status": 400,
  "error": "Solicitud incorrecta",
  "mensaje": "La solicitud contiene datos inválidos",
  "ruta": "/api/usuarios",
  "errores": {
    "nombres": "Los nombres son obligatorios",
    "contrasena": "La contraseña debe tener entre 8 y 72 caracteres",
    "correo": "El correo no tiene un formato válido"
  }
}
```

**Valor de enum no válido** (`400`):

```json
{
  "timestamp": "2026-09-27T19:50:57.6878748",
  "status": 400,
  "error": "Solicitud incorrecta",
  "mensaje": "Valor 'JEFE' no válido para el campo 'tipoUsuario'. Valores permitidos: [CLIENTE, TECNICO, ADMINISTRADOR]",
  "ruta": "/api/usuarios"
}
```

**Regla de negocio** (`400`):

```json
{
  "timestamp": "2026-09-27T19:50:57.7036433",
  "status": 400,
  "error": "Solicitud incorrecta",
  "mensaje": "Un servicio en estado EN_PROCESO debe tener un técnico asignado",
  "ruta": "/api/servicios"
}
```

**Registro inexistente** (`404`):

```json
{
  "timestamp": "2026-09-27T19:50:57.6878748",
  "status": 404,
  "error": "No encontrado",
  "mensaje": "No se encontró el usuario con id 999",
  "ruta": "/api/usuarios/999"
}
```

**Borrado de un registro con datos relacionados** (`409`):

```json
{
  "timestamp": "2026-09-27T19:50:57.6878748",
  "status": 409,
  "error": "Conflicto",
  "mensaje": "No se puede eliminar o modificar el registro porque tiene información relacionada (por ejemplo servicios, pagos o suscripciones)",
  "ruta": "/api/usuarios/2"
}
```

---

## Valores permitidos

Los campos de tipo y estado solo aceptan estos valores, escritos exactamente así:

| Enum | Usado en | Valores |
|---|---|---|
| `TipoUsuario` | `tipoUsuario` | `CLIENTE`, `TECNICO`, `ADMINISTRADOR` |
| `EstadoUsuario` | `estado` (usuario) | `ACTIVO`, `INACTIVO`, `BLOQUEADO` |
| `EstadoVerificacion` | `estadoVerificacion` | `PENDIENTE`, `VERIFICADO`, `RECHAZADO` |
| `EstadoServicio` | `estadoServicio` | `PENDIENTE`, `ASIGNADO`, `EN_PROCESO`, `FINALIZADO`, `CANCELADO` |
| `Prioridad` | `prioridad` | `BAJA`, `MEDIA`, `ALTA`, `URGENTE` |
| `DiaSemana` | `diaSemana` | `LUNES`, `MARTES`, `MIERCOLES`, `JUEVES`, `VIERNES`, `SABADO`, `DOMINGO` |
| `EstadoRegistro` | `estado` (disponibilidad y plan) | `ACTIVO`, `INACTIVO` |
| `TipoEvidencia` | `tipoEvidencia` | `CERTIFICADO`, `TITULO_PROFESIONAL`, `ANTECEDENTES`, `DOCUMENTO_IDENTIDAD`, `FOTO_TRABAJO`, `OTRO` |
| `EstadoValidacion` | `estadoValidacion` | `PENDIENTE`, `APROBADO`, `RECHAZADO` |
| `MetodoPago` | `metodoPago` | `EFECTIVO`, `TARJETA_CREDITO`, `TARJETA_DEBITO`, `TRANSFERENCIA`, `BILLETERA_DIGITAL` |
| `EstadoSuscripcion` | `estadoSuscripcion` | `PENDIENTE`, `ACTIVA`, `VENCIDA`, `CANCELADA` |
| `EstadoPago` | `estadoPago` | `PENDIENTE`, `APROBADO`, `RECHAZADO`, `REEMBOLSADO` |

---

## Autenticación

### `POST /api/auth/login`

Valida el correo y la contraseña y devuelve los datos del usuario. **No emite un token:** el resto de los endpoints no exigen autenticación por ahora.

| Campo | Tipo | Obligatorio | Validación |
|---|---|---|---|
| `correo` | texto | Sí | Formato de correo. No distingue mayúsculas. |
| `contrasena` | texto | Sí | — |

| Respuesta | Cuándo |
|---|---|
| `200` | Credenciales correctas y cuenta `ACTIVO` |
| `400` | Falta el correo o la contraseña, o el correo no tiene formato válido |
| `401` | `"Correo o contraseña incorrectos"`: el mensaje es el mismo si el correo no existe, para no revelar qué correos están registrados |
| `401` | `"La cuenta está INACTIVO; comuníquese con el administrador"` (o `BLOQUEADO`): la contraseña es correcta, pero la cuenta no está activa |

Petición:

```json
{ "correo": "maria.quispe@correo.example", "contrasena": "TecnicoYa2026!" }
```

Respuesta `200`:

```json
{
  "idUsuario": 2,
  "nombres": "María Fernanda",
  "apellidos": "Quispe Huamán",
  "correo": "maria.quispe@correo.example",
  "telefono": "987200301",
  "tipoUsuario": "CLIENTE",
  "estado": "ACTIVO",
  "fechaRegistro": "2026-02-11T18:22:10"
}
```

Respuesta `401`:

```json
{
  "timestamp": "2026-09-27T19:50:57.6127917",
  "status": 401,
  "error": "No autorizado",
  "mensaje": "Correo o contraseña incorrectos",
  "ruta": "/api/auth/login"
}
```

---

## Usuarios

`/api/usuarios`: clientes, técnicos y administradores.

| Campo | Tipo | Obligatorio | Validación / notas |
|---|---|---|---|
| `nombres` | texto | Sí | Máximo 100 caracteres |
| `apellidos` | texto | Sí | Máximo 100 caracteres |
| `correo` | texto | Sí | Formato de correo, máximo 150. Se guarda en minúsculas. Único. |
| `contrasena` | texto | Al crear | 8 a 72 caracteres. Al actualizar es opcional: si se omite, se conserva. |
| `telefono` | texto | No | 7 a 15 dígitos, opcionalmente precedidos de `+` |
| `tipoUsuario` | `TipoUsuario` | Sí | — |
| `estado` | `EstadoUsuario` | No | Por defecto `ACTIVO` |

**Reglas**

- Correo ya registrado → `409`.
- Un usuario con perfil de técnico no puede cambiar su `tipoUsuario` → `400`.
- No se puede eliminar un usuario con servicios → `409`. Si tiene perfil de técnico, este se elimina junto con él.
- La respuesta **nunca** incluye la contraseña.

Crear (`POST /api/usuarios`):

```json
{
  "nombres": "Lucía",
  "apellidos": "Campos Núñez",
  "correo": "lucia.campos@correo.example",
  "contrasena": "ClaveSegura1",
  "telefono": "987111222",
  "tipoUsuario": "CLIENTE"
}
```

Respuesta de `GET /api/usuarios/2`:

```json
{
  "idUsuario": 2,
  "nombres": "María Fernanda",
  "apellidos": "Quispe Huamán",
  "correo": "maria.quispe@correo.example",
  "telefono": "987200301",
  "tipoUsuario": "CLIENTE",
  "estado": "ACTIVO",
  "fechaRegistro": "2026-02-11T18:22:10"
}
```

---

## Técnicos

`/api/tecnicos`: perfil profesional de un usuario de tipo `TECNICO`.

| Campo | Tipo | Obligatorio | Validación / notas |
|---|---|---|---|
| `usuarioId` | id | Sí | Usuario existente de tipo `TECNICO` |
| `especialidad` | texto | Sí | Máximo 100 caracteres |
| `descripcion` | texto | No | Máximo 1000 caracteres |
| `estadoVerificacion` | `EstadoVerificacion` | No | Por defecto `PENDIENTE` |

**Reglas**

- El usuario no es de tipo `TECNICO` → `400`. Ya tiene perfil de técnico → `409`.
- Solo los técnicos `VERIFICADO` pueden asignarse a servicios.
- No se puede eliminar un técnico con servicios o suscripciones → `409`. Su disponibilidad, evidencias y experiencia se eliminan junto con él.

Crear (`POST /api/tecnicos`):

```json
{ "usuarioId": 9, "especialidad": "Pintura y acabados", "descripcion": "Pintura de interiores y exteriores." }
```

Respuesta de `GET /api/tecnicos/1` (incluye los datos de contacto del usuario):

```json
{
  "idTecnico": 1,
  "usuarioId": 5,
  "nombreCompleto": "Pedro Alonso Ramírez Castillo",
  "correo": "pedro.ramirez@tecnicoya.example",
  "telefono": "987500604",
  "especialidad": "Electricidad",
  "descripcion": "Instalaciones eléctricas domiciliarias, tableros y cortocircuitos.",
  "estadoVerificacion": "VERIFICADO"
}
```

---

## Servicios

`/api/servicios`: atención que un cliente solicita y que realiza un técnico.

| Campo | Tipo | Obligatorio | Validación / notas |
|---|---|---|---|
| `clienteId` | id | Sí | Usuario de tipo `CLIENTE` en estado `ACTIVO` |
| `tecnicoId` | id | No | Técnico `VERIFICADO`. Puede quedar vacío mientras no se asigne. |
| `titulo` | texto | Sí | Máximo 150 caracteres |
| `descripcionProblema` | texto | Sí | Máximo 1000 caracteres |
| `estadoServicio` | `EstadoServicio` | No | Por defecto `ASIGNADO` si hay técnico, `PENDIENTE` si no |
| `prioridad` | `Prioridad` | No | Por defecto `MEDIA` |
| `fechaServicio` | fecha y hora | No | Fecha programada; no puede ser anterior a la de solicitud |
| `fechaCierre` | fecha y hora | No | Solo en `FINALIZADO` o `CANCELADO`; si se omite, se usa la fecha actual |

**Reglas**

- `fechaSolicitud` la asigna el sistema al crear.
- `ASIGNADO`, `EN_PROCESO` y `FINALIZADO` exigen un técnico → `400`.
- Enviar `fechaCierre` en un estado abierto → `400`.
- No se puede eliminar un servicio con evidencias de pago → `409`. Su calificación se elimina junto con él.

Crear (`POST /api/servicios`):

```json
{
  "clienteId": 2,
  "tecnicoId": 1,
  "titulo": "Enchufe quemado",
  "descripcionProblema": "El enchufe de la sala echa chispas.",
  "prioridad": "ALTA",
  "fechaServicio": "2026-10-05T10:00:00"
}
```

Finalizar (`PUT /api/servicios/{id}`); `fechaCierre` se completa sola:

```json
{
  "clienteId": 2,
  "tecnicoId": 1,
  "titulo": "Enchufe quemado",
  "descripcionProblema": "Se cambió el tomacorriente.",
  "estadoServicio": "FINALIZADO"
}
```

Respuesta de `GET /api/servicios/1`:

```json
{
  "idServicio": 1,
  "clienteId": 2,
  "clienteNombre": "María Fernanda Quispe Huamán",
  "tecnicoId": 1,
  "tecnicoNombre": "Pedro Alonso Ramírez Castillo",
  "titulo": "Cortocircuito en la cocina",
  "descripcionProblema": "Al encender el microondas se baja la llave general y huele a quemado.",
  "estadoServicio": "FINALIZADO",
  "prioridad": "ALTA",
  "fechaSolicitud": "2026-08-03T09:15:00",
  "fechaServicio": "2026-08-04T10:00:00",
  "fechaCierre": "2026-08-04T12:30:00"
}
```

Un servicio sin técnico devuelve `"tecnicoId": null` y `"tecnicoNombre": null`.

---

## Disponibilidades

`/api/disponibilidades`: franjas horarias semanales en las que atiende un técnico.

| Campo | Tipo | Obligatorio | Validación / notas |
|---|---|---|---|
| `tecnicoId` | id | Sí | Técnico existente |
| `diaSemana` | `DiaSemana` | Sí | — |
| `horaInicio` | hora | Sí | — |
| `horaFin` | hora | Sí | Posterior a `horaInicio` |
| `estado` | `EstadoRegistro` | No | Por defecto `ACTIVO` |

**Reglas**

- `horaFin` igual o anterior a `horaInicio` → `400`.
- La franja se cruza con otra del mismo técnico el mismo día → `409`.

Crear (`POST /api/disponibilidades`):

```json
{ "tecnicoId": 3, "diaSemana": "DOMINGO", "horaInicio": "09:00", "horaFin": "12:00" }
```

Respuesta de `GET /api/disponibilidades/1`:

```json
{
  "idDisponibilidad": 1,
  "tecnicoId": 1,
  "tecnicoNombre": "Pedro Alonso Ramírez Castillo",
  "diaSemana": "LUNES",
  "horaInicio": "08:00:00",
  "horaFin": "13:00:00",
  "estado": "ACTIVO"
}
```

---

## Evidencias

`/api/evidencias`: documentos que respaldan la idoneidad de un técnico.

| Campo | Tipo | Obligatorio | Validación / notas |
|---|---|---|---|
| `tecnicoId` | id | Sí | Técnico existente |
| `tipoEvidencia` | `TipoEvidencia` | Sí | — |
| `urlArchivo` | texto | Sí | URL `http://` o `https://`, máximo 500 caracteres |
| `descripcion` | texto | No | Máximo 500 caracteres |
| `estadoValidacion` | `EstadoValidacion` | No | Por defecto `PENDIENTE` |

`fechaCarga` la asigna el sistema al crear.

Crear (`POST /api/evidencias`):

```json
{
  "tecnicoId": 2,
  "tipoEvidencia": "CERTIFICADO",
  "urlArchivo": "https://archivos.tecnicoya.example/evidencias/prueba-certificado.pdf",
  "descripcion": "Certificado de prueba"
}
```

Respuesta de `GET /api/evidencias/1`:

```json
{
  "idEvidencia": 1,
  "tecnicoId": 1,
  "tecnicoNombre": "Pedro Alonso Ramírez Castillo",
  "tipoEvidencia": "CERTIFICADO",
  "urlArchivo": "https://archivos.tecnicoya.example/evidencias/tec1-certificado-electricista.pdf",
  "descripcion": "Certificado de electricista industrial",
  "estadoValidacion": "APROBADO",
  "fechaCarga": "2026-01-21T09:00:00"
}
```

---

## Experiencias laborales

`/api/experiencias-laborales`: empleos anteriores o actuales de un técnico.

| Campo | Tipo | Obligatorio | Validación / notas |
|---|---|---|---|
| `tecnicoId` | id | Sí | Técnico existente |
| `empresa` | texto | Sí | Máximo 150 caracteres |
| `cargo` | texto | Sí | Máximo 100 caracteres |
| `descripcion` | texto | No | Máximo 1000 caracteres |
| `fechaInicio` | fecha | Sí | No puede ser futura |
| `fechaFin` | fecha | Según `actualidad` | No puede ser futura ni anterior a `fechaInicio` |
| `actualidad` | booleano | Sí | `true` si aún trabaja allí |

**Reglas**

- Con `actualidad: true` **no** se envía `fechaFin`; con `actualidad: false` es obligatoria → `400` si no se cumple.

Crear (`POST /api/experiencias-laborales`):

```json
{
  "tecnicoId": 2,
  "empresa": "Servicios Integrales S.A.C.",
  "cargo": "Ayudante de plomería",
  "fechaInicio": "2014-01-06",
  "fechaFin": "2016-04-30",
  "actualidad": false
}
```

Respuesta de `GET /api/experiencias-laborales/2` (trabajo actual):

```json
{
  "idExperiencia": 2,
  "tecnicoId": 1,
  "tecnicoNombre": "Pedro Alonso Ramírez Castillo",
  "empresa": "Trabajador independiente",
  "cargo": "Electricista independiente",
  "descripcion": "Atención de emergencias eléctricas a domicilio.",
  "fechaInicio": "2023-01-15",
  "fechaFin": null,
  "actualidad": true
}
```

---

## Calificaciones

`/api/calificaciones`: valoración que recibe un servicio finalizado.

| Campo | Tipo | Obligatorio | Validación / notas |
|---|---|---|---|
| `servicioId` | id | Sí | Servicio en estado `FINALIZADO` |
| `puntuacion` | entero | Sí | De 1 a 5 |
| `comentario` | texto | No | Máximo 500 caracteres |

**Reglas**

- El servicio no está `FINALIZADO` → `400`.
- El servicio ya fue calificado → `409` (máximo una calificación por servicio).
- `fechaCalificacion` la asigna el sistema al crear.

Crear (`POST /api/calificaciones`):

```json
{ "servicioId": 7, "puntuacion": 5, "comentario": "Excelente trabajo, muy puntual." }
```

Respuesta de `GET /api/calificaciones/1` (incluye el técnico calificado):

```json
{
  "idCalificacion": 1,
  "servicioId": 1,
  "servicioTitulo": "Cortocircuito en la cocina",
  "tecnicoId": 1,
  "tecnicoNombre": "Pedro Alonso Ramírez Castillo",
  "puntuacion": 5,
  "comentario": "Excelente trabajo, muy puntual y explicó todo lo que hizo.",
  "fechaCalificacion": "2026-08-04T18:00:00"
}
```

---

## Evidencias de pago

`/api/evidencias-pago`: comprobantes del pago de un servicio.

| Campo | Tipo | Obligatorio | Validación / notas |
|---|---|---|---|
| `servicioId` | id | Sí | Servicio no `CANCELADO` y con técnico asignado |
| `monto` | monto | Sí | Mayor que 0; hasta 8 enteros y 2 decimales |
| `metodoPago` | `MetodoPago` | Sí | — |
| `archivoEvidencia` | texto | Sí | URL `http://` o `https://` del comprobante, máximo 500 |
| `fechaPago` | fecha y hora | No | No puede ser futura; si se omite, se usa la actual |
| `estadoValidacion` | `EstadoValidacion` | No | Por defecto `PENDIENTE` |

**Reglas**

- Servicio `CANCELADO` o sin técnico → `400`.
- No se puede eliminar un servicio que tenga evidencias de pago.

Crear (`POST /api/evidencias-pago`):

```json
{
  "servicioId": 4,
  "monto": 50.00,
  "metodoPago": "TRANSFERENCIA",
  "archivoEvidencia": "https://archivos.tecnicoya.example/pagos/prueba-voucher.png"
}
```

Respuesta de `GET /api/evidencias-pago/1`:

```json
{
  "idEvidenciaPago": 1,
  "servicioId": 1,
  "servicioTitulo": "Cortocircuito en la cocina",
  "monto": 150.00,
  "metodoPago": "BILLETERA_DIGITAL",
  "archivoEvidencia": "https://archivos.tecnicoya.example/pagos/servicio1-comprobante.png",
  "fechaPago": "2026-08-04T12:45:00",
  "estadoValidacion": "APROBADO"
}
```

---

## Planes de suscripción

`/api/planes-suscripcion`: planes que la plataforma ofrece a los técnicos. Cada plan cobra `precioInicial` hasta atender `limiteClientes` clientes, y `precioPosterior` a partir de ahí.

| Campo | Tipo | Obligatorio | Validación / notas |
|---|---|---|---|
| `nombrePlan` | texto | Sí | Máximo 100 caracteres. Único, sin distinguir mayúsculas. |
| `precioInicial` | monto | Sí | ≥ 0 |
| `limiteClientes` | entero | Sí | ≥ 1 |
| `precioPosterior` | monto | Sí | ≥ 0 |
| `descripcion` | texto | No | Máximo 500 caracteres |
| `estado` | `EstadoRegistro` | No | Por defecto `ACTIVO` |

**Reglas**

- Nombre repetido → `409`.
- Un plan `INACTIVO` no puede contratarse en nuevas suscripciones.
- No se puede eliminar un plan con suscripciones → `409`.

Crear (`POST /api/planes-suscripcion`):

```json
{ "nombrePlan": "Empresarial", "precioInicial": 99.90, "limiteClientes": 500, "precioPosterior": 149.90 }
```

Respuesta de `GET /api/planes-suscripcion/2`:

```json
{
  "idPlan": 2,
  "nombrePlan": "Profesional",
  "precioInicial": 29.90,
  "limiteClientes": 30,
  "precioPosterior": 49.90,
  "descripcion": "Hasta 30 clientes al mes y posicionamiento destacado en búsquedas.",
  "estado": "ACTIVO"
}
```

---

## Suscripciones

`/api/suscripciones`: contratación de un plan por parte de un técnico.

| Campo | Tipo | Obligatorio | Validación / notas |
|---|---|---|---|
| `tecnicoId` | id | Sí | Técnico existente |
| `planId` | id | Sí | Plan en estado `ACTIVO` |
| `fechaInicio` | fecha | Sí | — |
| `fechaFin` | fecha | Sí | Posterior a `fechaInicio` |
| `fechaCancelacion` | fecha | No | Solo en `CANCELADA`; si se omite, se usa la fecha actual |
| `estadoSuscripcion` | `EstadoSuscripcion` | No | Por defecto `PENDIENTE` |

**Reglas**

- `fechaFin` igual o anterior a `fechaInicio`, o plan `INACTIVO` → `400`.
- Un técnico solo puede tener **una** suscripción `ACTIVA` → `409`.
- No se puede eliminar una suscripción con pagos → `409`.

Crear (`POST /api/suscripciones`):

```json
{ "tecnicoId": 4, "planId": 3, "fechaInicio": "2026-10-20", "fechaFin": "2026-11-19" }
```

Cancelar (`PUT /api/suscripciones/{id}`); `fechaCancelacion` se completa sola:

```json
{ "tecnicoId": 4, "planId": 3, "fechaInicio": "2026-10-20", "fechaFin": "2026-11-19", "estadoSuscripcion": "CANCELADA" }
```

Respuesta de `GET /api/suscripciones/2`:

```json
{
  "idSuscripcion": 2,
  "tecnicoId": 1,
  "tecnicoNombre": "Pedro Alonso Ramírez Castillo",
  "planId": 2,
  "nombrePlan": "Profesional",
  "fechaInicio": "2026-09-01",
  "fechaFin": "2026-09-30",
  "fechaCancelacion": null,
  "estadoSuscripcion": "ACTIVA"
}
```

---

## Pagos de suscripción

`/api/pagos-suscripcion`: pagos que un técnico realiza por su suscripción.

| Campo | Tipo | Obligatorio | Validación / notas |
|---|---|---|---|
| `suscripcionId` | id | Sí | Suscripción que no esté `CANCELADA` |
| `montoPago` | monto | Sí | Mayor que 0; hasta 8 enteros y 2 decimales |
| `metodoPago` | `MetodoPago` | Sí | — |
| `estadoPago` | `EstadoPago` | No | Por defecto `PENDIENTE` |
| `fechaPago` | fecha y hora | No | No puede ser futura; si se omite, se usa la actual |

**Reglas**

- Suscripción `CANCELADA` → `400`.

Crear (`POST /api/pagos-suscripcion`):

```json
{ "suscripcionId": 2, "montoPago": 29.90, "metodoPago": "TARJETA_CREDITO" }
```

Respuesta de `GET /api/pagos-suscripcion/1`:

```json
{
  "idPagoSuscripcion": 1,
  "suscripcionId": 1,
  "tecnicoId": 1,
  "nombrePlan": "Profesional",
  "montoPago": 29.90,
  "metodoPago": "TARJETA_CREDITO",
  "estadoPago": "APROBADO",
  "fechaPago": "2026-08-01T08:00:00"
}
```
