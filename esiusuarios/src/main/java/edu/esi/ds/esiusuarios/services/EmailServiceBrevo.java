package edu.esi.ds.esiusuarios.services;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import brevo.ApiClient;
import brevo.Configuration;
import brevo.auth.ApiKeyAuth;
import brevoApi.TransactionalEmailsApi;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailSender;
import brevoModel.SendSmtpEmailTo;

@Primary
@Service
public class EmailServiceBrevo extends EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.api.sender}")
    private String senderEmail;

    @Override
    public void sendEmail(String destinatario, Object... params) {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("ERROR: No se ha configurado la API Key de Brevo en el entorno.");
            return;
        }

        String asunto = "";
        String texto = "";

        for (int i = 0; i < params.length - 1; i += 2) {
            String key = (String) params[i];
            String value = (String) params[i + 1];
            if (key.equals("asunto")) asunto = value;
            if (key.equals("texto")) texto = value;
        }

        try {
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
            apiKeyAuth.setApiKey(apiKey);

            TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();

            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(new SendSmtpEmailSender().name("EsiUsuarios").email(senderEmail));
            sendSmtpEmail.setTo(Collections.singletonList(new SendSmtpEmailTo().email(destinatario)));
            sendSmtpEmail.setSubject(asunto);
            sendSmtpEmail.setHtmlContent(texto);

            apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("Email enviado profesionalmente vía Brevo API a " + destinatario);
        
        } catch (brevo.ApiException e) {
            System.err.println("Fallo en el envío - código: " + e.getCode());
            System.err.println("Fallo en el envío - body: " + e.getResponseBody());
            e.printStackTrace();
        
        } catch (Exception e) {
            System.err.println("Fallo en el envío: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

