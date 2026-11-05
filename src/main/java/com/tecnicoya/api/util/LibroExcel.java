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

/**
 * Arma un libro de Excel ({@code .xlsx}) con Apache POI.
 *
 * <p>Cada hoja tiene un título, una fila de encabezados con estilo, filtros automáticos, la fila de encabezados
 * fija al desplazarse y anchos de columna ajustados al contenido. Las celdas toman el tipo del valor: números,
 * montos con formato de moneda, fechas con formato de fecha y el resto como texto.
 *
 * <p>Seguridad: los textos se escriben siempre como texto ({@code setCellValue(String)}), nunca como fórmula, por lo
 * que un valor como {@code =HYPERLINK(...)} ingresado por un usuario no se ejecuta al abrir el archivo
 * (previene la "inyección de fórmulas").
 *
 * <pre>{@code
 * try (LibroExcel libro = new LibroExcel()) {
 *     libro.agregarHoja("Usuarios", "Listado de usuarios", List.of("ID", "Nombre"), filas);
 *     byte[] xlsx = libro.aBytes();
 * }
 * }</pre>
 */
public final class LibroExcel implements AutoCloseable {

    /** Ancho máximo de una columna, en caracteres. */
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

    /**
     * Agrega una hoja con un título en la primera fila, los encabezados en la tercera y luego los datos.
     *
     * @param nombre      nombre de la pestaña (se ajusta si tiene caracteres no permitidos por Excel)
     * @param titulo      texto de la primera fila
     * @param encabezados nombres de las columnas
     * @param filas       valores de cada fila, en el mismo orden que los encabezados; puede ser vacía o nula
     * @return esta misma instancia, para encadenar llamadas
     */
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

    /**
     * Agrega una hoja de dos columnas (indicador y valor), útil para resúmenes.
     *
     * @param nombre      nombre de la pestaña
     * @param titulo      texto de la primera fila
     * @param indicadores filas de dos elementos: nombre del indicador y su valor
     * @return esta misma instancia
     */
    public LibroExcel agregarResumen(String nombre, String titulo, List<List<?>> indicadores) {
        return agregarHoja(nombre, titulo, List.of("Indicador", "Valor"), indicadores);
    }

    /**
     * Genera el archivo.
     *
     * @return contenido del archivo {@code .xlsx}
     */
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

    /** Escribe el valor con el tipo y estilo que le corresponde y devuelve su largo aproximado en caracteres. */
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
