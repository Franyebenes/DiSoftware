package edu.esi.ds.esiusuarios.services;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.esi.ds.esiusuarios.auxiliares.Manager;
import edu.esi.ds.esiusuarios.model.User;

@Service
public class UserService {

    private List<User> users;

    public UserService() {
        this.users = List.of(
            new User("Pepe", "pepe123", "1234"),
            new User("Ana", "ana123", "5678"));
    }

    public String login(String name, String password) {
        for (User user : users) {
            if (user.getName().equals(name) && user.getPassword().equals(password)) {
                return "Login successful";
            }
        }
        return null;
    }

    public String checkToken(String token) {
        for (User user : users) {
            if (user.getToken().equals(token)) {
                return user.getName();
            }
        }
        return null;
    }

    public String register (String emila,String pwd1){
        String email = null;
        User newUser = new User (email, pwd1,String.valueOf(this.users.size()+1));
        this.users.add(newUser);

        ((EmailService) Manager.getInstance().getEmailService()).sendEmail(email,
            "asunto", "Bienvenido a esiusuarios",
            "texto", "Bienvenido al sistema, confirma tu registro aqui: http://localhost:8000/confirm?Token=" + newUser.getToken()
        );
        return "Le hemos enviado la confirmación a: " + email;
    }

}
