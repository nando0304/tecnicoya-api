package com.tecnicoya.api.service;

/**
 * Reportes en Excel ({@code .xlsx}) generados con Apache POI.
 *
 * <p>Cada método devuelve el contenido del archivo listo para descargar. Los reportes nunca incluyen las
 * contraseñas de los usuarios.
 */
public interface ReporteService {

    /** Tipo MIME de los archivos {@code .xlsx}. */
    String TIPO_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * Listado de usuarios: datos de contacto, tipo, estado y fecha de registro.
     *
     * @return archivo {@code .xlsx}
     */
    byte[] reporteUsuarios();

    /**
     * Listado de técnicos con sus indicadores: servicios asignados, servicios finalizados y calificación promedio.
     *
     * @return archivo {@code .xlsx}
     */
    byte[] reporteTecnicos();

    /**
     * Listado de servicios con cliente, técnico, prioridad, estado y fechas.
     *
     * @return archivo {@code .xlsx}
     */
    byte[] reporteServicios();

    /**
     * Reporte general: una hoja de resumen con los indicadores de la plataforma y una hoja por cada listado
     * (usuarios, técnicos y servicios).
     *
     * @return archivo {@code .xlsx}
     */
    byte[] reporteGeneral();
}
