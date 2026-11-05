package com.tecnicoya.api.security;

import com.tecnicoya.api.model.enums.TipoUsuario;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restringe un controlador o un endpoint a ciertos roles (principio de mínimo privilegio).
 *
 * <p>{@link JwtInterceptor} lee el rol guardado en el token y, si no está entre los permitidos, responde 403.
 * Si se anota la clase y también el método, manda la anotación del método.
 *
 * <pre>{@code
 * @RequiereRol(TipoUsuario.ADMINISTRADOR)
 * @GetMapping("/usuarios")
 * public ResponseEntity<byte[]> exportarUsuarios() { ... }
 * }</pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequiereRol {

    /** Roles que pueden usar el recurso. */
    TipoUsuario[] value();
}
