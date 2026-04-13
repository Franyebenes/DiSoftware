package edu.esi.ds.esiusuarios.auxiliares;

import edu.esi.ds.esiusuarios.services.EmailService;
import edu.esi.ds.esiusuarios.services.EmailServiceFalso;

public class Manager {

    private static Manager yo;
    private EmailService emailService;

    private Manager() {
        this.emailService= new EmailServiceFalso();
        yo = this;
    }

    public synchronized static Manager getInstance() {
        if (yo == null) {
            new Manager();
        }
        return yo;
    }

    public Object getEmailService() {
        return this.emailService;
    }
}
