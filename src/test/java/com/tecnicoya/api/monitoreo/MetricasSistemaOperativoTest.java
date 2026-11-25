package com.tecnicoya.api.monitoreo;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MetricasSistemaOperativoTest {
    private static final long GB = 1024L * 1024 * 1024;

    private final HardwareAbstractionLayer hardware = mock(HardwareAbstractionLayer.class);
    private final GlobalMemory memoria = mock(GlobalMemory.class);
    private final NetworkIF wifi = mock(NetworkIF.class);
    private final NetworkIF ethernet = mock(NetworkIF.class);
    private final AtomicLong relojNanos = new AtomicLong();
    private final MeterRegistry registry = new SimpleMeterRegistry();

    @BeforeEach
    void setUp() {
        when(hardware.getMemory()).thenReturn(memoria);
        when(memoria.getTotal()).thenReturn(16 * GB);
        when(memoria.getAvailable()).thenReturn(6 * GB);
        when(hardware.getNetworkIFs(false)).thenReturn(List.of(wifi, ethernet));
        when(wifi.getBytesRecv()).thenReturn(1_000L);
        when(wifi.getBytesSent()).thenReturn(400L);
        when(wifi.getInErrors()).thenReturn(1L);
        when(ethernet.getBytesRecv()).thenReturn(500L);
        when(ethernet.getBytesSent()).thenReturn(100L);
        when(ethernet.getOutErrors()).thenReturn(2L);
        new MetricasSistemaOperativo(hardware, relojNanos::get).bindTo(registry);
    }

    @Test
    void publicaLaMemoriaRamDelServidor() {
        assertThat(registry.get("sistema.memoria.total").gauge().value()).isEqualTo(16.0 * GB);
        assertThat(registry.get("sistema.memoria.disponible").gauge().value()).isEqualTo(6.0 * GB);
    }

    @Test
    void sumaElTraficoYLosErroresDeTodasLasInterfaces() {
        assertThat(trafico("recibido")).isEqualTo(1_500.0);
        assertThat(trafico("enviado")).isEqualTo(500.0);
        assertThat(registry.get("sistema.red.errores").functionCounter().count()).isEqualTo(3.0);
    }

    @Test
    void reutilizaLaLecturaDeRedDuranteCincoSegundos() {
        trafico("recibido");
        trafico("enviado");
        verify(wifi, times(1)).updateAttributes();

        relojNanos.addAndGet(TimeUnit.MILLISECONDS.toNanos(MetricasSistemaOperativo.VIGENCIA_LECTURA_RED_MS));
        trafico("recibido");
        verify(wifi, times(2)).updateAttributes();

        verify(hardware, times(1)).getNetworkIFs(false);
    }

    private double trafico(String sentido) {
        return registry.get("sistema.red.trafico").tag("sentido", sentido).functionCounter().count();
    }
}
