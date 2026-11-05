package com.tecnicoya.api.service.impl;

import com.google.common.collect.ImmutableMap;
import com.tecnicoya.api.dao.ICalificacionDao;
import com.tecnicoya.api.dao.IEstadisticasDao;
import com.tecnicoya.api.dao.IServicioDao;
import com.tecnicoya.api.dao.ITecnicoDao;
import com.tecnicoya.api.dao.IUsuarioDao;
import com.tecnicoya.api.model.Calificacion;
import com.tecnicoya.api.model.enums.EstadoServicio;
import com.tecnicoya.api.model.enums.EstadoUsuario;
import com.tecnicoya.api.model.enums.EstadoVerificacion;
import com.tecnicoya.api.model.enums.Prioridad;
import com.tecnicoya.api.model.enums.TipoUsuario;
import com.tecnicoya.api.model.Servicio;
import com.tecnicoya.api.model.Tecnico;
import com.tecnicoya.api.model.Usuario;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Genera los reportes con DAO simulados y vuelve a abrir el .xlsx con Apache POI para revisar su contenido. */
@ExtendWith(MockitoExtension.class)
class ReporteServiceImplTest {

    @Mock
    private IUsuarioDao usuarioDao;
    @Mock
    private ITecnicoDao tecnicoDao;
    @Mock
    private IServicioDao servicioDao;
    @Mock
    private ICalificacionDao calificacionDao;
    @Mock
    private IEstadisticasDao estadisticasDao;
    @InjectMocks
    private ReporteServiceImpl reporteService;

    private final Usuario cliente = usuario(2L, "María", "Quispe", TipoUsuario.CLIENTE);
    private final Usuario usuarioTecnico = usuario(3L, "Jorge", "Ramos", TipoUsuario.TECNICO);
    private final Tecnico tecnico = tecnico();

    @Test
    void reporteUsuarios_tieneEncabezadosYDatos_sinContrasena() throws Exception {
        when(usuarioDao.listar()).thenReturn(List.of(cliente));

        try (Workbook libro = abrir(reporteService.reporteUsuarios())) {
            Sheet hoja = libro.getSheet("Usuarios");
            assertThat(textos(hoja.getRow(2))).containsExactly("ID", "Nombres", "Apellidos", "Correo", "Teléfono",
                    "Tipo", "Estado", "Fecha de registro");
            Row fila = hoja.getRow(3);
            assertThat(fila.getCell(0).getNumericCellValue()).isEqualTo(2);
            assertThat(fila.getCell(3).getStringCellValue()).isEqualTo("maria@correo.example");
            assertThat(textos(fila)).doesNotContain("hash-secreto");
        }
    }

    @Test
    void reporteTecnicos_calculaIndicadoresAgrupandoConMultimap() throws Exception {
        Servicio finalizado = servicio(10L, EstadoServicio.FINALIZADO, tecnico);
        Servicio enProceso = servicio(11L, EstadoServicio.EN_PROCESO, tecnico);
        Servicio sinTecnico = servicio(12L, EstadoServicio.PENDIENTE, null);
        when(servicioDao.listar()).thenReturn(List.of(finalizado, enProceso, sinTecnico));
        when(tecnicoDao.listar()).thenReturn(List.of(tecnico));
        when(calificacionDao.listar()).thenReturn(List.of(calificacion(finalizado, 4), calificacion(finalizado, 5)));

        try (Workbook libro = abrir(reporteService.reporteTecnicos())) {
            Row fila = libro.getSheet("Técnicos").getRow(3);
            assertThat(fila.getCell(1).getStringCellValue()).isEqualTo("Jorge Ramos");
            assertThat(fila.getCell(6).getNumericCellValue()).as("servicios asignados").isEqualTo(2);
            assertThat(fila.getCell(7).getNumericCellValue()).as("servicios finalizados").isEqualTo(1);
            assertThat(fila.getCell(9).getNumericCellValue()).as("promedio").isEqualTo(4.5);
        }
    }

    @Test
    void reporteGeneral_incluyeResumenYUnaHojaPorListado() throws Exception {
        when(servicioDao.listar()).thenReturn(List.of());
        when(estadisticasDao.contarUsuariosPorTipo()).thenReturn(ImmutableMap.of(TipoUsuario.CLIENTE, 1L));
        when(estadisticasDao.contarServiciosPorEstado()).thenReturn(ImmutableMap.of(EstadoServicio.PENDIENTE, 0L));
        when(estadisticasDao.sumarPagosAprobados()).thenReturn(new BigDecimal("149.90"));

        try (Workbook libro = abrir(reporteService.reporteGeneral())) {
            assertThat(libro.getNumberOfSheets()).isEqualTo(4);
            assertThat(libro.getSheetName(0)).isEqualTo("Resumen");
            Sheet resumen = libro.getSheet("Resumen");
            boolean tieneIngresos = false;
            for (Row fila : resumen) {
                if (fila.getCell(0) != null && "Ingresos por suscripciones aprobadas".equals(fila.getCell(0).toString())) {
                    tieneIngresos = fila.getCell(1).getNumericCellValue() == 149.90;
                }
            }
            assertThat(tieneIngresos).isTrue();
            assertThat(libro.getSheet("Servicios").getRow(3).getCell(0).getStringCellValue()).isEqualTo("Sin registros");
        }
    }

    private static Workbook abrir(byte[] xlsx) throws Exception {
        assertThat(xlsx).isNotEmpty();
        return new XSSFWorkbook(new ByteArrayInputStream(xlsx));
    }

    private static List<String> textos(Row fila) {
        return java.util.stream.StreamSupport.stream(fila.spliterator(), false).map(Object::toString).toList();
    }

    private static Usuario usuario(Long id, String nombres, String apellidos, TipoUsuario tipo) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setCorreo(nombres.toLowerCase().replace("í", "i") + "@correo.example");
        usuario.setContrasena("hash-secreto");
        usuario.setTipoUsuario(tipo);
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setFechaRegistro(LocalDateTime.of(2026, 1, 15, 10, 30));
        return usuario;
    }

    private Tecnico tecnico() {
        Tecnico t = new Tecnico();
        t.setIdTecnico(1L);
        t.setUsuario(usuarioTecnico);
        t.setEspecialidad("Gasfitería");
        t.setEstadoVerificacion(EstadoVerificacion.VERIFICADO);
        return t;
    }

    private Servicio servicio(Long id, EstadoServicio estado, Tecnico asignado) {
        Servicio servicio = new Servicio();
        servicio.setIdServicio(id);
        servicio.setCliente(cliente);
        servicio.setTecnico(asignado);
        servicio.setTitulo("Servicio " + id);
        servicio.setEstadoServicio(estado);
        servicio.setPrioridad(Prioridad.MEDIA);
        servicio.setFechaSolicitud(LocalDateTime.of(2026, 2, 1, 9, 0));
        return servicio;
    }

    private static Calificacion calificacion(Servicio servicio, int puntuacion) {
        Calificacion calificacion = new Calificacion();
        calificacion.setServicio(servicio);
        calificacion.setPuntuacion(puntuacion);
        return calificacion;
    }
}
