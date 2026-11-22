package com.tecnicoya.api.monitoreo;

import com.google.common.annotations.VisibleForTesting;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/**
 * Métricas de salud del sistema operativo que la JVM no expone por sí sola, leídas con la librería OSHI.
 *
 * <p>Spring Boot Actuator ya publica el uso de CPU ({@code system.cpu.usage}, {@code process.cpu.usage}), la memoria
 * de la JVM ({@code jvm.memory.used}) y el espacio en disco ({@code disk.free}, {@code disk.total}). Esta clase
 * completa el cuadro con:
 * <ul>
 *   <li>{@code sistema.memoria.total} y {@code sistema.memoria.disponible}: memoria RAM física del servidor.</li>
 *   <li>{@code sistema.red.trafico} (etiqueta {@code sentido} = recibido | enviado): bytes que pasaron por las
 *       interfaces de red del servidor.</li>
 *   <li>{@code sistema.red.errores}: paquetes con error en esas interfaces.</li>
 * </ul>
 * Spring Boot registra automáticamente todo {@link MeterBinder} que sea un bean. Leer las interfaces de red cuesta
 * algo de tiempo, así que los contadores se reutilizan durante {@link #VIGENCIA_LECTURA_RED_MS} milisegundos y la
 * lista de interfaces durante {@link #VIGENCIA_LISTA_INTERFACES_MS}.
 */
@Component
public class MetricasSistemaOperativo implements MeterBinder {

    /** Tiempo durante el que se reutiliza la última lectura de las interfaces de red. */
    static final long VIGENCIA_LECTURA_RED_MS = 5_000;
    /** Tiempo durante el que se reutiliza la lista de interfaces de red (detecta una VPN o un cable nuevo). */
    static final long VIGENCIA_LISTA_INTERFACES_MS = 60_000;

    private final HardwareAbstractionLayer hardware;
    private final GlobalMemory memoria;
    private final LongSupplier relojNanos;

    private List<NetworkIF> interfaces;
    private long momentoListaInterfaces;
    private LecturaRed ultimaLectura;
    private long momentoUltimaLectura;

    /** Lee el hardware real del servidor. */
    public MetricasSistemaOperativo() {
        this(new SystemInfo().getHardware(), System::nanoTime);
    }

    /** Permite a las pruebas usar un hardware simulado y controlar el paso del tiempo. */
    @VisibleForTesting
    MetricasSistemaOperativo(HardwareAbstractionLayer hardware, LongSupplier relojNanos) {
        this.hardware = hardware;
        this.memoria = hardware.getMemory();
        this.relojNanos = relojNanos;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("sistema.memoria.total", memoria, GlobalMemory::getTotal)
                .description("Memoria RAM física total del servidor")
                .baseUnit(BaseUnits.BYTES)
                .register(registry);
        Gauge.builder("sistema.memoria.disponible", memoria, GlobalMemory::getAvailable)
                .description("Memoria RAM física libre del servidor")
                .baseUnit(BaseUnits.BYTES)
                .register(registry);
        FunctionCounter.builder("sistema.red.trafico", this, m -> m.leerRed().bytesRecibidos())
                .description("Bytes recibidos por las interfaces de red del servidor")
                .baseUnit(BaseUnits.BYTES)
                .tag("sentido", "recibido")
                .register(registry);
        FunctionCounter.builder("sistema.red.trafico", this, m -> m.leerRed().bytesEnviados())
                .description("Bytes enviados por las interfaces de red del servidor")
                .baseUnit(BaseUnits.BYTES)
                .tag("sentido", "enviado")
                .register(registry);
        FunctionCounter.builder("sistema.red.errores", this, m -> m.leerRed().errores())
                .description("Paquetes con error (entrada y salida) en las interfaces de red del servidor")
                .register(registry);
    }

    /** Suma los contadores de todas las interfaces de red físicas (sin la interfaz local o loopback). */
    synchronized LecturaRed leerRed() {
        long ahora = relojNanos.getAsLong();
        if (ultimaLectura != null
                && TimeUnit.NANOSECONDS.toMillis(ahora - momentoUltimaLectura) < VIGENCIA_LECTURA_RED_MS) {
            return ultimaLectura;
        }
        // Enumerar las interfaces es lo más lento (en Windows, medio segundo): se repite solo una vez por minuto
        if (interfaces == null
                || TimeUnit.NANOSECONDS.toMillis(ahora - momentoListaInterfaces) >= VIGENCIA_LISTA_INTERFACES_MS) {
            interfaces = hardware.getNetworkIFs(false);
            momentoListaInterfaces = ahora;
        }
        long recibidos = 0;
        long enviados = 0;
        long errores = 0;
        for (NetworkIF red : interfaces) {
            red.updateAttributes();
            recibidos += red.getBytesRecv();
            enviados += red.getBytesSent();
            errores += red.getInErrors() + red.getOutErrors();
        }
        ultimaLectura = new LecturaRed(recibidos, enviados, errores);
        momentoUltimaLectura = ahora;
        return ultimaLectura;
    }

    /** Totales acumulados de las interfaces de red en una lectura. */
    record LecturaRed(long bytesRecibidos, long bytesEnviados, long errores) {
    }
}
