package com.tecnicoya.api.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SinHtmlValidatorTest {
    private final SinHtmlValidator validador = new SinHtmlValidator();

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "Fuga de agua en el baño", "La temperatura baja a < 5 grados", "Cotización: 3 > 2",
            "Correo: soporte@tecnicoya.example"})
    void aceptaTextoNormal(String texto) {
        assertThat(validador.isValid(texto, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"<script>alert(1)</script>", "Hola <b>mundo</b>", "< img src=x onerror=alert(1)>",
            "</div>", "<!-- comentario -->", "javascript:alert(1)", "JavaScript : alert(1)",
            "texto on error = alert(1)", "data:text/html;base64,PHNjcmlwdD4="})
    void rechazaHtmlYJavascript(String texto) {
        assertThat(validador.isValid(texto, null)).isFalse();
    }
}
