package edu.esi.ds.esiusuarios.services;

public class EmailServiceFalso extends EmailService {

    @Override
    public void sendEmail(String destinatario, Object... params) {
        System.out.println("Enviando email a " + destinatario);
        for (int i = 0; i < params.length - 1; i += 2) {
            String key = (String) params[i];
            String value = (String) params[i + 1];
            System.out.println(key + ": " + value);
        }
    }
}

