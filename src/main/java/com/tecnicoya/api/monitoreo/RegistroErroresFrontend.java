package com.tecnicoya.api.monitoreo;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tecnicoya.api.dto.request.ErrorFrontendRequest;
import com.tecnicoya.api.exception.DemasiadosIntentosException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Rastreo de errores del front-end: registra en el log los errores de JavaScript que ocurren en el navegador de los
 * usuarios (que de otro modo nadie vería) y los cuenta en la métrica {@code tecnicoya.errores.frontend}.
 *
 * <p>Los eventos usan el logger {@code FRONTEND} con nivel WARN: no son fallas del servidor, así que no se mezclan
 * con los ERROR de la API. En el log JSON cada dato (url, navegador, tipo, ip) queda en su propio campo.
 *
 * <p>El endpoint que lo usa es público (un error puede ocurrir antes de iniciar sesión), por eso cada IP puede enviar
 * como máximo {@link #MAXIMO_POR_MINUTO} reportes por minuto: así nadie puede llenar el disco con reportes falsos.
 * Todos los textos se recortan y se les escapan los saltos de línea antes de registrarlos (inyección en logs).
 */
@Component
public class RegistroErroresFrontend {

    /** Logger de los errores reportados por el front-end. */
    public static final Logger LOG = LoggerFactory.getLogger("FRONTEND");

    /** Reportes permitidos por IP en un minuto. */
    static final int MAXIMO_POR_MINUTO = 30;

    private static final Duration VENTANA = Duration.ofMinutes(1);
    private static final long MAXIMO_IPS = 10_000;
    private static final int LARGO_TEXTO = 300;
    private static final int LARGO_PILA = 2_000;

    private final Counter contador;
    private final Cache<String, Integer> reportesPorIp;

    /**
     * @param registry registro de métricas de Micrometer
     */
    @Autowired
    public RegistroErroresFrontend(MeterRegistry registry) {
        this(registry, Ticker.systemTicker());
    }

    /** Permite a las pruebas controlar el paso del tiempo con un {@link Ticker} propio. */
    @VisibleForTesting
    RegistroErroresFrontend(MeterRegistry registry, Ticker reloj) {
        this.contador = Counter.builder("tecnicoya.errores.frontend")
                .description("Errores de JavaScript reportados por el front-end")
                .register(registry);
        this.reportesPorIp = CacheBuilder.newBuilder()
                .expireAfterWrite(VENTANA)
                .maximumSize(MAXIMO_IPS)
                .ticker(reloj)
                .build();
    }

    /**
     * Registra un error del front-end en el log y en la métrica.
     *
     * @param error datos del error enviados por el navegador
     * @param ip    dirección IP del cliente
     * @throws DemasiadosIntentosException si esa IP superó los reportes permitidos por minuto
     */
    public void registrar(ErrorFrontendRequest error, String ip) {
        int reportes = reportesPorIp.asMap().merge(ip, 1, Integer::sum);
        if (reportes > MAXIMO_POR_MINUTO) {
            throw new DemasiadosIntentosException(
                    "Se enviaron demasiados reportes de errores; espere un minuto", VENTANA);
        }
        contador.increment();
        LOG.atWarn()
                .addKeyValue("tipo", limpiar(error.tipo(), LARGO_TEXTO))
                .addKeyValue("url", limpiar(error.url(), LARGO_TEXTO))
                .addKeyValue("navegador", limpiar(error.navegador(), LARGO_TEXTO))
                .addKeyValue("ip", limpiar(ip, LARGO_TEXTO))
                .log("ERROR_FRONTEND {} | url={} | pila={}", limpiar(error.mensaje(), LARGO_TEXTO),
                        limpiar(error.url(), LARGO_TEXTO), limpiar(error.pila(), LARGO_PILA));
    }

    /** Recorta el texto y escapa los saltos de línea (como {@code Auditoria.limpiar}, pero con largo variable). */
    private static String limpiar(String valor, int largoMaximo) {
        if (valor == null) {
            return null;
        }
        // El reemplazo va al final: así FindSecBugs (SAST) reconoce que el valor quedó neutralizado
        return StringUtils.abbreviate(valor, largoMaximo).replace("\r", "\\r").replace("\n", "\\n");
    }
}
