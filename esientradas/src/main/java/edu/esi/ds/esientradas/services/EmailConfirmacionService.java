package edu.esi.ds.esientradas.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.esi.ds.esientradas.dao.ConfiguracionDao;
import edu.esi.ds.esientradas.model.Compra;
import edu.esi.ds.esientradas.model.Entrada;

/**
 * Envía el email de confirmación de compra con el PDF adjunto
 * usando la API REST de Brevo directamente (sin SDK),
 * para evitar problemas de compatibilidad con la versión del SDK.
 */
@Service
public class EmailConfirmacionService {

    @Autowired private PDFService       pdfService;
    @Autowired private ConfiguracionDao configuracionDao;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void enviar(String destinatario, Compra compra, List<Entrada> entradas) {

        // 1. Generar PDF y codificar en Base64
        byte[] pdfBytes  = pdfService.generarPdfCompra(compra, entradas);
        String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);

        // 2. Obtener API key desde BD
        String apiKey = configuracionDao.findByClave("brevoApiKey");
        if (apiKey == null || apiKey.isBlank()) apiKey = configuracionDao.findByClave("brevoKey");
        if (apiKey == null || apiKey.isBlank())
            throw new IllegalStateException("Clave de Brevo no configurada en BD");

        // 3. Construir el JSON del email manualmente
        String htmlBody   = buildHtmlEmail(destinatario, compra, entradas);
        String htmlEscaped = htmlBody.replace("\\", "\\\\").replace("\"", "\\\"")
                                     .replace("\n", "\\n").replace("\r", "");
        String referencia = compra.getId() != null ? "#" + compra.getId() : "-";
        String nombrePdf  = "entrada_" + referencia.replace("#", "") + ".pdf";

        String json = "{"
            + "\"sender\":{\"name\":\"esientradas\",\"email\":\"fjyebenesc@gmail.com\"},"
            + "\"to\":[{\"email\":\"" + destinatario + "\"}],"
            + "\"subject\":\"Confirmacion de tu compra - esientradas\","
            + "\"htmlContent\":\"" + htmlEscaped + "\","
            + "\"attachment\":[{"
            +   "\"content\":\"" + pdfBase64 + "\","
            +   "\"name\":\"" + nombrePdf + "\""
            + "}]"
            + "}";

        // 4. Llamada HTTP a la API REST de Brevo
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("Content-Type", "application/json")
                .header("api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new RuntimeException("Brevo devolvio error " + response.statusCode() + ": " + response.body());
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar email con Brevo: " + e.getMessage(), e);
        }
    }

    // ── HTML del cuerpo del email ─────────────────────────────────────────

    private String buildHtmlEmail(String email, Compra compra, List<Entrada> entradas) {
        String fecha      = compra.getCreatedAt().format(FMT);
        String totalEuros = String.format("%.2f", compra.getTotalCentimos() / 100.0);
        String referencia = compra.getId() != null ? "#" + compra.getId() : "-";
        String filas      = buildFilasEntradas(entradas);

        return "<!DOCTYPE html>"
            + "<html lang='es'><head><meta charset='UTF-8'></head>"
            + "<body style='margin:0;padding:0;background:#f8fafc;font-family:Segoe UI,Arial,sans-serif;'>"
            + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f8fafc;padding:40px 0;'>"
            + "<tr><td align='center'>"
            + "<table width='600' cellpadding='0' cellspacing='0' style='background:#fff;border-radius:16px;"
            +     "overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.08);max-width:600px;width:100%;'>"

            // Cabecera
            + "<tr><td style='background:#1e3a8a;padding:32px 40px;text-align:center;'>"
            + "<h1 style='margin:0;color:#fff;font-size:24px;font-weight:900;'>esientradas</h1>"
            + "<p style='margin:8px 0 0;color:#93c5fd;font-size:14px;'>Confirmacion de compra</p>"
            + "</td></tr>"

            // Cuerpo
            + "<tr><td style='padding:36px 40px;'>"
            + "<p style='margin:0 0 8px;font-size:16px;color:#1e293b;'>Hola, <strong>" + email + "</strong></p>"
            + "<p style='margin:0 0 28px;font-size:15px;color:#475569;line-height:1.6;'>"
            + "Tu compra se ha completado correctamente. Tu entrada PDF esta adjunta a este email.</p>"

            // Tabla entradas
            + "<table width='100%' cellpadding='0' cellspacing='0' "
            +     "style='border:1px solid #e2e8f0;border-collapse:separate;border-spacing:0;border-radius:10px;'>"
            + "<thead><tr style='background:#f1f5f9;'>"
            + "<th style='padding:10px 16px;text-align:left;font-size:12px;color:#64748b;font-weight:700;'>Entrada</th>"
            + "<th style='padding:10px 16px;text-align:right;font-size:12px;color:#64748b;font-weight:700;'>Precio</th>"
            + "</tr></thead><tbody>" + filas + "</tbody></table>"

            // Total
            + "<table width='100%' cellpadding='0' cellspacing='0' style='margin-top:20px;'><tr>"
            + "<td style='font-size:16px;font-weight:700;color:#1e293b;'>Total pagado</td>"
            + "<td style='text-align:right;font-size:22px;font-weight:900;color:#1e3a8a;'>" + totalEuros + " EUR</td>"
            + "</tr></table>"

            // Info compra
            + "<div style='margin-top:28px;padding:16px;background:#eff6ff;"
            +     "border-left:4px solid #1e3a8a;border-radius:0 8px 8px 0;'>"
            + "<p style='margin:0;font-size:13px;color:#1e40af;line-height:1.7;'>"
            + "<strong>Fecha:</strong> " + fecha + "<br>"
            + "<strong>Referencia:</strong> " + referencia + "<br>"
            + "<strong>Tu entrada PDF esta adjunta a este email.</strong></p></div>"

            + "<p style='margin:28px 0 0;font-size:14px;color:#64748b;line-height:1.6;'>"
            + "Presenta el PDF adjunto en la entrada del recinto.<br>"
            + "Si tienes algun problema, contactanos respondiendo a este correo.</p>"
            + "</td></tr>"

            // Pie
            + "<tr><td style='background:#f8fafc;padding:20px 40px;text-align:center;border-top:1px solid #e2e8f0;'>"
            + "<p style='margin:0;font-size:12px;color:#94a3b8;'>2025 esientradas - Pago seguro con Stripe</p>"
            + "</td></tr></table></td></tr></table></body></html>";
    }

    private String buildFilasEntradas(List<Entrada> entradas) {
        StringBuilder sb = new StringBuilder();
        for (Entrada entrada : entradas) {
            String espectaculo = "Espectaculo";
            if (entrada.getEspectaculo() != null) {
                espectaculo = entrada.getEspectaculo().getArtista()
                    + " - " + entrada.getEspectaculo().getFecha().format(FMT);
            }
            String precio = String.format("%.2f EUR", entrada.getPrecio() / 100.0);
            sb.append("<tr>")
              .append("<td style='padding:10px 16px;border-bottom:1px solid #e2e8f0;color:#334155;'>")
              .append("Entrada #").append(entrada.getId()).append(" - ").append(espectaculo)
              .append("</td>")
              .append("<td style='padding:10px 16px;border-bottom:1px solid #e2e8f0;"
                    + "text-align:right;font-weight:700;color:#1e3a8a;'>")
              .append(precio).append("</td></tr>");
        }
        return sb.toString();
    }
}