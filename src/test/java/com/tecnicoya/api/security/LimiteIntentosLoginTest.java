package com.tecnicoya.api.security;

import com.google.common.base.Ticker;
import com.tecnicoya.api.exception.DemasiadosIntentosException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Prueba el bloqueo por fuerza bruta con un reloj falso, sin esperar los minutos reales. */
class LimiteIntentosLoginTest {

    private static final String CLAVE = LimiteIntentosLogin.clave(" Maria@Correo.example ", "10.0.0.1");

    private final RelojFalso reloj = new RelojFalso();
    private final LimiteIntentosLogin limite = new LimiteIntentosLogin(3, Duration.ofMinutes(15), reloj);

    @Test
    void clave_normalizaElCorreoEIncluyeLaIp() {
        assertThat(CLAVE).isEqualTo("maria@correo.example|10.0.0.1");
    }

    @Test
    void bloquea_alAlcanzarElMaximoDeFallos() {
        limite.registrarFallo(CLAVE);
        limite.registrarFallo(CLAVE);
        assertThatCode(() -> limite.verificar(CLAVE)).doesNotThrowAnyException();

        assertThat(limite.registrarFallo(CLAVE)).isEqualTo(3);
        assertThatThrownBy(() -> limite.verificar(CLAVE))
                .isInstanceOf(DemasiadosIntentosException.class)
                .hasMessageContaining("15 minutos");
    }

    @Test
    void desbloquea_cuandoPasaElTiempoDesdeElUltimoFallo() {
        for (int i = 0; i < 3; i++) {
            limite.registrarFallo(CLAVE);
        }
        reloj.avanzar(Duration.ofMinutes(14));
        assertThatThrownBy(() -> limite.verificar(CLAVE)).isInstanceOf(DemasiadosIntentosException.class);

        reloj.avanzar(Duration.ofMinutes(2));
        assertThatCode(() -> limite.verificar(CLAVE)).doesNotThrowAnyException();
    }

    @Test
    void loginCorrecto_reiniciaElContador() {
        limite.registrarFallo(CLAVE);
        limite.registrarFallo(CLAVE);
        limite.reiniciar(CLAVE);

        assertThat(limite.registrarFallo(CLAVE)).isEqualTo(1);
    }

    @Test
    void otraIp_tieneSuPropioContador() {
        for (int i = 0; i < 3; i++) {
            limite.registrarFallo(CLAVE);
        }
        String otraIp = LimiteIntentosLogin.clave("maria@correo.example", "10.0.0.2");

        assertThatCode(() -> limite.verificar(otraIp)).doesNotThrowAnyException();
    }

    /** {@link Ticker} de Guava controlado por la prueba. */
    private static final class RelojFalso extends Ticker {
        private final AtomicLong nanos = new AtomicLong();

        @Override
        public long read() {
            return nanos.get();
        }

        void avanzar(Duration tiempo) {
            nanos.addAndGet(tiempo.toNanos());
        }
    }
}
