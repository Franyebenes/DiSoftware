package edu.esi.ds.esiusuarios.auxiliares;

import edu.esi.ds.esiusuarios.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Manager {

    private static Manager yo;

    @Autowired
    private EmailService emailService;

    @Autowired
    public void init(EmailService emailService) {
        this.emailService = emailService;
        yo = this;
    }

    public synchronized static Manager getInstance() {
        return yo;
    }

    public Object getEmailService() {
        return this.emailService;
    }
}