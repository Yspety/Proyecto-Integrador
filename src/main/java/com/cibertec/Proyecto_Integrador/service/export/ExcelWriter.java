package com.cibertec.Proyecto_Integrador.service.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import com.cibertec.Proyecto_Integrador.exception.StorageException;

/** Renderiza una {@link TablaReporte} como .xlsx. */
@Component
public class ExcelWriter {

    public byte[] escribir(TablaReporte tabla) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Reporte");
            int columnas = tabla.encabezados().size();
            int fila = 0;

            fila = escribirTitulo(sheet, wb, tabla, columnas, fila);
            fila = escribirResumen(sheet, wb, tabla, fila);
            fila = escribirEncabezados(sheet, wb, tabla, fila);
            escribirFilas(sheet, wb, tabla, fila);

            // Ancho automático DESPUÉS de escribir: mide el contenido ya presente.
            for (int c = 0; c < columnas; c++) {
                sheet.autoSizeColumn(c);
                sheet.setColumnWidth(c, Math.min(sheet.getColumnWidth(c) + 700, 12000));
            }
            sheet.createFreezePane(0, fila);   // los encabezados quedan fijos al scrollear

            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new StorageException("No se pudo generar el Excel del reporte", e);
        }
    }

    private int escribirTitulo(Sheet sheet, Workbook wb, TablaReporte tabla, int columnas, int fila) {
        CellStyle estilo = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        estilo.setFont(font);

        Row row = sheet.createRow(fila++);
        Cell cell = row.createCell(0);
        cell.setCellValue(tabla.titulo());
        cell.setCellStyle(estilo);
        if (columnas > 1) {
            sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, columnas - 1));
        }

        if (tabla.subtitulo() != null && !tabla.subtitulo().isBlank()) {
            sheet.createRow(fila++).createCell(0).setCellValue(tabla.subtitulo());
        }
        return fila + 1;   // línea en blanco de separación
    }

    private int escribirResumen(Sheet sheet, Workbook wb, TablaReporte tabla, int fila) {
        if (tabla.resumen().isEmpty()) {
            return fila;
        }
        CellStyle label = wb.createCellStyle();
        Font bold = wb.createFont();
        bold.setBold(true);
        label.setFont(bold);

        for (TablaReporte.Kpi kpi : tabla.resumen()) {
            Row row = sheet.createRow(fila++);
            Cell c0 = row.createCell(0);
            c0.setCellValue(kpi.label());
            c0.setCellStyle(label);
            row.createCell(1).setCellValue(kpi.valor());
        }
        return fila + 1;
    }

    private int escribirEncabezados(Sheet sheet, Workbook wb, TablaReporte tabla, int fila) {
        CellStyle estilo = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(font);
        estilo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setBorderBottom(BorderStyle.THIN);

        Row row = sheet.createRow(fila++);
        List<String> encabezados = tabla.encabezados();
        for (int c = 0; c < encabezados.size(); c++) {
            Cell cell = row.createCell(c);
            cell.setCellValue(encabezados.get(c));
            cell.setCellStyle(estilo);
        }
        return fila;
    }

    private void escribirFilas(Sheet sheet, Workbook wb, TablaReporte tabla, int fila) {
        CellStyle numerico = wb.createCellStyle();
        numerico.setAlignment(HorizontalAlignment.RIGHT);

        for (List<String> datos : tabla.filas()) {
            Row row = sheet.createRow(fila++);
            for (int c = 0; c < datos.size(); c++) {
                Cell cell = row.createCell(c);
                String valor = datos.get(c);

                // Las columnas numéricas se escriben como NÚMERO, no como texto: si van
                // como texto, en Excel no se pueden sumar ni graficar, y un export que
                // no se puede analizar no sirve de mucho.
                if (tabla.esNumerica(c) && esNumero(valor)) {
                    cell.setCellValue(Double.parseDouble(valor));
                    cell.setCellStyle(numerico);
                } else {
                    cell.setCellValue(valor);
                }
            }
        }
    }

    private static boolean esNumero(String valor) {
        if (valor == null || valor.isBlank()) {
            return false;
        }
        try {
            Double.parseDouble(valor);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
