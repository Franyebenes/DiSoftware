package edu.esi.ds.esiusuarios.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service; 


//@Service 
public class EmailServiceReal extends EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendEmail(String destinatario, Object... params) {
        String asunto = "";
        String texto = "";

        for (int i = 0; i < params.length - 1; i += 2) {
            String key = (String) params[i];
            String value = (String) params[i + 1];
            if (key.equals("asunto")) asunto = value;
            if (key.equals("texto")) texto = value;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinatario);
            message.setSubject(asunto);
            message.setText(texto);
            mailSender.send(message);
            System.out.println("Email enviado a " + destinatario);
        } catch (Exception e) {
            System.err.println("Error enviando email: " + e.getMessage());
        }
    }
}