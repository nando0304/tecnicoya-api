package com.tecnicoya.api.security;

import com.tecnicoya.api.exception.AccesoDenegadoException;
import com.tecnicoya.api.model.enums.TipoUsuario;
import com.tecnicoya.api.util.Auditoria;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Arrays;
import java.util.Objects;

@Component
public class ControlAcceso {
    public void exigirAdministrador(String accion) {
        if (!esAdministrador()) {
            denegar(accion);
        }
    }

    public void exigirPropietario(String accion, Long... idsUsuarios) {
        if (esAdministrador()) {
            return;
        }
        Long actual = usuarioActual();
        if (actual == null || Arrays.stream(idsUsuarios).filter(Objects::nonNull).noneMatch(actual::equals)) {
            denegar(accion);
        }
    }

    public boolean esAdministrador() {
        return leer(JwtInterceptor.ATRIBUTO_ROL) == TipoUsuario.ADMINISTRADOR;
    }

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
