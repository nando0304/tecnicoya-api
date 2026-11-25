package com.tecnicoya.api.validation;

import com.google.common.collect.ImmutableList;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class SinHtmlValidator implements ConstraintValidator<SinHtml, String> {
    private static final Pattern ETIQUETA_HTML = Pattern.compile("<\\s*[a-zA-Z/!?]");

    private static final ImmutableList<String> PATRONES_PELIGROSOS =
            ImmutableList.of("javascript:", "vbscript:", "data:text/html", "onerror=", "onload=");

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        if (valor == null) {
            return true;
        }

        String compacto = StringUtils.deleteWhitespace(valor);
        return !ETIQUETA_HTML.matcher(valor).find()
                && PATRONES_PELIGROSOS.stream().noneMatch(patron -> StringUtils.containsIgnoreCase(compacto, patron));
    }
}
