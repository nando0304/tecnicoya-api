# Librerías Java, patrón DAO y seguridad

Este documento indica **dónde y para qué** se usa cada librería y cada medida de seguridad en la API de TécnicoYa. Cada fila apunta a la clase donde se puede ver el código.

## Resumen

| Tema | Librería / técnica | Uso en el proyecto |
|---|---|---|
| Google Guava | `com.google.guava:guava` | Límite de intentos de login (caché), validaciones, colecciones inmutables, agrupación de datos para reportes |
| Apache POI | `org.apache.poi:poi-ooxml` | Reportes en Excel (`.xlsx`) descargables desde la web |
| Apache Commons | Lang, IO, Codec, Collections | Normalización de textos, manejo seguro de archivos, huella SHA-256, utilidades de colecciones |
| Logback | incluido en Spring Boot | Log general en archivo rotativo y log de auditoría de seguridad |
| Patrón DAO | JPA (`EntityManager`) + JPQL | Capa de acceso a datos `dao/` con interfaces `IXxxDao` e implementaciones `XxxDaoImpl` |
| Seguridad | JWT, BCrypt, validaciones, OWASP | Autenticación, roles, fuerza bruta, XSS, inyección SQL, cabeceras, auditoría, escaneo de dependencias |

Las versiones están en `pom.xml` (propiedades `guava.version`, `poi.version`, etc.).

---

## Google Guava

| Utilidad | Dónde | Para qué |
|---|---|---|
| `CacheBuilder` / `Cache` | `security/LimiteIntentosLogin` | Cuenta los intentos fallidos de login por correo + IP. Cada entrada caduca sola a los 15 minutos (`expireAfterWrite`) y la caché tiene un tamaño máximo (`maximumSize`) |
| `Ticker` | `security/LimiteIntentosLogin` y su prueba | Permite simular el paso del tiempo en las pruebas sin esperar 15 minutos reales |
| `Preconditions` (`checkNotNull`, `checkArgument`) | `dao/impl/GenericDaoImpl`, `LimiteIntentosLogin`, `util/LibroExcel` | Validar argumentos al inicio de los métodos con un mensaje claro |
| `ImmutableList`, `ImmutableSet`, `Sets.immutableEnumSet` | `ArchivoServiceImpl`, `ServicioServiceImpl`, `SinHtmlValidator`, `AuditoriaInterceptor`, `ReporteServiceImpl` | Constantes que nadie puede modificar en tiempo de ejecución |
| `Maps.immutableEnumMap` | `dao/impl/EstadisticasDaoImpl` | Devuelve los conteos por estado/tipo como un mapa inmutable |
| `Multimap` (`Multimaps.index`) y `FluentIterable` | `service/impl/ReporteServiceImpl` | Agrupa servicios y calificaciones por técnico (técnico → lista) en una sola pasada para calcular sus indicadores |
| `Stopwatch` | `security/AuditoriaInterceptor` | Mide cuánto demora cada operación que se registra en la auditoría |
| `Strings.nullToEmpty` | `util/LibroExcel` | Escribir celdas vacías en lugar de `null` |

## Apache POI

| Dónde | Qué hace |
|---|---|
| `util/LibroExcel` | Clase reutilizable que arma un libro `.xlsx` (`XSSFWorkbook`): título, encabezados con estilo, filtros automáticos, fila de encabezados fija, anchos de columna ajustados y formatos de moneda (`S/`), decimal y fecha |
| `service/ReporteService` y `service/impl/ReporteServiceImpl` | Genera los reportes de usuarios, técnicos (con servicios atendidos y calificación promedio), servicios y un reporte general con hoja de resumen |
| `controller/ReporteController` | Endpoints `GET /api/reportes/{usuarios,tecnicos,servicios,general}` que devuelven el archivo como adjunto |
| Web: `features/exportar-reporte` | Botones **Exportar Excel** en Usuarios, Técnicos y Servicios, y **Descargar reporte general** en el panel del administrador |

Los textos se escriben siempre como texto y nunca como fórmula, por lo que un valor malicioso como `=HYPERLINK(...)` no se ejecuta al abrir el archivo (*inyección de fórmulas*).

## Apache Commons

| Componente | Dónde | Para qué |
|---|---|---|
| **Commons Lang** (`StringUtils`) | Servicios (`normalizeSpace`, `isBlank`), `SinHtmlValidator` (`deleteWhitespace`, `containsIgnoreCase`), `ArchivoServiceImpl` | Normalizar los textos que llegan en las solicitudes y detectar código peligroso |
| **Commons IO** (`FilenameUtils`, `BoundedInputStream`, `FileUtils`) | `ArchivoServiceImpl`, `GlobalExceptionHandler` | Obtener nombre y extensión seguros de los archivos subidos (evita rutas como `../../`), medir su tamaño sin cargarlo en memoria y mostrar tamaños legibles ("5 MB") |
| **Commons Codec** (`DigestUtils`) | `ArchivoServiceImpl` | Calcular la huella SHA-256 de cada archivo subido, que se usa como su nombre |
| **Commons Collections** (`CollectionUtils`) | `util/LibroExcel` | Comprobar de forma segura ante `null` si un listado viene vacío |

## Logback

Spring Boot usa Logback como implementación de SLF4J. La configuración está en `src/main/resources/logback-spring.xml`.

| Destino | Archivo | Contenido |
|---|---|---|
| `CONSOLA` | — | Todo, con colores |
| `ARCHIVO` (asíncrono) | `logs/tecnicoya-api.log` | Log general. Rota cada día o a los 10 MB, guarda 30 días comprimidos |
| `AUDITORIA` | `logs/auditoria.log` | Solo eventos de seguridad. Rota cada día y guarda 90 días |

- **Niveles:** `INFO` por defecto; con el perfil `dev` (`SPRING_PROFILES_ACTIVE=dev`) pasa a `DEBUG` y muestra las consultas SQL.
- **Usuario en cada línea:** `JwtInterceptor` guarda `id:rol` del usuario en el MDC y el patrón lo muestra como `[5:ADMINISTRADOR]`.
- **Eventos de negocio:** `UsuarioServiceImpl` (altas, bajas, cambios de contraseña) y `ReporteServiceImpl` usan `@Slf4j`.
- **Eventos de seguridad** (`util/Auditoria`): `LOGIN_EXITOSO`, `LOGIN_FALLIDO`, `LOGIN_BLOQUEADO`, `TOKEN_RECHAZADO`, `ACCESO_DENEGADO`, `REPORTE_DESCARGADO` y cada `OPERACION` `POST`/`PUT`/`PATCH`/`DELETE` con usuario, resultado y duración.
- **Inyección en logs:** el patrón reemplaza los saltos de línea del mensaje (`%replace`), para que nadie pueda falsificar líneas escribiendo saltos de línea en un campo.

Ejemplo de `logs/auditoria.log`:

```
2026-11-04 20:30:21.974 WARN  [anonimo] LOGIN_BLOQUEADO correo=jose.torres@correo.example ip=0:0:0:0:0:0:0:1
2026-11-04 20:30:31.746 INFO  [anonimo] LOGIN_EXITOSO usuario=1 rol=ADMINISTRADOR ip=0:0:0:0:0:0:0:1
2026-11-04 20:30:32.258 INFO  [1:ADMINISTRADOR] OPERACION POST /api/disponibilidades -> 409 usuario=1 rol=ADMINISTRADOR (47 ms)
```

---

## Patrón DAO

La capa de acceso a datos está en el paquete `dao/` y sigue el patrón **DAO (Data Access Object)**: los servicios dependen de **interfaces** y no saben qué tecnología guarda los datos.

```
dao/
├── IGenericDao<T, ID>        listar, buscarPorId, guardar, eliminar, contar
├── IUsuarioDao               + buscarPorCorreo, existeCorreo, existeCorreoEnOtroUsuario
├── ITecnicoDao, IServicioDao, ICalificacionDao, IDisponibilidadDao, IEvidenciaDao,
│   IEvidenciaPagoDao, IExperienciaLaboralDao, IPlanSuscripcionDao, ISuscripcionDao,
│   IPagoSuscripcionDao       una interfaz por entidad, con sus consultas propias
├── IEstadisticasDao          consultas agregadas (COUNT, SUM, AVG) para los reportes
└── impl/
    ├── GenericDaoImpl<T, ID> CRUD común con EntityManager
    └── XxxDaoImpl            @Repository; extiende GenericDaoImpl e implementa IXxxDao
```

- `GenericDaoImpl` implementa el CRUD una sola vez: `persist` si la entidad es nueva, `merge` si ya existe, y `remove` + `flush` al eliminar para detectar en el momento si otros registros dependen de ella.
- Cada `XxxDaoImpl` redefine `consultaListar()` con `JOIN FETCH` para traer las relaciones en una sola consulta (evita el problema N+1).
- `@Repository` hace que Spring traduzca los errores de Hibernate a `DataIntegrityViolationException`, que la API responde como `409`.
- Ejemplo de uso en un servicio: `usuarioDao.buscarPorCorreo(correo)`, `servicioDao.guardar(servicio)`.

---

## Seguridad en la aplicación

| Riesgo (OWASP) | Medida | Dónde |
|---|---|---|
| **Inyección SQL** | Todas las consultas JPQL usan parámetros nombrados (`:correo`); ningún valor del usuario se concatena en una consulta | `dao/impl/*` |
| **XSS** (scripts maliciosos) | `@SinHtml` rechaza etiquetas HTML y `javascript:` en todos los textos libres; la web aplica la misma regla y React escapa todo lo que muestra. Las respuestas JSON llevan `Content-Security-Policy: default-src 'none'` | `validation/SinHtml`, DTO de `dto/request`, web `shared/lib/validacion.ts` |
| **Broken Authentication** (fuerza bruta) | 5 intentos fallidos por correo + IP bloquean el login 15 minutos (`429` con `Retry-After`) | `security/LimiteIntentosLogin`, `controller/AuthController` |
| **Broken Authentication** (contraseñas) | Contraseñas cifradas con BCrypt; política de 8 a 72 caracteres con letras y números; mismo mensaje para correo inexistente y contraseña errónea | `config/PasswordConfig`, `UsuarioRequest`, `UsuarioServiceImpl` |
| **Accesos no autorizados** | Token JWT firmado (HMAC-SHA256) y con vencimiento en todas las rutas privadas (`401`) | `security/JwtService`, `security/JwtInterceptor` |
| **Mínimo privilegio** | `@RequiereRol` restringe endpoints por rol (`403`): reportes, lista de usuarios, planes, validación de pagos y eliminaciones son solo para `ADMINISTRADOR` | `security/RequiereRol`, controladores |
| **Broken Access Control / IDOR** (A01) | `ControlAcceso` verifica que el registro pertenezca al usuario del token: nadie edita datos ajenos, se registra como `ADMINISTRADOR`, se autoverifica ni activa un plan sin pagar (`403`) | `security/ControlAcceso`, `service/impl/*` |
| **Sensitive Data Exposure** | Las respuestas y los reportes nunca incluyen la contraseña; los logs nunca registran contraseñas ni tokens; `Cache-Control: no-store` en las respuestas JSON | `dto/response/UsuarioResponse`, `util/Auditoria`, `CabecerasSeguridadFilter` |
| **Security Misconfiguration** | Errores sin trazas ni detalles internos (`server.error.include-stacktrace=never`); CORS limitado al front-end; cabeceras `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy` y HSTS si hay HTTPS; secretos en `.env` | `application.properties`, `config/CorsConfig`, `security/CabecerasSeguridadFilter` |
| **Archivos maliciosos** | Solo PDF/JPG/PNG/WEBP de hasta 5 MB; el nombre en disco es su SHA-256, por lo que no se puede escribir fuera de la carpeta | `ArchivoServiceImpl` |
| **Manejar excepciones** | `GlobalExceptionHandler` convierte cualquier error en un JSON con mensaje en español y registra en el log los errores inesperados | `exception/GlobalExceptionHandler` |
| **Registrar eventos importantes** | Log de auditoría con logins, bloqueos, accesos denegados y operaciones | `util/Auditoria`, `security/AuditoriaInterceptor` |
| **Inyección en logs** (CRLF, CWE-117) | Los datos del cliente pasan por `Auditoria.limpiar()` antes de registrarse y el patrón de Logback reemplaza los saltos de línea | `util/Auditoria`, `logback-spring.xml` |
| **Dependencias vulnerables** | Plugin **OWASP Dependency-Check**: `.\mvnw.cmd dependency-check:check` analiza las librerías contra la base de CVE y falla si alguna tiene severidad alta (CVSS ≥ 7) | `pom.xml` |
| **Usar HTTPS** | Configuración de SSL lista en `application.properties` (comentada); en producción se activa con un certificado o se publica detrás de un proxy HTTPS | `application.properties` |

### Herramientas recomendadas

| Herramienta | Función | Estado en el proyecto |
|---|---|---|
| OWASP Dependency-Check | Vulnerabilidades en dependencias | Configurado en `pom.xml` |
| SpotBugs + FindSecBugs | Análisis estático de seguridad (SAST) | Configurado en `pom.xml`: `.\mvnw.cmd compile spotbugs:check` |
| OWASP ZAP | Análisis dinámico (DAST) | Se usa como proxy con la colección de Postman (ver abajo) |
| SonarQube | Calidad y seguridad del código | No configurado; con un servidor SonarQube se ejecuta con `.\mvnw.cmd verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.host.url=... -Dsonar.token=...` |
| Snyk | Escaneo de dependencias | Compatible: `snyk test` lee el `pom.xml` |
| Checkmarx | Análisis estático | Herramienta comercial; no configurada |

### Cómo probar las medidas

| Prueba | Cómo |
|---|---|
| Pruebas automáticas | `.\mvnw.cmd test`: incluye `ControlAccesoTest` (IDOR), `LimiteIntentosLoginTest`, `AuthControllerTest` (429), `ReporteControllerTest` (403 y cabeceras), `SinHtmlValidatorTest`, `ReporteServiceImplTest` (abre el Excel generado con POI) y casos de acceso denegado en `UsuarioServiceImplTest` y `ServicioServiceImplTest` |
| Postman | Carpetas **12 - Errores generales** (XSS y cabeceras), **13 - Reportes Excel** (descarga y 403) y **14 - Pruebas de penetración** (18 ataques) |
| Fuerza bruta a mano | Inicie sesión 6 veces con una contraseña errónea: la sexta responde `429` |

---

## Pruebas de seguridad: SAST, DAST y penetración

| Prueba | Qué analiza | Herramienta | Cómo se ejecuta | Resultado |
|---|---|---|---|---|
| **SAST** (estática) | El código compilado, sin ejecutarlo | SpotBugs + FindSecBugs | `.\mvnw.cmd compile spotbugs:check` | 108 hallazgos iniciales; los reales se corrigieron y los falsos positivos quedaron justificados en `spotbugs-exclude.xml`. Hoy: **0 pendientes** (`BUILD SUCCESS`) |
| **DAST** (dinámica) | La API corriendo, desde afuera, como un atacante | OWASP ZAP | ZAP como proxy (`127.0.0.1:8090`), se ejecuta la colección de Postman a través de él y luego **Escaneo activo** sobre `http://localhost:8080` | Reporte HTML de ZAP con las alertas por nivel de riesgo |
| **Prueba de penetración** | Ataques dirigidos a la lógica de la aplicación (OWASP Top 10) | Postman, carpeta **14 - Pruebas de penetración** | `postman collection run postman/TecnicoYa-API.postman_collection.json -i "14 - Pruebas de penetración"` | 18 ataques, 36 validaciones: **todos bloqueados** |

### SAST: hallazgos de SpotBugs + FindSecBugs

| Hallazgo | Cantidad | Decisión |
|---|---|---|
| `CRLF_INJECTION_LOGS`: datos del cliente escritos en el log | 14 | **Corregido**: `Auditoria.limpiar()` escapa `\r` y `\n` en correo, IP, ruta, método y rol. Los 8 que quedan registran valores `Long`/`enum` o constantes internas: falsos positivos |
| `EI_EXPOSE_REP` / `EI_EXPOSE_REP2`: se guarda o devuelve un objeto mutable | 25 | **Corregido** en `CorsConfig` (copia del arreglo) y `ApiErrorResponse` (mapa inmutable). En las entidades JPA es necesario para Hibernate y nunca salen de la API (se responde con DTO): falso positivo |
| `CT_CONSTRUCTOR_THROW`: constructor que lanza excepción (ataque de finalizador) | 2 | **Corregido**: `JwtService` y `ArchivoServiceImpl` son `final` |
| `SE_TRANSIENT_FIELD_NOT_RESTORED` | 1 | **Corregido** en `DemasiadosIntentosException` |
| `SERVLET_HEADER`: se lee la cabecera `Authorization` | 2 | Falso positivo: el token se valida con su firma antes de usarlo |
| `WEAK_FILENAMEUTILS`: nombre de archivo sin revisar bytes nulos | 2 | Falso positivo: Commons IO rechaza los bytes nulos y el archivo se guarda con su SHA-256 como nombre |
| `SPRING_ENDPOINT`: aviso informativo por cada endpoint | 62 | Revisados: todos pasan por `JwtInterceptor`, `@RequiereRol` y `ControlAcceso` |

### Prueba de penetración: ataques de la carpeta 14

| Ataque | Defensa | Respuesta |
|---|---|---|
| Registrarse como `ADMINISTRADOR` sin token; un cliente que se asciende a `ADMINISTRADOR` | `ControlAcceso` en `UsuarioServiceImpl` | `403` |
| IDOR: leer, editar o eliminar la cuenta de otro usuario; listar todos los usuarios | `ControlAcceso` y `@RequiereRol` | `403` |
| Solicitar un servicio a nombre de otro cliente; crear un plan siendo cliente | `ControlAcceso` y `@RequiereRol` | `403` |
| Técnico que edita el perfil o la agenda de otro técnico, o activa un plan sin pagar | `ControlAcceso` | `403` |
| Token JWT falsificado (`alg: none`, rol `ADMINISTRADOR`) | Firma HMAC-SHA256 en `JwtService` | `401` |
| Inyección SQL en el login y en un id de la URL | Consultas JPQL con parámetros y conversión del id a `Long` | `400`/`401`, sin detalles internos |
| Path traversal para leer `application.properties` | Tomcat y `ArchivoServiceImpl` (solo nombres SHA-256) | `400` |
| XSS almacenado en un servicio | `@SinHtml` | `400` |
| Fuerza bruta: 6 contraseñas erróneas seguidas | `LimiteIntentosLogin` | `429` con `Retry-After` |

