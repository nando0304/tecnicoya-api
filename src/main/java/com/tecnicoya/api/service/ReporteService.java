package com.tecnicoya.api.service;

public interface ReporteService {
    String TIPO_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    byte[] reporteUsuarios();

    byte[] reporteTecnicos();

    byte[] reporteServicios();

    byte[] reporteGeneral();
}
