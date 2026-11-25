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

@Component
public class MetricasSistemaOperativo implements MeterBinder {
    static final long VIGENCIA_LECTURA_RED_MS = 5_000;

    static final long VIGENCIA_LISTA_INTERFACES_MS = 60_000;

    private final HardwareAbstractionLayer hardware;
    private final GlobalMemory memoria;
    private final LongSupplier relojNanos;

    private List<NetworkIF> interfaces;
    private long momentoListaInterfaces;
    private LecturaRed ultimaLectura;
    private long momentoUltimaLectura;

    public MetricasSistemaOperativo() {
        this(new SystemInfo().getHardware(), System::nanoTime);
    }

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

    synchronized LecturaRed leerRed() {
        long ahora = relojNanos.getAsLong();
        if (ultimaLectura != null
                && TimeUnit.NANOSECONDS.toMillis(ahora - momentoUltimaLectura) < VIGENCIA_LECTURA_RED_MS) {
            return ultimaLectura;
        }

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

    record LecturaRed(long bytesRecibidos, long bytesEnviados, long errores) {
    }
}
