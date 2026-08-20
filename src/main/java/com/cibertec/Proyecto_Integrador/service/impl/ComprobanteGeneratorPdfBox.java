package com.cibertec.Proyecto_Integrador.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.cibertec.Proyecto_Integrador.entity.ItemOrden;
import com.cibertec.Proyecto_Integrador.entity.Orden;
import com.cibertec.Proyecto_Integrador.exception.StorageException;
import com.cibertec.Proyecto_Integrador.service.ComprobanteGenerator;

/**
 * Comprobante en PDF con PDFBox, dibujado a mano (sin plantilla externa).
 *
 * <p>Zona horaria Lima para la fecha impresa: el pedido se guarda en UTC, pero el
 * comprobante lo lee una persona en Perú.
 *
 * <p>Las fuentes Standard 14 usan WinAnsiEncoding, que NO cubre todo lo que puede venir
 * en un nombre de producto (comillas tipográficas, emojis, guiones largos). Un carácter
 * fuera del set hace que PDFBox tire IllegalArgumentException al escribir, así que todo
 * texto variable pasa por {@link #sanitize}. Sin eso, un solo producto con un guion
 * largo en el nombre rompe la descarga del comprobante.
 */
@Service
public class ComprobanteGeneratorPdfBox implements ComprobanteGenerator {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(LIMA);

    private static final float MARGIN = 50f;
    private static final float LINE = 16f;

    private final String empresa;
    private final String ruc;

    public ComprobanteGeneratorPdfBox(
            @Value("${app.comprobante.empresa:Krypton S.A.C.}") String empresa,
            @Value("${app.comprobante.ruc:20123456789}") String ruc) {
        this.empresa = empresa;
        this.ruc = ruc;
    }

    @Override
    public byte[] generar(Orden order) {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float y = page.getMediaBox().getHeight() - MARGIN;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                y = header(cs, order, y);
                y = receptor(cs, order, y);
                y = tablaItems(cs, order, y);
                totales(cs, order, y);
            }

            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new StorageException("No se pudo generar el comprobante del pedido " + order.getId(), e);
        }
    }

    // ─── secciones ──────────────────────────────────────────────────────────────

    private float header(PDPageContentStream cs, Orden order, float y) throws IOException {
        text(cs, bold(), 16, MARGIN, y, empresa);
        y -= LINE;
        text(cs, regular(), 10, MARGIN, y, "RUC: " + ruc);
        y -= LINE * 2;

        String titulo = order.getDocumentType().name() + " ELECTRONICA";
        text(cs, bold(), 13, MARGIN, y, titulo);
        y -= LINE;
        text(cs, regular(), 10, MARGIN, y,
                String.format("N%s %08d", "°", order.getId()));
        y -= LINE;
        text(cs, regular(), 10, MARGIN, y, "Fecha: " + FECHA.format(order.getOrderDate()));
        y -= LINE;
        text(cs, regular(), 10, MARGIN, y, "Estado: " + order.getStatus().name());
        return y - LINE * 2;
    }

    private float receptor(PDPageContentStream cs, Orden order, float y) throws IOException {
        boolean esFactura = "FACTURA".equals(order.getDocumentType().name());
        text(cs, bold(), 10, MARGIN, y, esFactura ? "Razon social:" : "Cliente:");
        text(cs, regular(), 10, MARGIN + 90, y, sanitize(order.getCustomerName()));
        y -= LINE;
        text(cs, bold(), 10, MARGIN, y, esFactura ? "RUC:" : "DNI:");
        text(cs, regular(), 10, MARGIN + 90, y, order.getCustomerDoc());
        return y - LINE * 2;
    }

    private float tablaItems(PDPageContentStream cs, Orden order, float y) throws IOException {
        text(cs, bold(), 10, MARGIN, y, "Descripcion");
        text(cs, bold(), 10, 330, y, "Cant.");
        text(cs, bold(), 10, 390, y, "P. Unit.");
        text(cs, bold(), 10, 480, y, "Importe");
        y -= 6;
        line(cs, y);
        y -= LINE;

        for (ItemOrden item : order.getItems()) {
            text(cs, regular(), 9, MARGIN, y, truncar(sanitize(item.getProductName()), 48));
            text(cs, regular(), 9, 330, y, String.valueOf(item.getQuantity()));
            text(cs, regular(), 9, 390, y, money(item.getUnitPrice()));
            text(cs, regular(), 9, 480, y, money(item.getSubtotal()));
            y -= LINE;
        }

        y -= 6;
        line(cs, y);
        return y - LINE;
    }

    private void totales(PDPageContentStream cs, Orden order, float y) throws IOException {
        BigDecimal base = order.getTotal().subtract(order.getIgv());

        y = totalRow(cs, y, "Subtotal", order.getSubtotal(), false);
        if (order.getDiscount().signum() > 0) {
            y = totalRow(cs, y, "Descuento", order.getDiscount().negate(), false);
        }
        y = totalRow(cs, y, "Envio", order.getShippingCost(), false);
        y = totalRow(cs, y, "Op. gravada", base, false);
        y = totalRow(cs, y, "IGV (18%)", order.getIgv(), false);
        totalRow(cs, y, "TOTAL", order.getTotal(), true);
    }

    private float totalRow(PDPageContentStream cs, float y, String label, BigDecimal amount, boolean strong)
            throws IOException {
        text(cs, strong ? bold() : regular(), strong ? 12 : 10, 390, y, label);
        text(cs, strong ? bold() : regular(), strong ? 12 : 10, 480, y, money(amount));
        return y - LINE;
    }

    // ─── helpers ────────────────────────────────────────────────────────────────

    private static PDType1Font regular() {
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }

    private static PDType1Font bold() {
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    }

    private static void text(PDPageContentStream cs, PDType1Font font, float size, float x, float y, String value)
            throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(value);
        cs.endText();
    }

    private static void line(PDPageContentStream cs, float y) throws IOException {
        cs.moveTo(MARGIN, y);
        cs.lineTo(545, y);
        cs.stroke();
    }

    private static String money(BigDecimal amount) {
        return String.format("S/ %,.2f", amount);
    }

    private static String truncar(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }

    /**
     * Deja sólo caracteres que WinAnsiEncoding sabe representar. Las tildes y la ñ
     * sobreviven (están en Latin-1); lo que no, se reemplaza por '?' en lugar de
     * reventar la generación entera.
     */
    private static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            sb.append((c >= 32 && c <= 126) || (c >= 160 && c <= 255) ? c : '?');
        }
        return sb.toString();
    }
}
