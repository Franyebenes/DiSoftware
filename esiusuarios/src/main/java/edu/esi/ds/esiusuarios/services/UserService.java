package edu.esi.ds.esiusuarios.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.esi.ds.esiusuarios.auxiliares.Manager;
import edu.esi.ds.esiusuarios.dao.UserRepository;
import edu.esi.ds.esiusuarios.model.User;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String login(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        
        if (!optionalUser.isPresent()) {
            return null;
        }
        
        User user = optionalUser.get();
        
        // Verificar que el usuario está confirmado
        if (!user.getConfirmed()) {
            return null;
        }
        
        // Verificar la contraseña con BCrypt
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return null;
        }
        
        return user.getToken();
    }

    public String checkToken(String token) {
        Optional<User> optionalUser = userRepository.findByToken(token);
        
        if (!optionalUser.isPresent()) {
            return null;
        }
        
        User user = optionalUser.get();
        
        // Verificar que el usuario está confirmado
        if (!user.getConfirmed()) {
            return null;
        }
        
        return user.getEmail();
    }

    public String register(String email, String pwd1) {
        // Validar que el email no esté registrado
        if (userRepository.findByEmail(email).isPresent()) {
            return null;
        }
        
        // Validar formato básico de email
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return null;
        }
        
        // Validar contraseña segura: al menos 8 caracteres, mayúscula, minúscula, número
        if (!isPasswordSecure(pwd1)) {
            return null;
        }
        
        // Cifrar la contraseña
        String passwordHash = passwordEncoder.encode(pwd1);
        
        // Generar token único para confirmación
        String token = UUID.randomUUID().toString();
        
        // Crear usuario
        User newUser = new User(email, passwordHash, token);
        userRepository.save(newUser);
        
        // Enviar email de confirmación
        ((EmailService) Manager.getInstance().getEmailService()).sendEmail(email,
            "asunto", "Bienvenido a esiusuarios",
            "texto", "Bienvenido al sistema, confirma tu registro aqui: http://localhost:3000/confirm?token=" + newUser.getToken()
        );
        
        return "Le hemos enviado la confirmación a: " + email;
    }

    public String confirmUser(String token) {
        Optional<User> optionalUser = userRepository.findByToken(token);
        
        if (!optionalUser.isPresent()) {
            return null;
        }
        
        User user = optionalUser.get();
        user.setConfirmed(true);
        userRepository.save(user);
        
        return "Cuenta confirmada exitosamente";
    }

    private boolean isPasswordSecure(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?].*");
        
        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }
}

