package edu.esi.ds.esientradas.services;

public abstract class EmailService {

    public abstract void enviarEmail(Object... partes);

    public void enviar(Object... partes) {
        enviarEmail(partes);
    }

    protected String buildHtml(Object... partes) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head></head><body>");
        for (Object parte : partes) {
            if (parte == null) {
                continue;
            }
            String texto = parte.toString();
            if (texto.startsWith("data:image/")) {
                html.append("<img src=\"").append(texto).append("\" alt=\"imagen\"/>");
            } else if (texto.startsWith("Base64")) {
                html.append("<img src=\"data:image/png;base64,").append(texto.substring(6)).append("\" alt=\"imagen\"/>");
            } else {
                html.append("<p>").append(texto).append("</p>");
            }
        }
        html.append("</body></html>");
        return html.toString();
    }
}

