package edu.esi.ds.esientradas.services;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import edu.esi.ds.esientradas.model.Compra;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Espectaculo;

@Service
public class PDFService {

    private static final DateTimeFormatter FMT       = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DeviceRgb        AZUL       = new DeviceRgb(30, 58, 138);
    private static final DeviceRgb        AZUL_CLARO = new DeviceRgb(239, 246, 255);
    private static final DeviceRgb        GRIS       = new DeviceRgb(100, 116, 139);

    public byte[] generarPdfCompra(Compra compra, List<Entrada> entradas) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfDocument pdf      = new PdfDocument(new PdfWriter(baos));
            Document    document = new Document(pdf, PageSize.A4);
            document.setMargins(40, 50, 40, 50);

            PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // ── CABECERA ──────────────────────────────────────────────────
            document.add(new Paragraph("esientradas")
                .setFont(bold).setFontSize(26).setFontColor(AZUL)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(4));

            document.add(new Paragraph("CONFIRMACION DE COMPRA")
                .setFont(regular).setFontSize(10).setFontColor(GRIS)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

            document.add(new Paragraph()
                .setBorderBottom(new SolidBorder(AZUL, 2)).setMarginBottom(20));

            // ── DATOS DE LA COMPRA ────────────────────────────────────────
            String referencia = compra.getId() != null ? "#" + compra.getId() : "-";
            String fecha      = compra.getCreatedAt().format(FMT);
            String total      = String.format("%.2f EUR", compra.getTotalCentimos() / 100.0);

            Table infoCompra = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(24);

            addInfoRow(infoCompra, "Referencia:",      referencia,                    bold, regular);
            addInfoRow(infoCompra, "Fecha de compra:", fecha,                         bold, regular);
            addInfoRow(infoCompra, "Email:",           compra.getUsuarioEmail(),      bold, regular);
            addInfoRow(infoCompra, "Total pagado:",    total,                         bold, bold);
            document.add(infoCompra);

            // ── ENTRADAS ──────────────────────────────────────────────────
            document.add(new Paragraph("ENTRADAS")
                .setFont(bold).setFontSize(11).setFontColor(AZUL).setMarginBottom(10));

            for (int i = 0; i < entradas.size(); i++) {
                document.add(buildEntradaBlock(entradas.get(i), i + 1, bold, regular));
            }

            // ── PIE ───────────────────────────────────────────────────────
            document.add(new Paragraph()
                .setBorderTop(new SolidBorder(GRIS, 0.5f)).setMarginTop(30).setMarginBottom(8));

            document.add(new Paragraph(
                "Presenta este documento en la entrada del recinto. " +
                "Pago procesado de forma segura por Stripe.")
                .setFont(regular).setFontSize(8).setFontColor(GRIS)
                .setTextAlignment(TextAlignment.CENTER));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage(), e);
        }
    }

    private Table buildEntradaBlock(Entrada entrada, int numero, PdfFont bold, PdfFont regular) {
        Espectaculo esp = entrada.getEspectaculo();

        String artista   = esp != null ? esp.getArtista()                      : "-";
        String fechaEsp  = esp != null ? esp.getFecha().format(FMT)             : "-";
        String escenario = esp != null && esp.getEscenario() != null
                           ? esp.getEscenario().getNombre()                     : "-";
        String precio    = String.format("%.2f EUR", entrada.getPrecio() / 100.0);

        Table bloque = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginBottom(12)
            .setBackgroundColor(AZUL_CLARO)
            .setBorder(new SolidBorder(AZUL, 1));

        // Columna izquierda
        Cell izq = new Cell().setBorder(Border.NO_BORDER).setPadding(14);
        izq.add(new Paragraph("ENTRADA #" + entrada.getId())
            .setFont(bold).setFontSize(8).setFontColor(GRIS).setMarginBottom(4));
        izq.add(new Paragraph(artista)
            .setFont(bold).setFontSize(14).setFontColor(AZUL).setMarginBottom(6));
        izq.add(new Paragraph("Fecha: " + fechaEsp)
            .setFont(regular).setFontSize(10).setFontColor(GRIS).setMarginBottom(3));
        izq.add(new Paragraph("Lugar: " + escenario)
            .setFont(regular).setFontSize(10).setFontColor(GRIS));
        bloque.addCell(izq);

        // Columna derecha — precio
        Cell der = new Cell().setBorder(Border.NO_BORDER)
            .setBorderLeft(new SolidBorder(AZUL, 1))
            .setPadding(14)
            .setTextAlignment(TextAlignment.CENTER)
            .setVerticalAlignment(VerticalAlignment.MIDDLE);
        der.add(new Paragraph("PRECIO")
            .setFont(bold).setFontSize(8).setFontColor(GRIS).setMarginBottom(6));
        der.add(new Paragraph(precio)
            .setFont(bold).setFontSize(16).setFontColor(AZUL));
        bloque.addCell(der);

        return bloque;
    }

    private void addInfoRow(Table table, String label, String value, PdfFont boldFont, PdfFont valueFont) {
        table.addCell(new Cell()
            .add(new Paragraph(label).setFont(boldFont).setFontSize(10).setFontColor(GRIS))
            .setBorder(Border.NO_BORDER).setPaddingBottom(6));
        table.addCell(new Cell()
            .add(new Paragraph(value).setFont(valueFont).setFontSize(10))
            .setBorder(Border.NO_BORDER).setPaddingBottom(6));
    }
}