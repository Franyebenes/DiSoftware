package edu.esi.ds.esiusuarios.services;

import org.springframework.stereotype.Service;

//@Primary 
@Service 
public class EmailServiceFalso extends EmailService {

    @Override
    public void sendEmail(String destinatario, Object... params) {
        System.out.println("Enviando email de mentira a " + destinatario);
        for (int i = 0; i < params.length - 1; i += 2) {
            String key = (String) params[i];
            String value = (String) params[i + 1];
            System.out.println(key + ": " + value);
        }
    }
}

