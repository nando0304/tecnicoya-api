package com.tecnicoya.api.service.impl;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import com.tecnicoya.api.dao.ICalificacionDao;
import com.tecnicoya.api.dao.IEstadisticasDao;
import com.tecnicoya.api.dao.IServicioDao;
import com.tecnicoya.api.dao.ITecnicoDao;
import com.tecnicoya.api.dao.IUsuarioDao;
import com.tecnicoya.api.model.Calificacion;
import com.tecnicoya.api.model.enums.EstadoServicio;
import com.tecnicoya.api.model.enums.TipoUsuario;
import com.tecnicoya.api.model.Servicio;
import com.tecnicoya.api.model.Tecnico;
import com.tecnicoya.api.model.Usuario;
import com.tecnicoya.api.service.ReporteService;
import com.tecnicoya.api.util.FechaUtil;
import com.tecnicoya.api.util.LibroExcel;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {
    private static final List<String> COLUMNAS_USUARIOS = ImmutableList.of(
            "ID", "Nombres", "Apellidos", "Correo", "Teléfono", "Tipo", "Estado", "Fecha de registro");
    private static final List<String> COLUMNAS_TECNICOS = ImmutableList.of(
            "ID", "Técnico", "Correo", "Teléfono", "Especialidad", "Verificación",
            "Servicios asignados", "Servicios finalizados", "Calificaciones", "Calificación promedio");
    private static final List<String> COLUMNAS_SERVICIOS = ImmutableList.of(
            "ID", "Título", "Cliente", "Técnico", "Prioridad", "Estado",
            "Fecha de solicitud", "Fecha del servicio", "Fecha de cierre");

    private final IUsuarioDao usuarioDao;
    private final ITecnicoDao tecnicoDao;
    private final IServicioDao servicioDao;
    private final ICalificacionDao calificacionDao;
    private final IEstadisticasDao estadisticasDao;

    @Override
    public byte[] reporteUsuarios() {
        try (LibroExcel libro = new LibroExcel()) {
            agregarUsuarios(libro);
            return generar(libro, "usuarios");
        }
    }

    @Override
    public byte[] reporteTecnicos() {
        try (LibroExcel libro = new LibroExcel()) {
            agregarTecnicos(libro, servicioDao.listar());
            return generar(libro, "técnicos");
        }
    }

    @Override
    public byte[] reporteServicios() {
        try (LibroExcel libro = new LibroExcel()) {
            agregarServicios(libro, servicioDao.listar());
            return generar(libro, "servicios");
        }
    }

    @Override
    public byte[] reporteGeneral() {
        List<Servicio> servicios = servicioDao.listar();
        try (LibroExcel libro = new LibroExcel()) {
            libro.agregarResumen("Resumen", "TécnicoYa - Resumen de la plataforma", indicadores());
            agregarUsuarios(libro);
            agregarTecnicos(libro, servicios);
            agregarServicios(libro, servicios);
            return generar(libro, "general");
        }
    }

    private List<List<?>> indicadores() {
        ImmutableList.Builder<List<?>> filas = ImmutableList.builder();
        filas.add(Arrays.asList("Fecha de generación", FechaUtil.ahora()));
        filas.add(Arrays.asList("Usuarios registrados", usuarioDao.contar()));
        for (Map.Entry<TipoUsuario, Long> tipo : estadisticasDao.contarUsuariosPorTipo().entrySet()) {
            filas.add(Arrays.asList("   Usuarios " + tipo.getKey(), tipo.getValue()));
        }
        filas.add(Arrays.asList("Técnicos con perfil", tecnicoDao.contar()));
        filas.add(Arrays.asList("Servicios registrados", servicioDao.contar()));
        for (Map.Entry<EstadoServicio, Long> estado : estadisticasDao.contarServiciosPorEstado().entrySet()) {
            filas.add(Arrays.asList("   Servicios " + estado.getKey(), estado.getValue()));
        }
        filas.add(Arrays.asList("Ingresos por suscripciones aprobadas", estadisticasDao.sumarPagosAprobados()));
        filas.add(Arrays.asList("Calificación promedio (1 a 5)", estadisticasDao.promedioCalificaciones()));
        return filas.build();
    }

    private void agregarUsuarios(LibroExcel libro) {
        List<List<?>> filas = usuarioDao.listar().stream()
                .<List<?>>map(u -> Arrays.asList(u.getIdUsuario(), u.getNombres(), u.getApellidos(), u.getCorreo(),
                        u.getTelefono(), u.getTipoUsuario(), u.getEstado(), u.getFechaRegistro()))
                .toList();
        libro.agregarHoja("Usuarios", "Usuarios registrados", COLUMNAS_USUARIOS, filas);
    }

    private void agregarTecnicos(LibroExcel libro, List<Servicio> servicios) {
        ImmutableListMultimap<Long, Servicio> serviciosPorTecnico = Multimaps.index(
                FluentIterable.from(servicios).filter(s -> s.getTecnico() != null),
                s -> s.getTecnico().getIdTecnico());
        ImmutableListMultimap<Long, Calificacion> calificacionesPorTecnico = Multimaps.index(
                FluentIterable.from(calificacionDao.listar()).filter(c -> c.getServicio().getTecnico() != null),
                c -> c.getServicio().getTecnico().getIdTecnico());

        List<List<?>> filas = tecnicoDao.listar().stream()
                .<List<?>>map(t -> filaTecnico(t, serviciosPorTecnico.get(t.getIdTecnico()),
                        calificacionesPorTecnico.get(t.getIdTecnico())))
                .toList();
        libro.agregarHoja("Técnicos", "Técnicos y sus indicadores", COLUMNAS_TECNICOS, filas);
    }

    private static List<?> filaTecnico(Tecnico tecnico, List<Servicio> servicios, List<Calificacion> calificaciones) {
        Usuario usuario = tecnico.getUsuario();
        long finalizados = servicios.stream().filter(s -> s.getEstadoServicio() == EstadoServicio.FINALIZADO).count();
        Double promedio = calificaciones.isEmpty() ? null
                : calificaciones.stream().mapToInt(Calificacion::getPuntuacion).average().orElse(0);
        return Arrays.asList(tecnico.getIdTecnico(), usuario.nombreCompleto(), usuario.getCorreo(),
                usuario.getTelefono(), tecnico.getEspecialidad(), tecnico.getEstadoVerificacion(),
                servicios.size(), finalizados, calificaciones.size(), promedio);
    }

    private static void agregarServicios(LibroExcel libro, List<Servicio> servicios) {
        List<List<?>> filas = servicios.stream()
                .<List<?>>map(s -> Arrays.asList(s.getIdServicio(), s.getTitulo(), s.getCliente().nombreCompleto(),
                        s.getTecnico() == null ? "Sin asignar" : s.getTecnico().getUsuario().nombreCompleto(),
                        s.getPrioridad(), s.getEstadoServicio(), s.getFechaSolicitud(), s.getFechaServicio(),
                        s.getFechaCierre()))
                .toList();
        libro.agregarHoja("Servicios", "Servicios solicitados", COLUMNAS_SERVICIOS, filas);
    }

    private static byte[] generar(LibroExcel libro, String reporte) {
        byte[] contenido = libro.aBytes();
        log.info("Reporte {} generado ({} bytes)", reporte, contenido.length);
        return contenido;
    }
}
