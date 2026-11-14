package com.tecnicoya.api.security;

import com.tecnicoya.api.exception.AccesoDenegadoException;
import com.tecnicoya.api.model.enums.TipoUsuario;
import com.tecnicoya.api.util.Auditoria;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Arrays;
import java.util.Objects;

/**
 * Control de acceso a nivel de registro: evita que un usuario modifique datos que no le pertenecen
 * (OWASP Top 10 A01 - Broken Access Control, conocido como IDOR) o que se dé a sí mismo privilegios que no tiene.
 *
 * <p>{@link RequiereRol} decide qué <b>roles</b> pueden usar un endpoint. Esta clase decide si el usuario
 * puede tocar <b>un registro en particular</b>: por ejemplo, un cliente puede editar sus propios servicios,
 * pero no los de otro cliente. El administrador siempre tiene acceso.
 *
 * <p>Lee el usuario del token desde los atributos de la petición que deja {@link JwtInterceptor}.
 * Si la petición no trae token (por ejemplo, el registro público de una cuenta), no hay usuario y solo
 * se permite lo que no exige permisos especiales. Cada acceso denegado queda en el log de auditoría.
 */
@Component
public class ControlAcceso {

    /**
     * Permite continuar solo si el usuario autenticado es administrador; si no, responde 403.
     *
     * @param accion qué se intentó hacer, para el mensaje de error y la auditoría
     */
    public void exigirAdministrador(String accion) {
        if (!esAdministrador()) {
            denegar(accion);
        }
    }

    /**
     * Permite continuar solo si el usuario autenticado es uno de los dueños indicados o es administrador.
     *
     * @param accion      qué se intentó hacer, para el mensaje de error y la auditoría
     * @param idsUsuarios ids de los usuarios dueños del registro (los {@code null} se ignoran)
     */
    public void exigirPropietario(String accion, Long... idsUsuarios) {
        if (esAdministrador()) {
            return;
        }
        Long actual = usuarioActual();
        if (actual == null || Arrays.stream(idsUsuarios).filter(Objects::nonNull).noneMatch(actual::equals)) {
            denegar(accion);
        }
    }

    /** @return {@code true} si la petición trae un token válido de un usuario ADMINISTRADOR */
    public boolean esAdministrador() {
        return leer(JwtInterceptor.ATRIBUTO_ROL) == TipoUsuario.ADMINISTRADOR;
    }

    /** @return id del usuario del token, o {@code null} si la petición no trae token */
    public Long usuarioActual() {
        return leer(JwtInterceptor.ATRIBUTO_USUARIO) instanceof Long id ? id : null;
    }

    private static Object leer(String atributo) {
        RequestAttributes peticion = RequestContextHolder.getRequestAttributes();
        return peticion == null ? null : peticion.getAttribute(atributo, RequestAttributes.SCOPE_REQUEST);
    }

    private void denegar(String accion) {
        Auditoria.LOG.warn("ACCESO_DENEGADO usuario={} rol={} accion=\"{}\"", Auditoria.limpiar(usuarioActual()),
                Auditoria.limpiar(leer(JwtInterceptor.ATRIBUTO_ROL)), Auditoria.limpiar(accion));
        throw new AccesoDenegadoException("No tiene permiso para " + accion);
    }
}
