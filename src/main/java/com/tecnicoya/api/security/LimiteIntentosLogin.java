package com.tecnicoya.api.security;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tecnicoya.api.exception.DemasiadosIntentosException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkArgument;

@Component
public class LimiteIntentosLogin {
    private static final long MAXIMO_ENTRADAS = 10_000;

    private final int maxIntentos;
    private final Duration bloqueo;
    private final Cache<String, Integer> intentosFallidos;

    @Autowired
    public LimiteIntentosLogin(@Value("${tecnicoya.login.max-intentos:5}") int maxIntentos,
                               @Value("${tecnicoya.login.minutos-bloqueo:15}") long minutosBloqueo) {
        this(maxIntentos, Duration.ofMinutes(minutosBloqueo), Ticker.systemTicker());
    }

    @VisibleForTesting
    LimiteIntentosLogin(int maxIntentos, Duration bloqueo, Ticker reloj) {
        checkArgument(maxIntentos > 0, "max-intentos debe ser mayor que 0");
        checkArgument(!bloqueo.isNegative() && !bloqueo.isZero(), "minutos-bloqueo debe ser mayor que 0");
        this.maxIntentos = maxIntentos;
        this.bloqueo = bloqueo;
        this.intentosFallidos = CacheBuilder.newBuilder()
                .expireAfterWrite(bloqueo)
                .maximumSize(MAXIMO_ENTRADAS)
                .ticker(reloj)
                .build();
    }

    public static String clave(String correo, String ip) {
        return correo.trim().toLowerCase(Locale.ROOT) + "|" + ip;
    }

    public void verificar(String clave) {
        Integer fallos = intentosFallidos.getIfPresent(clave);
        if (fallos != null && fallos >= maxIntentos) {
            throw new DemasiadosIntentosException(String.format(
                    "Demasiados intentos fallidos de inicio de sesión. Espere %d minutos e inténtelo de nuevo",
                    bloqueo.toMinutes()), bloqueo);
        }
    }

    public int registrarFallo(String clave) {
        return intentosFallidos.asMap().merge(clave, 1, Integer::sum);
    }

    public void reiniciar(String clave) {
        intentosFallidos.invalidate(clave);
    }

    public int getMaxIntentos() {
        return maxIntentos;
    }
}
