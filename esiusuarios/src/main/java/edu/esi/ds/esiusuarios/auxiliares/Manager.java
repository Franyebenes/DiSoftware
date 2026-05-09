package edu.esi.ds.esiusuarios.auxiliares;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.esi.ds.esiusuarios.services.EmailService;

@Component
public class Manager {

    private static Manager yo;
    
    private EmailService emailService; 

    @Autowired
    public void init(EmailService emailService) {
        this.emailService = emailService;
        yo = this;
    }

    public synchronized static Manager getInstance() {
        return yo;
    }

    public EmailService getEmailService() { 
        return this.emailService;
    }
}