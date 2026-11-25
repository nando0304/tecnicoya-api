package com.tecnicoya.api.security;

import com.tecnicoya.api.dto.response.UsuarioResponse;
import com.tecnicoya.api.exception.AccesoDenegadoException;
import com.tecnicoya.api.exception.NoAutenticadoException;
import com.tecnicoya.api.model.enums.EstadoUsuario;
import com.tecnicoya.api.model.enums.TipoUsuario;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MonitoreoSeguridadFilterTest {
    private static final String TOKEN_PROMETHEUS = "token-de-prometheus-para-pruebas";
    private static final UsuarioResponse ADMINISTRADOR = new UsuarioResponse(1L, "Ana", "Torres",
            "admin@correo.example", null, TipoUsuario.ADMINISTRADOR, EstadoUsuario.ACTIVO, LocalDateTime.now());

    private final JwtService jwtService = new JwtService(JwtServiceTest.SECRETO, 8);

    private final HandlerExceptionResolver resolver = (request, response, handler, ex) -> {
        response.setStatus(ex instanceof NoAutenticadoException ? 401 : ex instanceof AccesoDenegadoException ? 403 : 500);
        return new ModelAndView();
    };
    private final MonitoreoSeguridadFilter filtro = new MonitoreoSeguridadFilter(jwtService, resolver, TOKEN_PROMETHEUS);

    @Test
    void health_esPublicoPeroSinDetalle() throws Exception {
        MockFilterChain cadena = ejecutar(peticion("GET", "/actuator/health", null));

        assertThat(cadena.getRequest()).as("la petición llegó a Actuator").isNotNull();
        HttpServletRequest recibida = (HttpServletRequest) cadena.getRequest();
        assertThat(recibida.getUserPrincipal()).isNull();
    }

    @Test
    void metricas_sinToken_devuelve401() throws Exception {
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        MockFilterChain cadena = new MockFilterChain();

        filtro.doFilter(peticion("GET", "/actuator/metrics", null), respuesta, cadena);

        assertThat(respuesta.getStatus()).isEqualTo(401);
        assertThat(cadena.getRequest()).as("no llegó a Actuator").isNull();
    }

    @Test
    void metricas_conTokenDeCliente_devuelve403() throws Exception {
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        MockFilterChain cadena = new MockFilterChain();

        filtro.doFilter(peticion("GET", "/actuator/metrics", jwtService.generarToken(JwtServiceTest.USUARIO)),
                respuesta, cadena);

        assertThat(respuesta.getStatus()).isEqualTo(403);
        assertThat(cadena.getRequest()).isNull();
    }

    @Test
    void metricas_conTokenDeAdministrador_pasaComoAdministrador() throws Exception {
        MockFilterChain cadena = ejecutar(peticion("GET", "/actuator/metrics",
                jwtService.generarToken(ADMINISTRADOR)));

        HttpServletRequest recibida = (HttpServletRequest) cadena.getRequest();
        assertThat(recibida.getUserPrincipal().getName()).isEqualTo("1");

        assertThat(recibida.isUserInRole("ADMINISTRADOR")).isTrue();
    }

    @Test
    void tokenDePrometheus_puedeLeerPeroNoModificar() throws Exception {
        MockFilterChain lectura = ejecutar(peticion("GET", "/actuator/prometheus", TOKEN_PROMETHEUS));
        assertThat(lectura.getRequest()).isNotNull();

        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        MockFilterChain cambioDeNivel = new MockFilterChain();
        filtro.doFilter(peticion("POST", "/actuator/loggers/com.tecnicoya.api", TOKEN_PROMETHEUS), respuesta,
                cambioDeNivel);
        assertThat(respuesta.getStatus()).isEqualTo(403);
        assertThat(cambioDeNivel.getRequest()).isNull();
    }

    @Test
    void rutasDeLaApi_noPasanPorEsteFiltro() throws Exception {
        MockFilterChain cadena = ejecutar(peticion("GET", "/api/servicios", null));

        assertThat(cadena.getRequest()).isNotNull();
    }

    private MockFilterChain ejecutar(MockHttpServletRequest peticion) throws Exception {
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        MockFilterChain cadena = new MockFilterChain();
        filtro.doFilter(peticion, respuesta, cadena);
        assertThat(respuesta.getStatus()).isEqualTo(200);
        return cadena;
    }

    private static MockHttpServletRequest peticion(String metodo, String ruta, String token) {
        MockHttpServletRequest peticion = new MockHttpServletRequest(metodo, ruta);
        if (token != null) {
            peticion.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return peticion;
    }
}
