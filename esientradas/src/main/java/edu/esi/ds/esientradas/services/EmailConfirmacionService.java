package edu.esi.ds.esientradas.services;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.esi.ds.esientradas.model.Compra;
import edu.esi.ds.esientradas.model.Entrada;

/**
 * Construye el HTML del email de confirmación de compra
 * y lo envía a través de BrevoEmailService.
 *
 * Separado de ComprasService para respetar el principio
 * de responsabilidad única: ComprasService = lógica de negocio,
 * EmailConfirmacionService = construcción y envío del email.
 */
@Service
public class EmailConfirmacionService {

    @Autowired
    private EmailService emailService;   // inyecta BrevoEmailService

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void enviar(String destinatario, Compra compra, List<Entrada> entradas) {

        String asunto   = "✅ Confirmación de tu compra — esientradas";
        String htmlBody = buildHtml(destinatario, compra, entradas);

        // Firma de BrevoEmailService.enviarEmail(Object... partes):
        //   partes[0]  = destinatario
        //   partes[1]  = (ignorado en el flujo normal, usamos partes[3] como asunto)
        //   partes[2]  = (ignorado)
        //   partes[3]  = asunto
        //   partes[4+] = cuerpo: BrevoEmailService llama a buildHtml(partes[4+])
        //
        // PERO buildHtml() envuelve cada parte en <p>...</p>, lo que estropearía
        // nuestro HTML ya construido. Por eso sobreescribimos buildHtml en
        // BrevoEmailService mediante el flag especial "RAW_HTML:".
        //
        // Alternativa más limpia: pasar el HTML completo como única parte[4]
        // con el prefijo RAW_HTML: que BrevoEmailService detecta y usa directamente.

        emailService.enviarEmail(
            destinatario,          // [0] para
            "",                    // [1] ignorado
            "",                    // [2] ignorado
            asunto,                // [3] asunto
            "RAW_HTML:" + htmlBody // [4] cuerpo — BrevoEmailService detecta el prefijo
        );
    }

    // ── Construcción del HTML del email ────────────────────────────────────

    private String buildHtml(String email, Compra compra, List<Entrada> entradas) {

        String fecha      = compra.getCreatedAt().format(FMT);
        String totalEuros = String.format("%.2f", compra.getTotalCentimos() / 100.0);
        String referencia = compra.getId() != null ? "#" + compra.getId() : "—";

        String filas = buildFilasEntradas(entradas);

        return "<!DOCTYPE html>" +
            "<html lang='es'>" +
            "<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'></head>" +
            "<body style='margin:0;padding:0;background:#f8fafc;font-family:Segoe UI,Arial,sans-serif;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f8fafc;padding:40px 0;'>" +
            "<tr><td align='center'>" +
            "<table width='600' cellpadding='0' cellspacing='0' " +
                "style='background:#fff;border-radius:16px;overflow:hidden;" +
                "box-shadow:0 4px 24px rgba(0,0,0,.08);max-width:600px;width:100%;'>" +

            // CABECERA
            "<tr><td style='background:#1e3a8a;padding:32px 40px;text-align:center;'>" +
            "<h1 style='margin:0;color:#fff;font-size:24px;font-weight:900;letter-spacing:-.5px;'>esientradas</h1>" +
            "<p style='margin:8px 0 0;color:#93c5fd;font-size:14px;'>Confirmación de compra</p>" +
            "</td></tr>" +

            // CUERPO
            "<tr><td style='padding:36px 40px;'>" +
            "<p style='margin:0 0 8px;font-size:16px;color:#1e293b;'>Hola, <strong>" + email + "</strong></p>" +
            "<p style='margin:0 0 28px;font-size:15px;color:#475569;line-height:1.6;'>" +
                "Tu compra se ha completado correctamente. Aquí tienes el resumen:</p>" +

            // Tabla entradas
            "<table width='100%' cellpadding='0' cellspacing='0' " +
                "style='border:1px solid #e2e8f0;border-radius:10px;border-collapse:separate;border-spacing:0;'>" +
            "<thead><tr style='background:#f1f5f9;'>" +
            "<th style='padding:10px 16px;text-align:left;font-size:12px;color:#64748b;font-weight:700;" +
                "text-transform:uppercase;letter-spacing:.06em;'>Entrada</th>" +
            "<th style='padding:10px 16px;text-align:right;font-size:12px;color:#64748b;font-weight:700;" +
                "text-transform:uppercase;letter-spacing:.06em;'>Precio</th>" +
            "</tr></thead>" +
            "<tbody>" + filas + "</tbody>" +
            "</table>" +

            // Total
            "<table width='100%' cellpadding='0' cellspacing='0' style='margin-top:20px;'>" +
            "<tr>" +
            "<td style='font-size:16px;font-weight:700;color:#1e293b;'>Total pagado</td>" +
            "<td style='text-align:right;font-size:22px;font-weight:900;color:#1e3a8a;'>" + totalEuros + " €</td>" +
            "</tr></table>" +

            // Info compra
            "<div style='margin-top:28px;padding:16px;background:#eff6ff;border-left:4px solid #1e3a8a;border-radius:0 8px 8px 0;'>" +
            "<p style='margin:0;font-size:13px;color:#1e40af;line-height:1.7;'>" +
            "📅 <strong>Fecha de compra:</strong> " + fecha + "<br>" +
            "🔑 <strong>Referencia:</strong> " + referencia +
            "</p></div>" +

            "<p style='margin:28px 0 0;font-size:14px;color:#64748b;line-height:1.6;'>" +
            "Presenta este email o el número de referencia en la entrada del recinto.<br>" +
            "Si tienes algún problema, contacta con nosotros respondiendo a este correo.</p>" +
            "</td></tr>" +

            // PIE
            "<tr><td style='background:#f8fafc;padding:20px 40px;text-align:center;border-top:1px solid #e2e8f0;'>" +
            "<p style='margin:0;font-size:12px;color:#94a3b8;'>" +
            "© 2025 esientradas · Pago procesado de forma segura por Stripe</p>" +
            "</td></tr>" +

            "</table></td></tr></table>" +
            "</body></html>";
    }

    private String buildFilasEntradas(List<Entrada> entradas) {
        StringBuilder sb = new StringBuilder();
        for (Entrada entrada : entradas) {
            String espectaculo = "Espectáculo";
            if (entrada.getEspectaculo() != null) {
                espectaculo = entrada.getEspectaculo().getArtista()
                    + " — " + entrada.getEspectaculo().getFecha();
            }
            String precio = String.format("%.2f €", entrada.getPrecio() / 100.0);

            sb.append("<tr>")
              .append("<td style='padding:10px 16px;border-bottom:1px solid #e2e8f0;color:#334155;'>")
              .append("Entrada #").append(entrada.getId()).append(" — ").append(espectaculo)
              .append("</td>")
              .append("<td style='padding:10px 16px;border-bottom:1px solid #e2e8f0;")
              .append("text-align:right;font-weight:700;color:#1e3a8a;'>").append(precio).append("</td>")
              .append("</tr>");
        }
        return sb.toString();
    }
}