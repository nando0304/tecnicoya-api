package com.tecnicoya.api.security;

import com.tecnicoya.api.dto.response.UsuarioResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public final class JwtService {
    private final SecretKey clave;
    private final Duration validez;

    public JwtService(@Value("${tecnicoya.jwt.secreto}") String secreto,
                      @Value("${tecnicoya.jwt.horas-validez}") long horasValidez) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.validez = Duration.ofHours(horasValidez);
    }

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

    public Claims validarToken(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
