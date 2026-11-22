package com.tecnicoya.api.monitoreo;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/** Cada petición recibe un id que va al log (MDC) y a la cabecera X-Request-Id de la respuesta. */
class IdPeticionFilterTest {

    private final IdPeticionFilter filtro = new IdPeticionFilter();

    @Test
    void sinCabecera_generaUnIdYLoDevuelve() throws Exception {
        AtomicReference<String> idEnElLog = new AtomicReference<>();
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        filtro.doFilter(new MockHttpServletRequest("GET", "/api/servicios"), respuesta,
                (req, res) -> idEnElLog.set(MDC.get(IdPeticionFilter.MDC_ID_PETICION)));

        assertThat(idEnElLog.get()).matches("[0-9a-f-]{36}");
        assertThat(respuesta.getHeader(IdPeticionFilter.CABECERA)).isEqualTo(idEnElLog.get());
        assertThat(MDC.get(IdPeticionFilter.MDC_ID_PETICION)).as("se limpia al terminar").isNull();
    }

    @Test
    void cabeceraConFormatoSeguro_seReutiliza() {
        assertThat(IdPeticionFilter.idValido("front-1234abcd")).isEqualTo("front-1234abcd");
    }

    @Test
    void cabeceraConTextoPeligroso_seReemplaza() {
        String id = IdPeticionFilter.idValido("abc\r\nLOGIN_EXITOSO usuario=1");

        assertThat(id).doesNotContain("LOGIN").matches("[0-9a-f-]{36}");
    }
}
