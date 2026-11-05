package com.tecnicoya.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rechaza textos que contienen etiquetas HTML o código JavaScript, como {@code <script>}, {@code <img onerror=...>}
 * o {@code javascript:}. Es una defensa contra XSS (Cross-Site Scripting) almacenado: impide que un usuario guarde
 * código que luego se ejecutaría en el navegador de otro.
 *
 * <p>Los valores {@code null} se consideran válidos (para eso está {@code @NotBlank}). Un signo {@code <} suelto,
 * como en "temperatura &lt; 5", sí se permite.
 */
@Documented
@Constraint(validatedBy = SinHtmlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface SinHtml {

    String message() default "No se permiten etiquetas HTML ni código JavaScript";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
