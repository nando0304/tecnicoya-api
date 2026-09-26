package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dto.request.CalificacionRequest;
import com.tecnicoya.api.entity.Servicio;
import com.tecnicoya.api.entity.enums.EstadoServicio;
import com.tecnicoya.api.exception.ConflictoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.repository.CalificacionRepository;
import com.tecnicoya.api.repository.ServicioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalificacionServiceImplTest {

    @Mock
    private CalificacionRepository calificacionRepository;
    @Mock
    private ServicioRepository servicioRepository;
    @InjectMocks
    private CalificacionServiceImpl calificacionService;

    @Test
    void crear_servicioNoFinalizado_lanzaReglaNegocio() {
        when(servicioRepository.findById(4L)).thenReturn(Optional.of(servicio(EstadoServicio.EN_PROCESO)));

        assertThatThrownBy(() -> calificacionService.crear(new CalificacionRequest(4L, 5, "Muy bien")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("FINALIZADOS");
        verify(calificacionRepository, never()).save(any());
    }

    @Test
    void crear_servicioYaCalificado_lanzaConflicto() {
        when(servicioRepository.findById(4L)).thenReturn(Optional.of(servicio(EstadoServicio.FINALIZADO)));
        when(calificacionRepository.existsByServicio_IdServicio(4L)).thenReturn(true);

        assertThatThrownBy(() -> calificacionService.crear(new CalificacionRequest(4L, 4, null)))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("ya fue calificado");
        verify(calificacionRepository, never()).save(any());
    }

    private static Servicio servicio(EstadoServicio estado) {
        Servicio servicio = new Servicio();
        servicio.setIdServicio(4L);
        servicio.setEstadoServicio(estado);
        return servicio;
    }
}
