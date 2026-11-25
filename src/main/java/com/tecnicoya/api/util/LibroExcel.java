package com.tecnicoya.api.util;

import com.google.common.base.Strings;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public final class LibroExcel implements AutoCloseable {
    private static final int ANCHO_MAXIMO = 60;
    private static final int ANCHO_MINIMO = 8;

    private final XSSFWorkbook libro = new XSSFWorkbook();
    private final CellStyle estiloTitulo;
    private final CellStyle estiloEncabezado;
    private final CellStyle estiloTexto;
    private final CellStyle estiloEntero;
    private final CellStyle estiloMoneda;
    private final CellStyle estiloDecimal;
    private final CellStyle estiloFechaHora;
    private final CellStyle estiloFecha;

    public LibroExcel() {
        Font fuenteTitulo = libro.createFont();
        fuenteTitulo.setBold(true);
        fuenteTitulo.setFontHeightInPoints((short) 14);
        estiloTitulo = libro.createCellStyle();
        estiloTitulo.setFont(fuenteTitulo);

        Font fuenteEncabezado = libro.createFont();
        fuenteEncabezado.setBold(true);
        fuenteEncabezado.setColor(IndexedColors.WHITE.getIndex());
        estiloEncabezado = conBordes(libro.createCellStyle());
        estiloEncabezado.setFont(fuenteEncabezado);
        estiloEncabezado.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        estiloEncabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloEncabezado.setAlignment(HorizontalAlignment.CENTER);

        estiloTexto = conBordes(libro.createCellStyle());
        estiloEntero = conFormato("0");
        estiloMoneda = conFormato("\"S/\" #,##0.00");
        estiloDecimal = conFormato("0.00");
        estiloFechaHora = conFormato("dd/mm/yyyy hh:mm");
        estiloFecha = conFormato("dd/mm/yyyy");
    }

    public LibroExcel agregarHoja(String nombre, String titulo, List<String> encabezados, List<List<?>> filas) {
        checkArgument(!encabezados.isEmpty(), "La hoja debe tener al menos una columna");
        Sheet hoja = libro.createSheet(WorkbookUtil.createSafeSheetName(nombre));
        int[] anchos = new int[encabezados.size()];

        Cell celdaTitulo = hoja.createRow(0).createCell(0);
        celdaTitulo.setCellValue(titulo);
        celdaTitulo.setCellStyle(estiloTitulo);

        int filaEncabezado = 2;
        Row encabezado = hoja.createRow(filaEncabezado);
        for (int c = 0; c < encabezados.size(); c++) {
            Cell celda = encabezado.createCell(c);
            celda.setCellValue(encabezados.get(c));
            celda.setCellStyle(estiloEncabezado);
            anchos[c] = encabezados.get(c).length();
        }

        int numeroFila = filaEncabezado + 1;
        if (CollectionUtils.isEmpty(filas)) {
            hoja.createRow(numeroFila).createCell(0).setCellValue("Sin registros");
        } else {
            for (List<?> valores : filas) {
                Row fila = hoja.createRow(numeroFila++);
                for (int c = 0; c < encabezados.size(); c++) {
                    Object valor = c < valores.size() ? valores.get(c) : null;
                    anchos[c] = Math.max(anchos[c], escribir(fila.createCell(c), valor));
                }
            }
            hoja.setAutoFilter(new CellRangeAddress(filaEncabezado, numeroFila - 1, 0, encabezados.size() - 1));
        }

        hoja.createFreezePane(0, filaEncabezado + 1);
        for (int c = 0; c < anchos.length; c++) {
            int caracteres = Math.min(ANCHO_MAXIMO, Math.max(ANCHO_MINIMO, anchos[c] + 2));
            hoja.setColumnWidth(c, caracteres * 256);
        }
        return this;
    }

    public LibroExcel agregarResumen(String nombre, String titulo, List<List<?>> indicadores) {
        return agregarHoja(nombre, titulo, List.of("Indicador", "Valor"), indicadores);
    }

    public byte[] aBytes() {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo generar el archivo Excel", ex);
        }
    }

    @Override
    public void close() {
        try {
            libro.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private int escribir(Cell celda, Object valor) {
        if (valor instanceof BigDecimal monto) {
            return escribirNumero(celda, monto.doubleValue(), estiloMoneda, monto.toPlainString().length() + 4);
        }
        if (valor instanceof Double decimal) {
            return escribirNumero(celda, decimal, estiloDecimal, 6);
        }
        if (valor instanceof Number numero) {
            return escribirNumero(celda, numero.doubleValue(), estiloEntero, numero.toString().length());
        }
        if (valor instanceof LocalDateTime fechaHora) {
            celda.setCellValue(fechaHora);
            celda.setCellStyle(estiloFechaHora);
            return 16;
        }
        if (valor instanceof LocalDate fecha) {
            celda.setCellValue(fecha);
            celda.setCellStyle(estiloFecha);
            return 10;
        }
        if (valor instanceof LocalTime hora) {
            return escribirTexto(celda, hora.toString());
        }
        if (valor instanceof Enum<?> enumerado) {
            return escribirTexto(celda, enumerado.name().replace('_', ' '));
        }
        return escribirTexto(celda, valor == null ? null : valor.toString());
    }

    private static int escribirNumero(Cell celda, double numero, CellStyle estilo, int largo) {
        celda.setCellValue(numero);
        celda.setCellStyle(estilo);
        return largo;
    }

    private int escribirTexto(Cell celda, String texto) {
        String limpio = Strings.nullToEmpty(texto);
        celda.setCellValue(limpio);
        celda.setCellStyle(estiloTexto);
        return limpio.length();
    }

    private CellStyle conFormato(String formato) {
        CellStyle estilo = conBordes(libro.createCellStyle());
        estilo.setDataFormat(libro.createDataFormat().getFormat(formato));
        return estilo;
    }

    private static CellStyle conBordes(CellStyle estilo) {
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        return estilo;
    }
}
