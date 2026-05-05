package edu.esi.ds.esientradas.services;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import brevo.ApiClient;
import brevoApi.TransactionalEmailsApi;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailSender;
import brevoModel.SendSmtpEmailTo;

import edu.esi.ds.esientradas.dao.ConfiguracionDao;

@Service
public class BrevoEmailService extends EmailService {

    @Autowired
    private ConfiguracionDao configuracionDao;

    @Override
    public void enviarEmail(Object... partes) {
        if (partes == null || partes.length == 0) {
            throw new IllegalArgumentException("Se requieren al menos los datos del destinatario");
        }

        String destinatario = partes[0].toString();
        String asunto = "Notificación de Esientradas";
        Object[] cuerpoPartes;

        if (partes.length > 3) {
            asunto = partes[3].toString();
            cuerpoPartes = Arrays.copyOfRange(partes, 4, partes.length);
        } else {
            cuerpoPartes = Arrays.copyOfRange(partes, 1, partes.length);
        }

        String apiKey = configuracionDao.findByClave("brevoApiKey");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = configuracionDao.findByClave("brevoKey");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("La clave de Brevo no está configurada en la base de datos");
        }

        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("https://api.brevo.com/v3");
        apiClient.addDefaultHeader("api-key", apiKey);

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi(apiClient);
        SendSmtpEmail email = new SendSmtpEmail();

        SendSmtpEmailSender sender = new SendSmtpEmailSender();
        sender.setName("Libro");
        sender.setEmail("libroadpweb@gmail.com");
        email.setSender(sender);

        SendSmtpEmailTo to = new SendSmtpEmailTo();
        to.setEmail(destinatario);
        email.setTo(Collections.singletonList(to));
        email.setSubject(asunto);
        email.setHtmlContent(buildHtml(cuerpoPartes));

        StringBuilder textoPlano = new StringBuilder();
        for (Object parte : cuerpoPartes) {
            if (parte != null) {
                textoPlano.append(parte.toString()).append("\n");
            }
        }
        email.setTextContent(textoPlano.toString().trim());

        try {
            apiInstance.sendTransacEmail(email);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar correo con Brevo", e);
        }
    }
}
