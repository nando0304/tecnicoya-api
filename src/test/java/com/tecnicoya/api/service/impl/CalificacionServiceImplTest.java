package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dao.ICalificacionDao;
import com.tecnicoya.api.dao.IServicioDao;
import com.tecnicoya.api.dto.request.CalificacionRequest;
import com.tecnicoya.api.exception.ConflictoException;
import com.tecnicoya.api.exception.ReglaNegocioException;
import com.tecnicoya.api.model.Calificacion;
import com.tecnicoya.api.model.enums.EstadoServicio;
import com.tecnicoya.api.model.Servicio;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalificacionServiceImplTest {

    @Mock
    private ICalificacionDao calificacionDao;
    @Mock
    private IServicioDao servicioDao;
    @InjectMocks
    private CalificacionServiceImpl calificacionService;

    @Test
    void crear_servicioNoFinalizado_lanzaReglaNegocio() {
        when(servicioDao.buscarPorId(4L)).thenReturn(Optional.of(servicio(EstadoServicio.EN_PROCESO)));

        assertThatThrownBy(() -> calificacionService.crear(new CalificacionRequest(4L, 5, "Muy bien")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("FINALIZADOS");
        verify(calificacionDao, never()).guardar(any());
    }

    @Test
    void crear_servicioYaCalificado_lanzaConflicto() {
        when(servicioDao.buscarPorId(4L)).thenReturn(Optional.of(servicio(EstadoServicio.FINALIZADO)));
        when(calificacionDao.existePorServicio(4L)).thenReturn(true);

        assertThatThrownBy(() -> calificacionService.crear(new CalificacionRequest(4L, 4, null)))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("ya fue calificado");
        verify(calificacionDao, never()).guardar(any());
    }

    @Test
    void crear_comentarioEnBlanco_seGuardaComoNull() {
        when(servicioDao.buscarPorId(4L)).thenReturn(Optional.of(servicio(EstadoServicio.FINALIZADO)));
        when(calificacionDao.guardar(any(Calificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        calificacionService.crear(new CalificacionRequest(4L, 5, "   "));

        ArgumentCaptor<Calificacion> guardada = ArgumentCaptor.forClass(Calificacion.class);
        verify(calificacionDao).guardar(guardada.capture());
        assertThat(guardada.getValue().getComentario()).isNull();
    }

    private static Servicio servicio(EstadoServicio estado) {
        Servicio servicio = new Servicio();
        servicio.setIdServicio(4L);
        servicio.setEstadoServicio(estado);
        return servicio;
    }
}
