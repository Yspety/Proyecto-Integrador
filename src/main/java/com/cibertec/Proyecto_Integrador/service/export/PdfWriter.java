package com.cibertec.Proyecto_Integrador.service.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;
import com.cibertec.Proyecto_Integrador.exception.StorageException;

/**
 * Renderiza una {@link TablaReporte} como PDF apaisado, con paginación.
 *
 * <p>Apaisado porque los reportes tienen más columnas que un comprobante y en vertical
 * quedan apretadas. Repite el encabezado en cada página: una tabla de kardex de tres
 * hojas donde sólo la primera dice qué es cada columna es inservible impresa.
 */
@Component
public class PdfWriter {

    private static final float MARGIN = 40f;
    private static final float LINE = 15f;
    private static final float FOOT = 45f;

    public byte[] escribir(TablaReporte tabla) {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDRectangle apaisado = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            float[] x = calcularColumnas(tabla, apaisado.getWidth());

            PDPage page = new PDPage(apaisado);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float y = apaisado.getHeight() - MARGIN;

            try {
                y = encabezadoDocumento(cs, tabla, y);
                y = resumen(cs, tabla, y);
                y = filaEncabezados(cs, tabla, x, y);

                for (List<String> fila : tabla.filas()) {
                    if (y < FOOT) {
                        cs.close();
                        page = new PDPage(apaisado);
                        doc.addPage(page);
                        cs = new PDPageContentStream(doc, page);
                        y = apaisado.getHeight() - MARGIN;
                        y = filaEncabezados(cs, tabla, x, y);   // encabezado en cada página
                    }
                    for (int c = 0; c < fila.size() && c < x.length; c++) {
                        texto(cs, regular(), 8.5f, x[c], y, recortar(fila.get(c), 28));
                    }
                    y -= LINE;
                }
            } finally {
                cs.close();
            }

            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new StorageException("No se pudo generar el PDF del reporte", e);
        }
    }

    // ─── secciones ──────────────────────────────────────────────────────────────

    private float encabezadoDocumento(PDPageContentStream cs, TablaReporte tabla, float y) throws IOException {
        texto(cs, bold(), 15, MARGIN, y, recortar(tabla.titulo(), 80));
        y -= LINE + 3;
        if (tabla.subtitulo() != null && !tabla.subtitulo().isBlank()) {
            texto(cs, regular(), 9.5f, MARGIN, y, recortar(tabla.subtitulo(), 120));
            y -= LINE;
        }
        return y - 6;
    }

    private float resumen(PDPageContentStream cs, TablaReporte tabla, float y) throws IOException {
        if (tabla.resumen().isEmpty()) {
            return y;
        }
        float x = MARGIN;
        for (TablaReporte.Kpi kpi : tabla.resumen()) {
            texto(cs, regular(), 8.5f, x, y, recortar(kpi.label(), 24));
            texto(cs, bold(), 11.5f, x, y - 13, recortar(kpi.valor(), 20));
            x += 150;
        }
        return y - 34;
    }

    private float filaEncabezados(PDPageContentStream cs, TablaReporte tabla, float[] x, float y)
            throws IOException {
        List<String> encabezados = tabla.encabezados();
        for (int c = 0; c < encabezados.size() && c < x.length; c++) {
            texto(cs, bold(), 8.5f, x[c], y, recortar(encabezados.get(c), 28));
        }
        y -= 5;
        cs.moveTo(MARGIN, y);
        cs.lineTo(x[x.length - 1] + 90, y);
        cs.stroke();
        return y - LINE;
    }

    // ─── helpers ────────────────────────────────────────────────────────────────

    /** Reparte el ancho disponible en columnas iguales. Suficiente para estos reportes. */
    private static float[] calcularColumnas(TablaReporte tabla, float anchoPagina) {
        int n = Math.max(1, tabla.encabezados().size());
        float disponible = anchoPagina - (MARGIN * 2);
        float ancho = disponible / n;
        float[] x = new float[n];
        for (int i = 0; i < n; i++) {
            x[i] = MARGIN + (ancho * i);
        }
        return x;
    }

    private static PDType1Font regular() {
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }

    private static PDType1Font bold() {
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    }

    private static void texto(PDPageContentStream cs, PDType1Font font, float size,
                              float x, float y, String valor) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(sanitize(valor));
        cs.endText();
    }

    private static String recortar(String valor, int max) {
        if (valor == null) {
            return "";
        }
        return valor.length() <= max ? valor : valor.substring(0, max - 1) + "…";
    }

    /**
     * Las fuentes Standard 14 usan WinAnsiEncoding. Un carácter fuera de ese set hace
     * que PDFBox lance al escribir y se cae la descarga entera — mismo motivo que en
     * ComprobanteGeneratorPdfBox.
     */
    private static String sanitize(String valor) {
        if (valor == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(valor.length());
        for (char c : valor.toCharArray()) {
            sb.append((c >= 32 && c <= 126) || (c >= 160 && c <= 255) ? c : '?');
        }
        return sb.toString();
    }
}
