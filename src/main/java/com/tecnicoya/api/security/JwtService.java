package com.tecnicoya.api.security;

import com.tecnicoya.api.dto.response.UsuarioResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Genera y valida los tokens JWT de la API.
 *
 * <p>El token guarda el id del usuario ({@code sub}), su correo, su rol y la fecha de vencimiento, y se firma con
 * HMAC-SHA256 usando la clave {@code tecnicoya.jwt.secreto}: si alguien cambia su contenido, la firma deja de
 * coincidir y el token se rechaza.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final Duration validez;

    /**
     * @param secreto      clave de firma; debe tener al menos 32 caracteres (256 bits)
     * @param horasValidez horas que dura el token desde que se emite
     */
    public JwtService(@Value("${tecnicoya.jwt.secreto}") String secreto,
                      @Value("${tecnicoya.jwt.horas-validez}") long horasValidez) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.validez = Duration.ofHours(horasValidez);
    }

    /** Crea el token que el usuario enviará en la cabecera {@code Authorization} de las demás peticiones. */
    public String generarToken(UsuarioResponse usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.idUsuario().toString())
                .claim("correo", usuario.correo())
                .claim("rol", usuario.tipoUsuario().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(validez)))
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Comprueba la firma y la fecha de vencimiento del token.
     *
     * @return los datos guardados en el token
     * @throws JwtException si el token está mal formado, fue modificado o ya venció
     */
    public Claims validarToken(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
