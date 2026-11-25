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

@Component
public class RegistroErroresFrontend {
    public static final Logger LOG = LoggerFactory.getLogger("FRONTEND");

    static final int MAXIMO_POR_MINUTO = 30;

    private static final Duration VENTANA = Duration.ofMinutes(1);
    private static final long MAXIMO_IPS = 10_000;
    private static final int LARGO_TEXTO = 300;
    private static final int LARGO_PILA = 2_000;

    private final Counter contador;
    private final Cache<String, Integer> reportesPorIp;

    @Autowired
    public RegistroErroresFrontend(MeterRegistry registry) {
        this(registry, Ticker.systemTicker());
    }

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

    private static String limpiar(String valor, int largoMaximo) {
        if (valor == null) {
            return null;
        }

        return StringUtils.abbreviate(valor, largoMaximo).replace("\r", "\\r").replace("\n", "\\n");
    }
}
