package com.tecnicoya.api.security;

import com.tecnicoya.api.exception.AccesoDenegadoException;
import com.tecnicoya.api.model.enums.TipoUsuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ControlAccesoTest {
    private final ControlAcceso controlAcceso = new ControlAcceso();

    @AfterEach
    void limpiar() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void propietario_puedeModificarSuRegistro() {
        sesion(2L, TipoUsuario.CLIENTE);

        assertThatCode(() -> controlAcceso.exigirPropietario("editar", 2L)).doesNotThrowAnyException();
    }

    @Test
    void otroUsuario_recibeAccesoDenegado() {
        sesion(3L, TipoUsuario.CLIENTE);

        assertThatThrownBy(() -> controlAcceso.exigirPropietario("modificar los datos de otro usuario", 2L))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessage("No tiene permiso para modificar los datos de otro usuario");
    }

    @Test
    void conVariosDuenos_bastaConSerUnoDeEllos() {
        sesion(5L, TipoUsuario.TECNICO);

        assertThatCode(() -> controlAcceso.exigirPropietario("editar", 2L, null, 5L)).doesNotThrowAnyException();
    }

    @Test
    void administrador_puedeModificarCualquierRegistro() {
        sesion(1L, TipoUsuario.ADMINISTRADOR);

        assertThatCode(() -> controlAcceso.exigirPropietario("editar", 2L)).doesNotThrowAnyException();
        assertThatCode(() -> controlAcceso.exigirAdministrador("crear un plan")).doesNotThrowAnyException();
    }

    @Test
    void noAdministrador_noPasaExigirAdministrador() {
        sesion(5L, TipoUsuario.TECNICO);

        assertThatThrownBy(() -> controlAcceso.exigirAdministrador("verificar su propio perfil"))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void sinToken_noEsDuenoDeNadaNiAdministrador() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        assertThatThrownBy(() -> controlAcceso.exigirPropietario("editar", 2L))
                .isInstanceOf(AccesoDenegadoException.class);
        assertThatThrownBy(() -> controlAcceso.exigirAdministrador("registrar un usuario ADMINISTRADOR"))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    private static void sesion(Long usuarioId, TipoUsuario rol) {
        MockHttpServletRequest peticion = new MockHttpServletRequest();
        peticion.setAttribute(JwtInterceptor.ATRIBUTO_USUARIO, usuarioId);
        peticion.setAttribute(JwtInterceptor.ATRIBUTO_ROL, rol);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(peticion));
    }
}
