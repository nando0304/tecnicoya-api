package com.tecnicoya.api.security;

import com.tecnicoya.api.dto.response.UsuarioResponse;
import com.tecnicoya.api.model.enums.EstadoUsuario;
import com.tecnicoya.api.model.enums.TipoUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    static final String SECRETO = "clave-de-prueba-con-mas-de-32-caracteres";
    static final UsuarioResponse USUARIO = new UsuarioResponse(2L, "María", "Quispe", "maria.quispe@correo.example",
            null, TipoUsuario.CLIENTE, EstadoUsuario.ACTIVO, LocalDateTime.now());

    private final JwtService jwtService = new JwtService(SECRETO, 8);

    @Test
    void validarToken_devuelveLosDatosGuardadosAlGenerarlo() {
        Claims datos = jwtService.validarToken(jwtService.generarToken(USUARIO));

        assertThat(datos.getSubject()).isEqualTo("2");
        assertThat(datos.get("correo", String.class)).isEqualTo("maria.quispe@correo.example");
        assertThat(datos.get("rol", String.class)).isEqualTo("CLIENTE");
        assertThat(datos.getExpiration()).isAfter(datos.getIssuedAt());
    }

    @Test
    void validarToken_conDatosCambiados_esRechazado() {
        // Un cliente toma los datos (payload) de un token de administrador y conserva su propia firma
        UsuarioResponse admin = new UsuarioResponse(1L, "Carlos", "Mendoza", "carlos.mendoza@tecnicoya.example",
                null, TipoUsuario.ADMINISTRADOR, EstadoUsuario.ACTIVO, LocalDateTime.now());
        String[] cliente = jwtService.generarToken(USUARIO).split("\\.");
        String[] administrador = jwtService.generarToken(admin).split("\\.");
        String falsificado = cliente[0] + "." + administrador[1] + "." + cliente[2];

        assertThatThrownBy(() -> jwtService.validarToken(falsificado)).isInstanceOf(JwtException.class);
    }

    @Test
    void validarToken_firmadoConOtraClave_esRechazado() {
        String ajeno = new JwtService("otra-clave-distinta-con-mas-de-32-caracteres", 8).generarToken(USUARIO);

        assertThatThrownBy(() -> jwtService.validarToken(ajeno)).isInstanceOf(JwtException.class);
    }

    @Test
    void validarToken_vencido_esRechazado() {
        String vencido = new JwtService(SECRETO, -1).generarToken(USUARIO);

        assertThatThrownBy(() -> jwtService.validarToken(vencido)).isInstanceOf(JwtException.class);
    }

    @Test
    void constructor_claveCorta_noPermiteIniciar() {
        assertThatThrownBy(() -> new JwtService("muy-corta", 8)).isInstanceOf(WeakKeyException.class);
    }
}
