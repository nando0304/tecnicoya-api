package com.tecnicoya.api.validation;

import com.google.common.collect.ImmutableList;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/** Validador de {@link SinHtml}. */
public class SinHtmlValidator implements ConstraintValidator<SinHtml, String> {

    /** Apertura de una etiqueta: {@code <} seguido de una letra, {@code /}, {@code !} o {@code ?}. */
    private static final Pattern ETIQUETA_HTML = Pattern.compile("<\\s*[a-zA-Z/!?]");

    /** Esquemas y atributos con los que se ejecuta JavaScript aun sin etiquetas. */
    private static final ImmutableList<String> PATRONES_PELIGROSOS =
            ImmutableList.of("javascript:", "vbscript:", "data:text/html", "onerror=", "onload=");

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        if (valor == null) {
            return true;
        }
        // Se quitan los espacios para detectar también variantes como "on error =" o "java script:"
        String compacto = StringUtils.deleteWhitespace(valor);
        return !ETIQUETA_HTML.matcher(valor).find()
                && PATRONES_PELIGROSOS.stream().noneMatch(patron -> StringUtils.containsIgnoreCase(compacto, patron));
    }
}
