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

/**
 * Protección contra ataques de fuerza bruta en el inicio de sesión.
 *
 * <p>Cuenta los intentos fallidos por correo y dirección IP en una caché en memoria de Google Guava
 * ({@link CacheBuilder}). Al llegar a {@code tecnicoya.login.max-intentos} fallos, ese correo queda bloqueado desde
 * esa IP durante {@code tecnicoya.login.minutos-bloqueo} minutos, contados desde el último fallo: cada entrada
 * caduca sola ({@code expireAfterWrite}), sin tareas de limpieza. Un inicio de sesión correcto reinicia el contador.
 */
@Component
public class LimiteIntentosLogin {

    /** Tope de entradas en memoria, para que un atacante no pueda agotarla con miles de correos distintos. */
    private static final long MAXIMO_ENTRADAS = 10_000;

    private final int maxIntentos;
    private final Duration bloqueo;
    private final Cache<String, Integer> intentosFallidos;

    /**
     * @param maxIntentos    intentos fallidos permitidos antes de bloquear
     * @param minutosBloqueo minutos que dura el bloqueo desde el último intento fallido
     */
    @Autowired
    public LimiteIntentosLogin(@Value("${tecnicoya.login.max-intentos:5}") int maxIntentos,
                               @Value("${tecnicoya.login.minutos-bloqueo:15}") long minutosBloqueo) {
        this(maxIntentos, Duration.ofMinutes(minutosBloqueo), Ticker.systemTicker());
    }

    /** Permite a las pruebas controlar el paso del tiempo con un {@link Ticker} propio. */
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

    /**
     * Arma la clave del contador: correo normalizado e IP de origen.
     *
     * @param correo correo con el que se intenta iniciar sesión
     * @param ip     dirección IP del cliente
     * @return clave de la caché
     */
    public static String clave(String correo, String ip) {
        return correo.trim().toLowerCase(Locale.ROOT) + "|" + ip;
    }

    /**
     * Rechaza el intento si la clave está bloqueada.
     *
     * @param clave clave obtenida con {@link #clave(String, String)}
     * @throws DemasiadosIntentosException si ya se alcanzó el máximo de intentos fallidos
     */
    public void verificar(String clave) {
        Integer fallos = intentosFallidos.getIfPresent(clave);
        if (fallos != null && fallos >= maxIntentos) {
            throw new DemasiadosIntentosException(String.format(
                    "Demasiados intentos fallidos de inicio de sesión. Espere %d minutos e inténtelo de nuevo",
                    bloqueo.toMinutes()), bloqueo);
        }
    }

    /**
     * Suma un intento fallido. Escribir en la caché reinicia el tiempo de bloqueo.
     *
     * @param clave clave obtenida con {@link #clave(String, String)}
     * @return intentos fallidos acumulados
     */
    public int registrarFallo(String clave) {
        return intentosFallidos.asMap().merge(clave, 1, Integer::sum);
    }

    /**
     * Borra el contador tras un inicio de sesión correcto.
     *
     * @param clave clave obtenida con {@link #clave(String, String)}
     */
    public void reiniciar(String clave) {
        intentosFallidos.invalidate(clave);
    }

    /** Intentos fallidos permitidos antes del bloqueo. */
    public int getMaxIntentos() {
        return maxIntentos;
    }
}
