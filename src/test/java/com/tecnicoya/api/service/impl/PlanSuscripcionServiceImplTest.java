package com.tecnicoya.api.service.impl;

import com.tecnicoya.api.dto.request.PlanSuscripcionRequest;
import com.tecnicoya.api.dto.response.PlanSuscripcionResponse;
import com.tecnicoya.api.exception.ConflictoException;
import com.tecnicoya.api.model.PlanSuscripcion;
import com.tecnicoya.api.repository.PlanSuscripcionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanSuscripcionServiceImplTest {

    @Mock
    private PlanSuscripcionRepository planSuscripcionRepository;
    @InjectMocks
    private PlanSuscripcionServiceImpl planSuscripcionService;

    @Test
    void crear_nombreConEspaciosRepetidos_detectaElDuplicado() {
        when(planSuscripcionRepository.existsByNombrePlanIgnoreCase("Plan Pro")).thenReturn(true);

        assertThatThrownBy(() -> planSuscripcionService.crear(request("  Plan   Pro ", null)))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("'Plan Pro'");
        verify(planSuscripcionRepository, never()).save(any());
    }

    @Test
    void crear_descripcionEnBlanco_seGuardaComoNull() {
        when(planSuscripcionRepository.existsByNombrePlanIgnoreCase("Plan Pro")).thenReturn(false);
        when(planSuscripcionRepository.save(any(PlanSuscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        PlanSuscripcionResponse respuesta = planSuscripcionService.crear(request("Plan Pro", " \n "));

        assertThat(respuesta.nombrePlan()).isEqualTo("Plan Pro");
        assertThat(respuesta.descripcion()).isNull();
    }

    private static PlanSuscripcionRequest request(String nombre, String descripcion) {
        return new PlanSuscripcionRequest(nombre, new BigDecimal("29.90"), 10, new BigDecimal("19.90"),
                descripcion, null);
    }
}
