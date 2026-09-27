package com.tecnicoya.api.exception;

import com.tecnicoya.api.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Convierte cualquier excepción en un {@link ApiErrorResponse} con mensaje en español. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final int MYSQL_REGISTRO_DUPLICADO = 1062;
    private static final int MYSQL_REGISTRO_REFERENCIADO = 1451;
    private static final int MYSQL_REFERENCIA_INEXISTENTE = 1452;

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> manejarNoEncontrado(RecursoNoEncontradoException ex,
                                                                HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ApiErrorResponse> manejarConflicto(ConflictoException ex, HttpServletRequest request) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiErrorResponse> manejarReglaNegocio(ReglaNegocioException ex,
                                                                HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiErrorResponse> manejarCredencialesInvalidas(CredencialesInvalidasException ex,
                                                                         HttpServletRequest request) {
        return construir(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> manejarValidacion(MethodArgumentNotValidException ex,
                                                              HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errores.merge(
                error.getField(),
                Objects.requireNonNullElse(error.getDefaultMessage(), "Valor inválido"),
                (actual, nuevo) -> actual + "; " + nuevo));
        return construir(HttpStatus.BAD_REQUEST, "La solicitud contiene datos inválidos", request, errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> manejarJsonIlegible(HttpMessageNotReadableException ex,
                                                                HttpServletRequest request) {
        String mensaje = "El cuerpo de la solicitud no es un JSON válido o tiene tipos de dato incorrectos";
        if (ex.getCause() instanceof InvalidFormatException formato) {
            String campo = formato.getPath().stream()
                    .map(JacksonException.Reference::getPropertyName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("."));
            Class<?> tipo = formato.getTargetType();
            if (tipo != null && tipo.isEnum()) {
                mensaje = String.format("Valor '%s' no válido para el campo '%s'. Valores permitidos: %s",
                        formato.getValue(), campo, Arrays.toString(tipo.getEnumConstants()));
            } else {
                mensaje = String.format("Valor '%s' no válido para el campo '%s'", formato.getValue(), campo);
            }
        }
        return construir(HttpStatus.BAD_REQUEST, mensaje, request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> manejarTipoParametro(MethodArgumentTypeMismatchException ex,
                                                                 HttpServletRequest request) {
        String mensaje = String.format("El valor '%s' no es válido para el parámetro '%s'", ex.getValue(), ex.getName());
        return construir(HttpStatus.BAD_REQUEST, mensaje, request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> manejarMetodoNoPermitido(HttpRequestMethodNotSupportedException ex,
                                                                     HttpServletRequest request) {
        String mensaje = String.format("El método %s no está permitido en esta ruta", ex.getMethod());
        return construir(HttpStatus.METHOD_NOT_ALLOWED, mensaje, request, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> manejarTipoContenido(HttpMediaTypeNotSupportedException ex,
                                                                 HttpServletRequest request) {
        return construir(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Tipo de contenido no soportado; envíe el cuerpo con 'Content-Type: application/json'", request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> manejarRutaInexistente(NoResourceFoundException ex,
                                                                   HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, "La ruta solicitada no existe", request, null);
    }

    /** Respaldo para restricciones de la BD (FK, UNIQUE) que no se hayan validado antes en el servicio. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> manejarIntegridad(DataIntegrityViolationException ex,
                                                              HttpServletRequest request) {
        String mensaje = switch (codigoErrorSql(ex)) {
            case MYSQL_REGISTRO_REFERENCIADO ->
                    "No se puede eliminar o modificar el registro porque tiene información relacionada "
                            + "(por ejemplo servicios, pagos o suscripciones)";
            case MYSQL_REFERENCIA_INEXISTENTE -> "El registro hace referencia a otro que no existe";
            case MYSQL_REGISTRO_DUPLICADO -> "Ya existe un registro con esos datos únicos";
            default -> "La operación viola una restricción de integridad de la base de datos";
        };
        log.warn("Violación de integridad en {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return construir(HttpStatus.CONFLICT, mensaje, request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> manejarGeneral(Exception ex, HttpServletRequest request) {
        // Otras excepciones propias de Spring MVC ya traen su código HTTP (406, 400, etc.)
        if (ex instanceof ErrorResponse errorSpring) {
            HttpStatus status = Objects.requireNonNullElse(
                    HttpStatus.resolve(errorSpring.getStatusCode().value()), HttpStatus.BAD_REQUEST);
            return construir(status, "La solicitud no pudo ser procesada", request, null);
        }
        log.error("Error no controlado en {} {}", request.getMethod(), request.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado en el servidor. Intente nuevamente más tarde.", request, null);
    }

    private static int codigoErrorSql(Throwable ex) {
        for (Throwable causa = ex; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql) {
                return sql.getErrorCode();
            }
        }
        return -1;
    }

    private static ResponseEntity<ApiErrorResponse> construir(HttpStatus status, String mensaje,
                                                              HttpServletRequest request,
                                                              Map<String, String> errores) {
        ApiErrorResponse cuerpo = new ApiErrorResponse(LocalDateTime.now(), status.value(), nombreEstado(status),
                mensaje, request.getRequestURI(), errores);
        return ResponseEntity.status(status).body(cuerpo);
    }

    private static String nombreEstado(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Solicitud incorrecta";
            case NOT_FOUND -> "No encontrado";
            case METHOD_NOT_ALLOWED -> "Método no permitido";
            case NOT_ACCEPTABLE -> "No aceptable";
            case CONFLICT -> "Conflicto";
            case UNSUPPORTED_MEDIA_TYPE -> "Tipo de contenido no soportado";
            case INTERNAL_SERVER_ERROR -> "Error interno del servidor";
            default -> status.getReasonPhrase();
        };
    }
}
